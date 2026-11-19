package com.wakepoint.app.feature.alarms

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.R
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.sound.AlarmSoundRecorder
import com.wakepoint.app.data.sound.SoundStorageRepository
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SoundOptionKind {
    Default,
    Recording,
    Imported
}

data class SoundOption(
    val kind: SoundOptionKind,
    val title: String,
    val soundUri: String?
) {
    val id: String = soundUri ?: "default"
}

data class SoundListUiState(
    val alarm: Alarm? = null,
    val options: List<SoundOption> = emptyList(),
    val isSaving: Boolean = false,
    val isRecording: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
) {
    val selectedId: String = alarm
        ?.let { if (it.soundType == SoundType.Custom) it.soundUri else null }
        ?: "default"
}

@HiltViewModel
class SoundListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository,
    private val soundStorageRepository: SoundStorageRepository,
    private val recorder: AlarmSoundRecorder
) : ViewModel() {
    private val alarmId: String = savedStateHandle.get<String>("alarmId").orEmpty()
    private val transientState = MutableStateFlow(SoundListUiState())
    private val recordings = MutableStateFlow(recorder.listRecordings())

    val uiState: StateFlow<SoundListUiState> = combine(
        alarmRepository.observeAlarms().map { alarms -> alarms.firstOrNull { it.id == alarmId } },
        recordings,
        transientState
    ) { alarm, recordingFiles, transient ->
        transient.copy(
            alarm = alarm,
            options = buildOptions(alarm, recordingFiles)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SoundListUiState()
    )

    fun selectOption(option: SoundOption) {
        val alarm = uiState.value.alarm ?: run {
            setError(context.getString(R.string.sound_alarm_not_found))
            return
        }
        val updated = when (option.kind) {
            SoundOptionKind.Default -> alarm.copy(soundType = SoundType.Default, soundUri = null)
            SoundOptionKind.Recording,
            SoundOptionKind.Imported -> alarm.copy(soundType = SoundType.Custom, soundUri = option.soundUri)
        }
        persist(updated)
    }

    fun importSound(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }.onFailure {
            setError(context.getString(R.string.sound_import_failed))
            return
        }
        val alarm = uiState.value.alarm ?: run {
            setError(context.getString(R.string.sound_alarm_not_found))
            return
        }
        persist(alarm.copy(soundType = SoundType.Custom, soundUri = uri.toString()))
        uploadBestEffort(uri, resolveDisplayName(uri))
    }

    fun startRecording() {
        recorder.start()
            .onSuccess {
                transientState.update { it.copy(isRecording = true, errorMessage = null, message = null) }
            }
            .onFailure {
                setError(context.getString(R.string.sound_record_failed))
            }
    }

    fun stopRecording(): Boolean {
        val file = recorder.stop()
        transientState.update { it.copy(isRecording = false) }
        recordings.value = recorder.listRecordings()
        if (file == null) {
            setError(context.getString(R.string.sound_record_empty))
            return false
        }
        pendingRecording = file
        return true
    }

    fun cancelRecording() {
        recorder.cancel()
        pendingRecording?.delete()
        pendingRecording = null
        transientState.update { it.copy(isRecording = false) }
        recordings.value = recorder.listRecordings()
    }

    fun saveRecording() {
        val file = pendingRecording ?: run {
            setError(context.getString(R.string.sound_record_empty))
            return
        }
        pendingRecording = null
        val alarm = uiState.value.alarm ?: run {
            setError(context.getString(R.string.sound_alarm_not_found))
            return
        }
        val uri = Uri.fromFile(file)
        persist(alarm.copy(soundType = SoundType.Custom, soundUri = uri.toString()))
        uploadBestEffort(uri, file.name)
    }

    fun consumeMessage() {
        transientState.update { it.copy(message = null, errorMessage = null) }
    }

    private var pendingRecording: File? = null

    private fun persist(alarm: Alarm) {
        transientState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = runCatching { alarmRepository.updateAlarm(alarm) }
            transientState.update { state ->
                state.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message
                        ?: if (result.isFailure) context.getString(R.string.sound_select_failed) else null
                )
            }
        }
    }

    private fun uploadBestEffort(uri: Uri, fileName: String) {
        viewModelScope.launch {
            val result = soundStorageRepository.uploadSound(uri, fileName)
            transientState.update { state ->
                state.copy(
                    message = if (result.isSuccess) {
                        context.getString(R.string.sound_upload_done)
                    } else {
                        context.getString(R.string.sound_upload_failed)
                    }
                )
            }
        }
    }

    private fun setError(message: String) {
        transientState.update { it.copy(errorMessage = message) }
    }

    private fun buildOptions(alarm: Alarm?, recordingFiles: List<File>): List<SoundOption> {
        val options = mutableListOf(
            SoundOption(
                kind = SoundOptionKind.Default,
                title = context.getString(R.string.alarm_default_sound),
                soundUri = null
            )
        )
        recordingFiles.forEach { file ->
            options += SoundOption(
                kind = SoundOptionKind.Recording,
                title = file.name,
                soundUri = Uri.fromFile(file).toString()
            )
        }
        val currentUri = alarm
            ?.let { if (it.soundType == SoundType.Custom) it.soundUri else null }
            ?.takeIf { it.isNotBlank() }
        if (currentUri != null && options.none { it.soundUri == currentUri }) {
            val uri = Uri.parse(currentUri)
            options += SoundOption(
                kind = if (uri.scheme == "file") SoundOptionKind.Recording else SoundOptionKind.Imported,
                title = resolveDisplayName(uri),
                soundUri = currentUri
            )
        }
        return options
    }

    private fun resolveDisplayName(uri: Uri): String {
        if (uri.scheme == "file") {
            return uri.lastPathSegment ?: context.getString(R.string.sound_option_custom_label)
        }
        val name = runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
                }
        }.getOrNull()
        return name?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment
            ?: context.getString(R.string.sound_option_custom_label)
    }

    override fun onCleared() {
        if (recorder.isRecording) {
            recorder.cancel()
        }
        super.onCleared()
    }
}
