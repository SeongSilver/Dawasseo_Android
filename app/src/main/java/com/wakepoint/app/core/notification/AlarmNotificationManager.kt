package com.wakepoint.app.core.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.wakepoint.app.MainActivity
import com.wakepoint.app.R
import com.wakepoint.app.domain.model.Alarm

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

    fun buildTrackingNotification(activeAlarmCount: Int): Notification {
        return NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("위치 알람 추적 중")
            .setContentText("활성 알람 ${activeAlarmCount}개를 확인하고 있어요.")
            .setContentIntent(contentIntent())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showArrivalAlarm(alarm: Alarm) {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("목적지에 도착했어요")
            .setContentText(alarm.label)
            .setContentIntent(contentIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 400, 180, 400))
            .build()

        NotificationManagerCompat.from(context).notify(
            alarm.id.hashCode(),
            notification
        )
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ALARM_CHANNEL_ID = "arrival_alarm"
        const val TRACKING_CHANNEL_ID = "location_tracking"
        const val TRACKING_NOTIFICATION_ID = 1001
    }
}
