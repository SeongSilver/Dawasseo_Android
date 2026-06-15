package com.wakepoint.app.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
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

    suspend fun savePushToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PUSH_TOKEN] = token
        }
    }

    private companion object {
        val PUSH_TOKEN = stringPreferencesKey("push_token")
    }
}
