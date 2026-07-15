package com.wakepoint.app.data.sound

import android.content.Context
import android.database.Cursor
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.domain.model.AlarmSound
import com.wakepoint.app.domain.model.SoundKind
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class DefaultSoundRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore
) : SoundRepository {

    private val customDir: File by lazy {
        File(context.filesDir, CUSTOM_DIR_NAME).apply { mkdirs() }
    }

    private val tempRecordingFile: File
        get() = File(context.cacheDir, TEMP_RECORDING_NAME)

    private var recorder: MediaRecorder? = null

    /** 파일 시스템 변경(가져오기/녹음/삭제)을 목록 Flow 로 전파하기 위한 트리거. */
    private val refreshTrigger = MutableStateFlow(0)

    override fun observeSounds(): Flow<List<AlarmSound>> {
        return refreshTrigger.map { loadAllSounds() }
    }

    override fun observeSelectedDefaultSound(): Flow<AlarmSound?> {
        return combine(observeSounds(), userPreferences.defaultSoundUri) { sounds, uri ->
            sounds.firstOrNull { it.uri == uri } ?: sounds.firstOrNull()
        }
    }

    override suspend fun setDefaultSound(sound: AlarmSound) {
        userPreferences.saveDefaultSoundUri(sound.uri)
    }

    override suspend fun importSound(uri: Uri): Result<AlarmSound> = withContext(Dispatchers.IO) {
        runCatching {
            val displayName = queryDisplayName(uri) ?: "가져온 사운드"
            val target = File(customDir, "$IMPORTED_PREFIX${System.currentTimeMillis()}$NAME_SEPARATOR$displayName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: error("오디오 파일을 열 수 없습니다.")
            refreshTrigger.value += 1
            target.toAlarmSound()
        }
    }

    override fun startRecording(): Result<Unit> = runCatching {
        discardRecording()
        val output = tempRecordingFile
        if (output.exists()) output.delete()

        val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        newRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(output.absolutePath)
            prepare()
            start()
        }
        recorder = newRecorder
    }.onFailure { discardRecording() }

    override fun stopRecording(): Result<Unit> = runCatching {
        val current = recorder ?: error("진행 중인 녹음이 없습니다.")
        current.stop()
        current.release()
        recorder = null
    }.onFailure { discardRecording() }

    override suspend fun saveRecordedSound(): Result<AlarmSound> = withContext(Dispatchers.IO) {
        runCatching {
            val temp = tempRecordingFile
            check(temp.exists() && temp.length() > 0) { "저장할 녹음이 없습니다." }
            val target = File(customDir, "$RECORDED_PREFIX${System.currentTimeMillis()}$RECORDED_EXTENSION")
            temp.copyTo(target, overwrite = true)
            temp.delete()
            refreshTrigger.value += 1
            target.toAlarmSound()
        }
    }

    override fun discardRecording() {
        recorder?.let { current ->
            runCatching { current.stop() }
            runCatching { current.release() }
        }
        recorder = null
        runCatching { if (tempRecordingFile.exists()) tempRecordingFile.delete() }
    }

    override suspend fun deleteSound(sound: AlarmSound): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            check(sound.kind != SoundKind.Bundled) { "기본 제공 사운드는 삭제할 수 없습니다." }
            val file = File(sound.uri)
            if (file.exists() && !file.delete()) {
                error("사운드 파일을 삭제하지 못했습니다.")
            }
            // 삭제한 사운드가 기본값이었다면 기본값 선택을 해제.
            if (userPreferences.currentDefaultSoundUri() == sound.uri) {
                userPreferences.saveDefaultSoundUri("")
            }
            refreshTrigger.value += 1
        }
    }

    private fun loadAllSounds(): List<AlarmSound> {
        return loadBundledSounds() + loadCustomSounds()
    }

    private fun loadBundledSounds(): List<AlarmSound> {
        val rawClass = runCatching {
            Class.forName("${context.packageName}.R\$raw")
        }.getOrNull() ?: return emptyList()

        return rawClass.fields.mapNotNull { field ->
            val name = field.name
            runCatching {
                AlarmSound(
                    id = "$BUNDLED_PREFIX$name",
                    title = name.humanizeResourceName(),
                    subtitle = "기본 제공 사운드",
                    uri = "android.resource://${context.packageName}/raw/$name",
                    kind = SoundKind.Bundled
                )
            }.getOrNull()
        }.sortedBy { it.title }
    }

    private fun loadCustomSounds(): List<AlarmSound> {
        val files = customDir.listFiles()?.filter { it.isFile } ?: return emptyList()
        return files.sortedByDescending { it.lastModified() }.map { it.toAlarmSound() }
    }

    private fun File.toAlarmSound(): AlarmSound {
        val fileName = name
        return when {
            fileName.startsWith(RECORDED_PREFIX) -> {
                val millis = fileName
                    .removePrefix(RECORDED_PREFIX)
                    .substringBefore('.')
                    .toLongOrNull()
                AlarmSound(
                    id = fileName,
                    title = "내 녹음",
                    subtitle = millis?.let { recordingDateFormat.format(it) } ?: "직접 녹음한 사운드",
                    uri = absolutePath,
                    kind = SoundKind.Recorded
                )
            }
            fileName.startsWith(IMPORTED_PREFIX) -> {
                val original = fileName.substringAfter(NAME_SEPARATOR, missingDelimiterValue = "가져온 사운드")
                AlarmSound(
                    id = fileName,
                    title = original.substringBeforeLast('.').ifBlank { original },
                    subtitle = "가져온 파일",
                    uri = absolutePath,
                    kind = SoundKind.Imported
                )
            }
            else -> AlarmSound(
                id = fileName,
                title = fileName.substringBeforeLast('.'),
                subtitle = "가져온 파일",
                uri = absolutePath,
                kind = SoundKind.Imported
            )
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor: Cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                cursor.getString(index)
            } else {
                null
            }
        }
    }

    private fun String.humanizeResourceName(): String {
        return split('_')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { it.uppercase(Locale.getDefault()) }
            }
            .ifBlank { this }
    }

    private companion object {
        const val CUSTOM_DIR_NAME = "alarm_sounds"
        const val TEMP_RECORDING_NAME = "recording_temp.m4a"
        const val BUNDLED_PREFIX = "bundled_"
        const val RECORDED_PREFIX = "rec_"
        const val RECORDED_EXTENSION = ".m4a"
        const val IMPORTED_PREFIX = "imp_"
        const val NAME_SEPARATOR = "__"

        val recordingDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
    }
}
