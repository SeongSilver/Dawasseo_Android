package com.wakepoint.app.core.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService

/**
 * 도착 알람 소리를 알람 스트림으로 반복 재생한다.
 * 커스텀 URI 재생에 실패하면 시스템 기본 알람음으로 대체한다.
 */
object AlarmSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    fun start(context: Context, customSoundUri: String?) {
        stop()

        val appContext = context.applicationContext
        val defaultAlarm = defaultUri(appContext, RingtoneManager.TYPE_ALARM)
        val defaultNotification = defaultUri(appContext, RingtoneManager.TYPE_NOTIFICATION)
        val defaultRingtone = defaultUri(appContext, RingtoneManager.TYPE_RINGTONE)

        val primary = resolveAlarmSoundUri(
            custom = customSoundUri,
            defaultAlarm = defaultAlarm,
            defaultNotification = defaultNotification,
            defaultRingtone = defaultRingtone
        ) ?: return
        val fallback = resolveFallbackSoundUri(
            defaultAlarm = defaultAlarm,
            defaultNotification = defaultNotification,
            defaultRingtone = defaultRingtone
        )

        requestAudioFocus(appContext)

        val started = play(appContext, primary)
        if (!started && fallback != null && fallback != primary) {
            play(appContext, fallback)
        }
    }

    fun stop() {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
            }
            runCatching { player.release() }
        }
        mediaPlayer = null
        abandonAudioFocus()
    }

    private fun play(context: Context, uriString: String): Boolean {
        return runCatching {
            val player = MediaPlayer().apply {
                setAudioAttributes(alarmAudioAttributes())
                setDataSource(context, Uri.parse(uriString))
                isLooping = true
                prepare()
                start()
            }
            mediaPlayer = player
            true
        }.getOrElse { error ->
            Log.w(TAG, "Failed to play alarm sound: $uriString", error)
            mediaPlayer?.let { runCatching { it.release() } }
            mediaPlayer = null
            false
        }
    }

    private fun requestAudioFocus(context: Context) {
        val manager = context.getSystemService<AudioManager>() ?: return
        audioManager = manager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(alarmAudioAttributes())
                .build()
            focusRequest = request
            manager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            manager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun abandonAudioFocus() {
        val manager = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { manager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            manager.abandonAudioFocus(null)
        }
        focusRequest = null
        audioManager = null
    }

    private fun defaultUri(context: Context, type: Int): String? {
        return runCatching {
            RingtoneManager.getDefaultUri(type)
                ?: RingtoneManager.getActualDefaultRingtoneUri(context, type)
        }.getOrNull()?.toString()
    }

    private fun alarmAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    private const val TAG = "AlarmSoundPlayer"
}
