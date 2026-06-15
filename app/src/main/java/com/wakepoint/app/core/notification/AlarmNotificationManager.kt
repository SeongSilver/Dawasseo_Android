package com.wakepoint.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

class AlarmNotificationManager(
    private val context: Context
) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "도착 알람",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "목적지 반경 진입 시 울리는 알람"
            enableVibration(true)
        }
        val trackingChannel = NotificationChannel(
            TRACKING_CHANNEL_ID,
            "위치 추적",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "활성 알람 위치 추적 상태"
        }
        notificationManager.createNotificationChannels(
            listOf(alarmChannel, trackingChannel)
        )
    }

    companion object {
        const val ALARM_CHANNEL_ID = "arrival_alarm"
        const val TRACKING_CHANNEL_ID = "location_tracking"
    }
}
