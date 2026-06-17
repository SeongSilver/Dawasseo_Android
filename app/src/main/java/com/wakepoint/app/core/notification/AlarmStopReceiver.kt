package com.wakepoint.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmVibrationController.stop()
        val alarmId = intent.getStringExtra(AlarmAlertContract.EXTRA_ALARM_ID)
        if (alarmId != null) {
            NotificationManagerCompat.from(context).cancel(alarmId.hashCode())
        }
    }
}
