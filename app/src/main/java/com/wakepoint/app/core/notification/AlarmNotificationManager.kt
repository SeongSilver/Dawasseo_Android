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
import com.wakepoint.app.feature.alarmalert.AlarmAlertActivity

class AlarmNotificationManager(
    private val context: Context
) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            context.getString(R.string.alarm_alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.alarm_alert_channel_description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(false)
        }
        val trackingChannel = NotificationChannel(
            TRACKING_CHANNEL_ID,
            context.getString(R.string.location_tracking_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.location_tracking_channel_description)
        }
        notificationManager.createNotificationChannels(
            listOf(alarmChannel, trackingChannel)
        )
    }

    fun buildTrackingNotification(activeAlarmCount: Int): Notification {
        return NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(context.getString(R.string.location_tracking_title))
            .setContentText(context.getString(R.string.location_tracking_message, activeAlarmCount))
            .setContentIntent(contentIntent())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showArrivalAlarm(alarm: Alarm) {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(context.getString(R.string.alarm_alert_arrived))
            .setContentText(alarm.displayTarget())
            .setContentIntent(alarmAlertIntent(alarm))
            .setFullScreenIntent(alarmAlertIntent(alarm), true)
            .setDeleteIntent(stopAlarmIntent(alarm.id))
            .addAction(
                R.drawable.ic_launcher,
                context.getString(R.string.alarm_alert_stop),
                stopAlarmIntent(alarm.id)
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        AlarmVibrationController.start(context)
        NotificationManagerCompat.from(context).notify(
            alarm.id.hashCode(),
            notification
        )
    }

    private fun alarmAlertIntent(alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmAlertContract.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmAlertContract.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmAlertContract.EXTRA_TARGET_ADDRESS, alarm.targetAddress)
        }
        return PendingIntent.getActivity(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopAlarmIntent(alarmId: String): PendingIntent {
        val intent = Intent(context, AlarmStopReceiver::class.java).apply {
            putExtra(AlarmAlertContract.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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

private fun Alarm.displayTarget(): String {
    return targetAddress.ifBlank { label }
}
