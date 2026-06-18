package com.wakepoint.app.core.notification

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService

object AlarmVibrationController {
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stop() }
    private var vibrator: Vibrator? = null

    fun start(context: Context) {
        stop()
        vibrator = context.alarmVibrator()
        val pattern = longArrayOf(0, 700, 250, 700)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, 0),
                alarmAudioAttributes()
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0, alarmAudioAttributes())
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
        handler.postDelayed(stopRunnable, MAX_VIBRATION_MILLIS)
    }

    fun stop() {
        handler.removeCallbacks(stopRunnable)
        vibrator?.cancel()
        vibrator = null
    }

    private fun Context.alarmVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService<VibratorManager>()?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun alarmAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    private const val MAX_VIBRATION_MILLIS = 30_000L
}
