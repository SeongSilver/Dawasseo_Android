package com.wakepoint.app.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wakepoint.app.data.auth.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
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

    val authSession: Flow<AuthSession?> = combine(
        dataStore.data.map { preferences -> preferences[AUTH_USER_ID] },
        dataStore.data.map { preferences -> preferences[AUTH_EMAIL] },
        dataStore.data.map { preferences -> preferences[AUTH_ACCESS_TOKEN] },
        dataStore.data.map { preferences -> preferences[AUTH_REFRESH_TOKEN] }
    ) { userId, email, accessToken, refreshToken ->
        if (userId.isNullOrBlank() || email.isNullOrBlank() || accessToken.isNullOrBlank()) {
            null
        } else {
            AuthSession(
                userId = userId,
                email = email,
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }
    }

    suspend fun savePushToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PUSH_TOKEN] = token
        }
    }

    suspend fun saveAuthSession(session: AuthSession) {
        dataStore.edit { preferences ->
            preferences[AUTH_USER_ID] = session.userId
            preferences[AUTH_EMAIL] = session.email
            preferences[AUTH_ACCESS_TOKEN] = session.accessToken
            session.refreshToken?.let { preferences[AUTH_REFRESH_TOKEN] = it }
                ?: preferences.remove(AUTH_REFRESH_TOKEN)
        }
    }

    suspend fun clearAuthSession() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_USER_ID)
            preferences.remove(AUTH_EMAIL)
            preferences.remove(AUTH_ACCESS_TOKEN)
            preferences.remove(AUTH_REFRESH_TOKEN)
        }
    }

    private companion object {
        val PUSH_TOKEN = stringPreferencesKey("push_token")
        val AUTH_USER_ID = stringPreferencesKey("auth_user_id")
        val AUTH_EMAIL = stringPreferencesKey("auth_email")
        val AUTH_ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        val AUTH_REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
    }
}
