package com.wakepoint.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.wakepoint.app.core.notification.AlarmAlertContract
import com.wakepoint.app.core.notification.AlarmNotificationManager
import com.wakepoint.app.core.notification.AlarmVibrationController

class AlarmAlertService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stopAlarm() }
    private lateinit var notificationManager: AlarmNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = AlarmNotificationManager(this)
        notificationManager.createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getStringExtra(AlarmAlertContract.EXTRA_ALARM_ID).orEmpty()
        if (alarmId.isBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val label = intent?.getStringExtra(AlarmAlertContract.EXTRA_ALARM_LABEL).orEmpty()
        val targetAddress = intent?.getStringExtra(AlarmAlertContract.EXTRA_TARGET_ADDRESS).orEmpty()

        val notification = notificationManager.buildArrivalNotification(
            alarmId = alarmId,
            label = label,
            targetAddress = targetAddress
        )
        startForeground(AlarmAlertContract.notificationId(alarmId), notification)
        AlarmVibrationController.start(this)

        handler.removeCallbacks(stopRunnable)
        handler.postDelayed(stopRunnable, MAX_RING_MILLIS)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(stopRunnable)
        AlarmVibrationController.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopAlarm() {
        handler.removeCallbacks(stopRunnable)
        AlarmVibrationController.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val ACTION_STOP = "com.wakepoint.app.action.STOP_ALARM_ALERT"
        private const val MAX_RING_MILLIS = 30_000L

        fun startIntent(
            context: Context,
            alarmId: String,
            label: String,
            targetAddress: String
        ): Intent {
            return Intent(context, AlarmAlertService::class.java).apply {
                putExtra(AlarmAlertContract.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmAlertContract.EXTRA_ALARM_LABEL, label)
                putExtra(AlarmAlertContract.EXTRA_TARGET_ADDRESS, targetAddress)
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, AlarmAlertService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }
}
