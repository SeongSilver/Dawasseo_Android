package com.wakepoint.app.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wakepoint.app.core.push.PushTokenSyncManager
import com.wakepoint.app.data.alarm.AlarmRepository
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
    lateinit var pushTokenSyncManager: PushTokenSyncManager

    @Inject
    lateinit var alarmRepository: AlarmRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            pushTokenSyncManager.persistAndUpload(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // 친구가 보낸 알람 등 서버 변경 알림 → 서버의 최신 알람을 로컬에 반영하고 위치 추적을 갱신.
        Log.i(TAG, "FCM message received from ${message.from.orEmpty()}")
        serviceScope.launch {
            runCatching { alarmRepository.refreshAlarms() }
                .onFailure { Log.w(TAG, "FCM 수신 후 알람 새로고침 실패", it) }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "WakepointFcm"
    }
}
