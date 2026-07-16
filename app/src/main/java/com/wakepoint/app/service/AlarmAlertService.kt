package com.wakepoint.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.wakepoint.app.core.audio.AlarmAudioPlayer
import com.wakepoint.app.feature.alarmalert.AlarmAlertActivity
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
        val soundUri = intent?.getStringExtra(AlarmAlertContract.EXTRA_SOUND_URI)
        val soundEnabled = intent?.getBooleanExtra(AlarmAlertContract.EXTRA_SOUND_ENABLED, true) ?: true
        val vibrate = intent?.getBooleanExtra(AlarmAlertContract.EXTRA_VIBRATE, true) ?: true

        val notification = notificationManager.buildArrivalNotification(
            alarmId = alarmId,
            label = label,
            targetAddress = targetAddress
        )
        startForeground(AlarmAlertContract.notificationId(alarmId), notification)

        // Try to bring the full-screen "알람 끄기" UI to the foreground directly.
        // The full-screen-intent notification already covers the locked-screen
        // case; this covers foreground/unlocked devices where it would otherwise
        // only appear as a heads-up notification.
        runCatching {
            startActivity(
                AlarmAlertActivity.launchIntent(
                    context = this,
                    alarmId = alarmId,
                    label = label,
                    targetAddress = targetAddress
                )
            )
        }

        if (soundEnabled) {
            AlarmAudioPlayer.start(this, soundUri)
        }
        if (vibrate) {
            AlarmVibrationController.start(this)
        }

        handler.removeCallbacks(stopRunnable)
        handler.postDelayed(stopRunnable, MAX_RING_MILLIS)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(stopRunnable)
        AlarmAudioPlayer.stop()
        AlarmVibrationController.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopAlarm() {
        handler.removeCallbacks(stopRunnable)
        AlarmAudioPlayer.stop()
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
            targetAddress: String,
            soundUri: String?,
            soundEnabled: Boolean,
            vibrate: Boolean
        ): Intent {
            return Intent(context, AlarmAlertService::class.java).apply {
                putExtra(AlarmAlertContract.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmAlertContract.EXTRA_ALARM_LABEL, label)
                putExtra(AlarmAlertContract.EXTRA_TARGET_ADDRESS, targetAddress)
                putExtra(AlarmAlertContract.EXTRA_SOUND_URI, soundUri)
                putExtra(AlarmAlertContract.EXTRA_SOUND_ENABLED, soundEnabled)
                putExtra(AlarmAlertContract.EXTRA_VIBRATE, vibrate)
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, AlarmAlertService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }
}
