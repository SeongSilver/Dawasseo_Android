package com.wakepoint.app.data.alarm

import com.wakepoint.app.core.supabase.SupabaseConfig
import com.wakepoint.app.core.location.LocationTrackingController
import com.wakepoint.app.data.alarm.local.AlarmDao
import com.wakepoint.app.data.alarm.local.toDomain
import com.wakepoint.app.data.alarm.local.toEntity
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
import org.json.JSONObject

@Singleton
class DefaultAlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val authRepository: AuthRepository,
    private val config: SupabaseConfig,
    private val locationTrackingController: LocationTrackingController
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

    override suspend fun saveAlarm(alarm: Alarm) {
        val accessToken = requireAccessToken()
        insertRemoteAlarm(alarm, accessToken)
        alarmDao.upsertAlarm(alarm.toEntity())
        syncTracking()
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        val accessToken = requireAccessToken()
        patchRemoteAlarm(
            alarmId = alarm.id,
            body = JSONObject()
                .put("label", alarm.label)
                .put("radius_km", alarm.radiusKm)
                .put(
                    "sound_type",
                    when (alarm.soundType) {
                        SoundType.Default -> "default"
                        SoundType.Custom -> "custom"
                    }
                )
                .put("sound_uri", alarm.soundUri),
            accessToken = accessToken
        )
        alarmDao.updateAlarmSettings(
            alarmId = alarm.id,
            label = alarm.label,
            radiusKm = alarm.radiusKm,
            soundType = alarm.soundType.name,
            soundUri = alarm.soundUri
        )
        syncTracking()
    }

    override suspend fun setAlarmActive(alarmId: String, isActive: Boolean) {
        val accessToken = requireAccessToken()
        patchRemoteAlarm(
            alarmId = alarmId,
            body = JSONObject().put("is_active", isActive),
            accessToken = accessToken
        )
        alarmDao.setAlarmActive(alarmId, isActive)
        syncTracking()
    }

    override suspend fun deleteAlarm(alarmId: String) {
        val accessToken = requireAccessToken()
        deleteRemoteAlarm(alarmId, accessToken)
        alarmDao.deleteAlarm(alarmId)
        syncTracking()
    }

    override suspend fun syncLocationTracking() {
        syncTracking()
    }

    override suspend fun markTriggered(alarmId: String, triggeredAt: String) {
        val accessToken = requireAccessToken()
        patchRemoteAlarm(
            alarmId = alarmId,
            body = JSONObject()
                .put("is_active", false)
                .put("triggered_at", triggeredAt),
            accessToken = accessToken
        )
        alarmDao.markTriggered(alarmId, triggeredAt)
        syncTracking()
    }

    private suspend fun syncTracking() {
        locationTrackingController.syncTracking(alarmDao.countActiveAlarms())
    }

    private suspend fun requireAccessToken(): String {
        check(config.isConfigured) {
            "Supabase 설정이 비어 있습니다. local.properties에 SUPABASE_URL과 SUPABASE_ANON_KEY를 입력해주세요."
        }
        return authRepository.requireValidSession().accessToken
    }

    private suspend fun insertRemoteAlarm(alarm: Alarm, accessToken: String) {
        val url = URL("${config.url.trimEnd('/')}/rest/v1/alarms")
        executeRemoteRequest(
            url = url,
            method = "POST",
            body = alarm.toDto().toJson(),
            accessToken = accessToken,
            prefer = "return=minimal"
        )
    }

    private suspend fun patchRemoteAlarm(
        alarmId: String,
        body: JSONObject,
        accessToken: String
    ) {
        val url = URL("${config.url.trimEnd('/')}/rest/v1/alarms?id=eq.$alarmId")
        executeRemoteRequest(
            url = url,
            method = "PATCH",
            body = body,
            accessToken = accessToken,
            prefer = "return=minimal"
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
    ) = withContext(Dispatchers.IO) {
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
    }

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
