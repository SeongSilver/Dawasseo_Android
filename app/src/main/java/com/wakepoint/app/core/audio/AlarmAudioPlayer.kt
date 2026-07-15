package com.wakepoint.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import java.io.File

/**
 * 알람 도착 시 선택된 사운드를 반복 재생한다.
 *
 * [AlarmVibrationController] 와 동일하게 프로세스 전역 상태로 동작하며,
 * [AlarmAlertService] 의 시작/종료 시점에 맞춰 [start]/[stop] 을 호출한다.
 *
 * soundUri 가 비어 있거나 재생에 실패하면 시스템 기본 알람 벨소리로 폴백한다.
 */
object AlarmAudioPlayer {
    private const val TAG = "AlarmAudioPlayer"

    private var player: MediaPlayer? = null

    fun start(context: Context, soundUri: String?) {
        stop()

        val uri = resolveUri(context, soundUri) ?: run {
            Log.w(TAG, "재생 가능한 알람 사운드를 찾지 못했습니다.")
            return
        }

        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(alarmAudioAttributes())
                setDataSource(context, uri)
                isLooping = true
                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "알람 사운드 재생 오류 what=$what extra=$extra")
                    stop()
                    true
                }
                prepare()
                start()
            }
        }.getOrElse { error ->
            Log.w(TAG, "알람 사운드 재생 실패, 기본 벨소리로 폴백합니다.", error)
            playFallback(context)
            null
        }
    }

    fun stop() {
        player?.let { current ->
            runCatching {
                if (current.isPlaying) current.stop()
            }
            runCatching { current.reset() }
            runCatching { current.release() }
        }
        player = null
    }

    private fun playFallback(context: Context) {
        val fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(alarmAudioAttributes())
                setDataSource(context, fallback)
                isLooping = true
                prepare()
                start()
            }
        }.getOrNull()
    }

    private fun resolveUri(context: Context, soundUri: String?): Uri? {
        if (soundUri.isNullOrBlank()) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
        // 커스텀(녹음/가져오기) 사운드는 파일 절대 경로로 저장된다.
        if (soundUri.startsWith("/")) {
            val file = File(soundUri)
            return if (file.exists()) Uri.fromFile(file) else null
        }
        return runCatching { Uri.parse(soundUri) }.getOrNull()
    }

    private fun alarmAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setLegacyStreamType(AudioManager.STREAM_ALARM)
            .build()
    }
}
