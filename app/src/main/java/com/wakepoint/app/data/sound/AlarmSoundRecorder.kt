package com.wakepoint.app.data.sound

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records microphone audio into the app's private `files/sounds` folder as an AAC `.m4a` file.
 * The caller is responsible for the `RECORD_AUDIO` runtime permission.
 */
@Singleton
class AlarmSoundRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    val isRecording: Boolean
        get() = recorder != null

    fun soundsDirectory(): File = File(context.filesDir, SOUNDS_DIRECTORY).apply { mkdirs() }

    fun listRecordings(): List<File> {
        return soundsDirectory()
            .listFiles { file -> file.isFile && file.extension.equals(RECORDING_EXTENSION, ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
    }

    fun start(): Result<File> = runCatching {
        check(recorder == null) { "이미 녹음 중입니다." }
        val file = File(soundsDirectory(), "${UUID.randomUUID()}.$RECORDING_EXTENSION")
        val mediaRecorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mediaRecorder
        outputFile = file
        file
    }

    /** Stops the recording and returns the finished file, or null when nothing was recording. */
    fun stop(): File? {
        val mediaRecorder = recorder ?: return null
        val file = outputFile
        runCatching { mediaRecorder.stop() }
        mediaRecorder.release()
        recorder = null
        outputFile = null
        return file?.takeIf { it.exists() && it.length() > 0L }
    }

    /** Stops the recording if running and deletes the partial file. */
    fun cancel() {
        val file = stop()
        file?.delete()
    }

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    private companion object {
        const val SOUNDS_DIRECTORY = "sounds"
        const val RECORDING_EXTENSION = "m4a"
    }
}
