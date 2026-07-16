package com.wakepoint.app.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wakepoint.app.data.auth.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wakepoint_user_preferences"
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.userPreferencesDataStore

    val pushToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PUSH_TOKEN]
    }

    val defaultSoundUri: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DEFAULT_SOUND_URI]?.takeIf { it.isNotBlank() }
    }

    val wakeupCount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[WAKEUP_COUNT]?.toInt() ?: 0
    }

    /** 사용자가 고른 기기(시스템) 알람음 목록. (uri, title) 쌍. */
    val systemSounds: Flow<List<Pair<String, String>>> = dataStore.data.map { preferences ->
        preferences[SYSTEM_SOUNDS].orEmpty().mapNotNull { entry ->
            val uri = entry.substringBefore('\t')
            val title = entry.substringAfter('\t', missingDelimiterValue = "")
            if (uri.isBlank()) null else uri to title
        }
    }

    val authSession: Flow<AuthSession?> = dataStore.data.map { preferences ->
        val userId = preferences[AUTH_USER_ID]
        val email = preferences[AUTH_EMAIL]
        val accessToken = preferences[AUTH_ACCESS_TOKEN]
        if (userId.isNullOrBlank() || email.isNullOrBlank() || accessToken.isNullOrBlank()) {
            null
        } else {
            AuthSession(
                userId = userId,
                email = email,
                nickname = preferences[AUTH_NICKNAME].orEmpty(),
                accessToken = accessToken,
                refreshToken = preferences[AUTH_REFRESH_TOKEN],
                expiresAtEpochSeconds = preferences[AUTH_EXPIRES_AT] ?: 0L
            )
        }
    }

    suspend fun savePushToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PUSH_TOKEN] = token
        }
    }

    suspend fun saveDefaultSoundUri(uri: String) {
        dataStore.edit { preferences ->
            if (uri.isBlank()) {
                preferences.remove(DEFAULT_SOUND_URI)
            } else {
                preferences[DEFAULT_SOUND_URI] = uri
            }
        }
    }

    suspend fun currentDefaultSoundUri(): String? = defaultSoundUri.first()

    /** 오프라인 등으로 아직 전송하지 못한 친구 알람들(직렬화된 DTO JSON). */
    suspend fun pendingFriendAlarms(): Set<String> =
        dataStore.data.first()[PENDING_FRIEND_ALARMS] ?: emptySet()

    suspend fun addPendingFriendAlarm(dtoJson: String) {
        dataStore.edit { preferences ->
            val existing = preferences[PENDING_FRIEND_ALARMS].orEmpty().toMutableSet()
            existing.add(dtoJson)
            preferences[PENDING_FRIEND_ALARMS] = existing
        }
    }

    suspend fun removePendingFriendAlarm(dtoJson: String) {
        dataStore.edit { preferences ->
            val existing = preferences[PENDING_FRIEND_ALARMS]?.toMutableSet() ?: return@edit
            existing.remove(dtoJson)
            preferences[PENDING_FRIEND_ALARMS] = existing
        }
    }

    suspend fun addSystemSound(uri: String, title: String) {
        if (uri.isBlank()) return
        dataStore.edit { preferences ->
            val existing = preferences[SYSTEM_SOUNDS].orEmpty().toMutableSet()
            existing.removeAll { it.substringBefore('\t') == uri }
            existing.add("$uri\t$title")
            preferences[SYSTEM_SOUNDS] = existing
        }
    }

    suspend fun removeSystemSound(uri: String) {
        dataStore.edit { preferences ->
            val existing = preferences[SYSTEM_SOUNDS]?.toMutableSet() ?: return@edit
            existing.removeAll { it.substringBefore('\t') == uri }
            preferences[SYSTEM_SOUNDS] = existing
        }
    }

    suspend fun incrementWakeupCount() {
        dataStore.edit { preferences ->
            preferences[WAKEUP_COUNT] = (preferences[WAKEUP_COUNT] ?: 0L) + 1L
        }
    }

    suspend fun saveAuthSession(session: AuthSession) {
        dataStore.edit { preferences ->
            preferences[AUTH_USER_ID] = session.userId
            preferences[AUTH_EMAIL] = session.email
            preferences[AUTH_ACCESS_TOKEN] = session.accessToken
            preferences[AUTH_EXPIRES_AT] = session.expiresAtEpochSeconds
            if (session.nickname.isNotBlank()) {
                preferences[AUTH_NICKNAME] = session.nickname
            }
            session.refreshToken?.let { preferences[AUTH_REFRESH_TOKEN] = it }
                ?: preferences.remove(AUTH_REFRESH_TOKEN)
        }
    }

    suspend fun saveNickname(nickname: String) {
        dataStore.edit { preferences ->
            if (nickname.isBlank()) {
                preferences.remove(AUTH_NICKNAME)
            } else {
                preferences[AUTH_NICKNAME] = nickname
            }
        }
    }

    suspend fun clearAuthSession() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_USER_ID)
            preferences.remove(AUTH_EMAIL)
            preferences.remove(AUTH_NICKNAME)
            preferences.remove(AUTH_ACCESS_TOKEN)
            preferences.remove(AUTH_REFRESH_TOKEN)
            preferences.remove(AUTH_EXPIRES_AT)
        }
    }

    private companion object {
        val PUSH_TOKEN = stringPreferencesKey("push_token")
        val DEFAULT_SOUND_URI = stringPreferencesKey("default_sound_uri")
        val SYSTEM_SOUNDS = stringSetPreferencesKey("system_sounds")
        val PENDING_FRIEND_ALARMS = stringSetPreferencesKey("pending_friend_alarms")
        val WAKEUP_COUNT = longPreferencesKey("wakeup_count")
        val AUTH_USER_ID = stringPreferencesKey("auth_user_id")
        val AUTH_EMAIL = stringPreferencesKey("auth_email")
        val AUTH_NICKNAME = stringPreferencesKey("auth_nickname")
        val AUTH_ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        val AUTH_REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
        val AUTH_EXPIRES_AT = longPreferencesKey("auth_expires_at")
    }
}
