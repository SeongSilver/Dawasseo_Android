package com.wakepoint.app.data.alarm

import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.core.network.retryingNetwork
import com.wakepoint.app.core.supabase.SupabaseConfig
import com.wakepoint.app.core.location.LocationTrackingController
import com.wakepoint.app.data.alarm.local.AlarmDao
import com.wakepoint.app.data.alarm.local.AlarmEntity
import com.wakepoint.app.data.alarm.local.toDomain
import com.wakepoint.app.data.alarm.local.toEntity
import com.wakepoint.app.data.alarm.remote.alarmDtoFromJson
import com.wakepoint.app.data.alarm.remote.toDomain
import com.wakepoint.app.data.alarm.remote.toDto
import com.wakepoint.app.data.alarm.remote.toJson
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class DefaultAlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val authRepository: AuthRepository,
    private val config: SupabaseConfig,
    private val locationTrackingController: LocationTrackingController,
    private val userPreferences: UserPreferencesDataStore
) : AlarmRepository {
    override fun observeAlarms(): Flow<List<Alarm>> {
        return alarmDao.observeAlarms().map { alarms ->
            alarms.map { it.toDomain() }
        }
    }

    override fun observeActiveAlarms(): Flow<List<Alarm>> {
        return alarmDao.observeActiveAlarms().map { alarms ->
            alarms.map { it.toDomain() }
        }
    }

    override suspend fun refreshAlarms() {
        if (!config.isConfigured) return
        val session = runCatching { authRepository.requireValidSession() }.getOrNull() ?: return

        // 서버 상태를 받기 전에, 아직 반영되지 못한 로컬 변경분(내 알람·친구 전송분·삭제분)을 먼저 올려
        // 유실/역전을 막는다. 특히 삭제분을 먼저 확정해야 방금 지운 알람이 아래 병합에서 되살아나지 않는다.
        pushPendingAlarms(session.accessToken)
        pushPendingFriendAlarms(session.accessToken)
        pushPendingDeletions(session.accessToken)

        val remote = fetchRemoteAlarms(ownerId = session.userId, accessToken = session.accessToken)
        val existing = alarmDao.getAllAlarms().associateBy { it.id }
        // 원격 삭제가 아직 확정되지 못한(오프라인 등) 알람 id — 병합 시 다시 만들지 않는다.
        val pendingDeletions = userPreferences.pendingDeletions()

        // 원격 필드로 갱신하되, 기기 로컬 전용 설정(진동/즐겨찾기/사운드 on)은 보존한다.
        remote.forEach { dto ->
            // 로컬에서 삭제했지만 서버 삭제가 밀린 알람은 되살리지 않는다.
            if (dto.id in pendingDeletions) return@forEach
            val local = existing[dto.id]
            // 아직 서버에 올리지 못한 로컬 변경(껐다/설정 변경 등)은 서버 값으로 덮어쓰지 않는다.
            // 이 가드가 없으면 "현재 위치에서 껐는데 refresh 가 다시 켜는" 무한 재활성 루프가 생긴다.
            if (local?.pendingSync == true) return@forEach
            alarmDao.upsertAlarm(
                AlarmEntity(
                    id = dto.id,
                    ownerId = dto.ownerId,
                    createdBy = dto.createdBy,
                    label = dto.label,
                    targetLat = dto.targetLat,
                    targetLng = dto.targetLng,
                    targetAddress = dto.targetAddress,
                    radiusKm = dto.radiusKm,
                    isActive = dto.isActive,
                    soundType = if (dto.soundType == "custom") SoundType.Custom.name else SoundType.Default.name,
                    soundUri = dto.soundUri,
                    soundEnabled = local?.soundEnabled ?: true,
                    vibrate = local?.vibrate ?: true,
                    isFavorite = local?.isFavorite ?: false,
                    repeating = local?.repeating ?: false,
                    pendingSync = false
                )
            )
        }

        // 서버에서 삭제된(다른 기기/친구가 지운) 알람은 로컬에서도 제거하되,
        // 아직 서버에 반영되지 못한 pending 로컬 알람은 지우지 않는다.
        val remoteIds = remote.map { it.id }
        if (remoteIds.isEmpty()) {
            alarmDao.deleteSyncedAlarms()
        } else {
            alarmDao.deleteSyncedAlarmsNotIn(remoteIds)
        }
        syncTracking()
    }

    override suspend fun saveAlarm(alarm: Alarm) {
        // 로컬 우선 저장: 오프라인이어도 유실 없이 즉시 위치 추적을 시작할 수 있다.
        // 원격 반영에 성공하면 pending 을 해제하고, 실패하면 pending 으로 남겨 다음 동기화 때 재전송한다.
        alarmDao.upsertAlarm(alarm.copy(pendingSync = true).toEntity())
        syncTracking()
        pushRow(alarm.id)
    }

    override suspend fun sendAlarmToFriend(alarm: Alarm) {
        // 이 알람의 소유자는 친구이므로 로컬에 캐시하거나 내 위치 추적에 반영하지 않고 원격에만 저장한다.
        // 전송 실패(오프라인 등) 시 유실을 막기 위해 대기 큐에 넣고 다음 동기화 때 재전송한다.
        val dtoJson = alarm.toDto().toJson().toString()
        val result = runCatching { insertRemoteAlarm(alarm, requireAccessToken()) }
        if (result.isFailure) {
            userPreferences.addPendingFriendAlarm(dtoJson)
        }
    }

    override suspend fun fetchSentAlarms(): List<Alarm> {
        if (!config.isConfigured) return emptyList()
        val session = authRepository.requireValidSession()
        return fetchRemoteSentAlarms(session.userId, session.accessToken).map { it.toDomain() }
    }

    override suspend fun pendingSentAlarms(): List<Alarm> =
        userPreferences.pendingFriendAlarms().mapNotNull { dtoJson ->
            runCatching { alarmDtoFromJson(JSONObject(dtoJson)).toDomain() }.getOrNull()
        }

    override suspend fun cancelSentAlarm(alarmId: String) {
        // 원격 행 삭제(멱등). 아직 전송 대기 중인 항목이었다면 대기 큐에서도 제거해 재전송을 막는다.
        userPreferences.pendingFriendAlarms().forEach { dtoJson ->
            val pendingId = runCatching { JSONObject(dtoJson).optString("id") }.getOrNull()
            if (pendingId == alarmId) userPreferences.removePendingFriendAlarm(dtoJson)
        }
        val session = authRepository.requireValidSession()
        deleteRemoteAlarm(alarmId, session.accessToken)
    }

    /** 내가 만든(created_by=나) + 소유자가 내가 아닌(owner_id≠나) 원격 알람 = 친구에게 보낸 알람. */
    private suspend fun fetchRemoteSentAlarms(
        userId: String,
        accessToken: String
    ): List<com.wakepoint.app.data.alarm.remote.AlarmDto> = retryingNetwork { withContext(Dispatchers.IO) {
        val url = URL(
            "${config.url.trimEnd('/')}/rest/v1/alarms?created_by=eq.$userId&owner_id=neq.$userId&select=*"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("apikey", config.anonKey)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
        }
        val responseText = connection.readResponseText()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(parseErrorMessage(responseText))
        }
        val array = JSONArray(responseText.ifBlank { "[]" })
        (0 until array.length()).map { index ->
            alarmDtoFromJson(array.getJSONObject(index))
        }
    } }

    override suspend fun updateAlarm(alarm: Alarm) {
        // 로컬 먼저 갱신 후 원격은 멱등 upsert 로 반영(실패 시 pending 으로 남아 재전송).
        alarmDao.updateAlarmSettings(
            alarmId = alarm.id,
            label = alarm.label,
            radiusKm = alarm.radiusKm,
            soundType = alarm.soundType.name,
            soundUri = alarm.soundUri,
            vibrate = alarm.vibrate,
            repeating = alarm.repeating
        )
        syncTracking()
        pushRow(alarm.id)
    }

    override suspend fun setAlarmActive(alarmId: String, isActive: Boolean) {
        alarmDao.setAlarmActive(alarmId, isActive)
        syncTracking()
        pushRow(alarmId)
    }

    override suspend fun setFavorite(alarmId: String, isFavorite: Boolean) {
        // 즐겨찾기는 기기 로컬 설정으로만 유지한다(원격 스키마 미변경).
        alarmDao.setFavorite(alarmId, isFavorite)
    }

    override suspend fun deleteAlarm(alarmId: String) {
        // 로컬 먼저 삭제(오프라인에도 즉시 삭제 반영)하고 삭제 대기에 기록한다.
        // 삭제 대기 기록은 이후 refresh 병합이나 트리거 재삽입으로 알람이 되살아나는 것을 원천 차단한다.
        alarmDao.deleteAlarm(alarmId)
        userPreferences.addPendingDeletion(alarmId)
        syncTracking()
        // 서버에도 삭제 반영(DELETE 는 멱등이라 로컬 전용 알람이어도 무해). 성공하면 다음 동기화 때
        // pushPendingDeletions 가 대기 목록을 정리한다. 실패(오프라인 등)해도 대기 목록에 남아 재시도된다.
        val session = runCatching { authRepository.requireValidSession() }.getOrNull() ?: return
        runCatching { deleteRemoteAlarm(alarmId, session.accessToken) }
    }

    override suspend fun syncLocationTracking() {
        // 앱 열림/부팅 등 동기화 시점에 밀린 원격 반영(내 알람·친구 전송분·삭제분)을 함께 재시도한다.
        runCatching {
            val token = requireAccessToken()
            pushPendingAlarms(token)
            pushPendingFriendAlarms(token)
            pushPendingDeletions(token)
        }
        syncTracking()
    }

    override suspend fun handleTriggeredAlarm(alarmId: String) {
        userPreferences.incrementWakeupCount()
        // 계속 유지(반복) 알람은 울려도 비활성화하지 않고 그대로 둔다.
        // 반경 이탈 후 재진입 시 다시 울리도록 재무장은 위치 추적 서비스가 담당한다.
        val entity = alarmDao.getAlarm(alarmId)
        if (entity?.repeating == true) return
        // 일반 알람: 삭제하지 않고 비활성화해 목록에 남긴다(재사용/즐겨찾기 가능).
        alarmDao.setAlarmActive(alarmId, false)
        syncTracking()
        pushRow(alarmId)
    }

    private suspend fun syncTracking() {
        locationTrackingController.syncTracking(alarmDao.countActiveAlarms())
    }

    /** 한 알람의 현재 로컬 상태를 원격에 멱등 반영하고, 성공/실패에 따라 pending 플래그를 갱신한다. */
    private suspend fun pushRow(alarmId: String) {
        val entity = alarmDao.getAlarm(alarmId) ?: return
        val synced = runCatching {
            insertRemoteAlarm(entity.toDomain(), requireAccessToken())
        }.isSuccess
        alarmDao.setPendingSync(alarmId, !synced)
    }

    /** 아직 서버에 반영되지 못한 로컬 알람들을 일괄 재전송한다. */
    private suspend fun pushPendingAlarms(accessToken: String) {
        alarmDao.pendingSyncAlarms().forEach { entity ->
            runCatching {
                insertRemoteAlarm(entity.toDomain(), accessToken)
                alarmDao.setPendingSync(entity.id, false)
            }
        }
    }

    /** 로컬에서 삭제했지만 서버 삭제가 밀린 알람들을 일괄 재삭제하고, 성공분은 대기 목록에서 제거한다. */
    private suspend fun pushPendingDeletions(accessToken: String) {
        userPreferences.pendingDeletions().forEach { alarmId ->
            runCatching {
                deleteRemoteAlarm(alarmId, accessToken)
                userPreferences.removePendingDeletion(alarmId)
            }
        }
    }

    /** 오프라인 등으로 전송하지 못한 친구 알람들을 일괄 재전송한다. */
    private suspend fun pushPendingFriendAlarms(accessToken: String) {
        userPreferences.pendingFriendAlarms().forEach { dtoJson ->
            runCatching {
                val url = URL("${config.url.trimEnd('/')}/rest/v1/alarms?on_conflict=id")
                executeRemoteRequest(
                    url = url,
                    method = "POST",
                    body = JSONObject(dtoJson),
                    accessToken = accessToken,
                    prefer = "return=minimal,resolution=merge-duplicates"
                )
                userPreferences.removePendingFriendAlarm(dtoJson)
            }
        }
    }

    private suspend fun requireAccessToken(): String {
        check(config.isConfigured) {
            "Supabase 설정이 비어 있습니다. local.properties에 SUPABASE_URL과 SUPABASE_ANON_KEY를 입력해주세요."
        }
        return authRepository.requireValidSession().accessToken
    }

    private suspend fun fetchRemoteAlarms(
        ownerId: String,
        accessToken: String
    ): List<com.wakepoint.app.data.alarm.remote.AlarmDto> = retryingNetwork { withContext(Dispatchers.IO) {
        val url = URL(
            "${config.url.trimEnd('/')}/rest/v1/alarms?owner_id=eq.$ownerId&select=*"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("apikey", config.anonKey)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
        }
        val responseText = connection.readResponseText()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(parseErrorMessage(responseText))
        }
        val array = JSONArray(responseText.ifBlank { "[]" })
        (0 until array.length()).map { index ->
            alarmDtoFromJson(array.getJSONObject(index))
        }
    } }

    // 멱등 upsert(on_conflict=id). 재시도/유령 행 재전송 시에도 409 없이 안전하게 반영된다.
    private suspend fun insertRemoteAlarm(alarm: Alarm, accessToken: String) {
        val url = URL("${config.url.trimEnd('/')}/rest/v1/alarms?on_conflict=id")
        executeRemoteRequest(
            url = url,
            method = "POST",
            body = alarm.toDto().toJson(),
            accessToken = accessToken,
            prefer = "return=minimal,resolution=merge-duplicates"
        )
    }

    private suspend fun deleteRemoteAlarm(alarmId: String, accessToken: String) {
        val url = URL("${config.url.trimEnd('/')}/rest/v1/alarms?id=eq.$alarmId")
        executeRemoteRequest(
            url = url,
            method = "DELETE",
            body = null,
            accessToken = accessToken,
            prefer = "return=minimal"
        )
    }

    private suspend fun executeRemoteRequest(
        url: URL,
        method: String,
        body: JSONObject?,
        accessToken: String,
        prefer: String
    ) = retryingNetwork { withContext(Dispatchers.IO) {
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = body != null
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", config.anonKey)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Prefer", prefer)
        }

        if (body != null) {
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }
        }

        val responseText = connection.readResponseText()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(parseErrorMessage(responseText))
        }
    } }

    private fun HttpURLConnection.readResponseText(): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        if (stream == null) return ""
        return stream.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
    }

    private fun parseErrorMessage(responseText: String): String {
        if (responseText.isBlank()) return "알람 저장 요청에 실패했습니다."
        return runCatching {
            val json = JSONObject(responseText)
            listOf("message", "details", "hint", "code")
                .firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
        }.getOrNull() ?: "알람 저장 요청에 실패했습니다."
    }
}
