package com.wakepoint.app.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.core.notification.AlarmNotificationManager
import com.wakepoint.app.data.auth.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WakepointFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var userPreferences: UserPreferencesDataStore

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var alarmNotificationManager: AlarmNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            userPreferences.savePushToken(token)
            authRepository.syncPushToken().onFailure { error ->
                Log.w(TAG, "Push token sync failed: ${error.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        when (data[KEY_TYPE]) {
            TYPE_ALARM_INVITATION -> {
                alarmNotificationManager.createChannels()
                alarmNotificationManager.showInvitationNotification(
                    invitationId = data[KEY_INVITATION_ID],
                    requesterNickname = data[KEY_REQUESTER_NICKNAME].orEmpty(),
                    label = data[KEY_LABEL].orEmpty()
                )
            }
            else -> Log.i(TAG, "Unhandled FCM message type: ${data[KEY_TYPE].orEmpty()}")
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "WakepointFcm"
        const val KEY_TYPE = "type"
        const val KEY_INVITATION_ID = "invitation_id"
        const val KEY_REQUESTER_NICKNAME = "requester_nickname"
        const val KEY_LABEL = "label"
        const val TYPE_ALARM_INVITATION = "alarm_invitation"
    }
}
