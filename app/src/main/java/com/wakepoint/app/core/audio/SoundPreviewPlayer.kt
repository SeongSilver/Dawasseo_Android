package com.wakepoint.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.io.File

/**
 * 사운드 목록 화면에서 항목을 선택했을 때 짧게 미리 들려주기 위한 재생기.
 * 알람 재생과 달리 반복하지 않고, 한 번에 하나의 미리듣기만 유지한다.
 */
object SoundPreviewPlayer {
    private const val TAG = "SoundPreviewPlayer"

    private var player: MediaPlayer? = null

    fun play(context: Context, soundUri: String) {
        stop()
        val uri = resolveUri(soundUri) ?: return
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, uri)
                setOnCompletionListener { stop() }
                prepare()
                start()
            }
        }.getOrElse { error ->
            Log.w(TAG, "미리듣기 재생 실패", error)
            null
        }
    }

    fun stop() {
        player?.let { current ->
            runCatching { if (current.isPlaying) current.stop() }
            runCatching { current.reset() }
            runCatching { current.release() }
        }
        player = null
    }

    private fun resolveUri(soundUri: String): Uri? {
        if (soundUri.isBlank()) return null
        if (soundUri.startsWith("/")) {
            val file = File(soundUri)
            return if (file.exists()) Uri.fromFile(file) else null
        }
        return runCatching { Uri.parse(soundUri) }.getOrNull()
    }
}
