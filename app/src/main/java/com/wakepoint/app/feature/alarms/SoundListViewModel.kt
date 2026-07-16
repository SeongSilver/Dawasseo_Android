package com.wakepoint.app.feature.alarms

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.core.audio.SoundPreviewPlayer
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.sound.SoundRepository
import com.wakepoint.app.domain.model.AlarmSound
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RecordingPhase { Idle, Recording, Recorded }

data class SoundListUiState(
    val sounds: List<AlarmSound> = emptyList(),
    val selectedUri: String? = null,
    val isPerAlarm: Boolean = false,
    val recordSheetVisible: Boolean = false,
    val recordingPhase: RecordingPhase = RecordingPhase.Idle,
    val elapsedSeconds: Int = 0,
    val errorMessage: String? = null
) {
    val bundledSounds: List<AlarmSound> = sounds.filter { it.kind == com.wakepoint.app.domain.model.SoundKind.Bundled }
    val customSounds: List<AlarmSound> = sounds.filterNot { it.kind == com.wakepoint.app.domain.model.SoundKind.Bundled }
}

@HiltViewModel
class SoundListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val soundRepository: SoundRepository,
    private val alarmRepository: AlarmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** null 이면 전역 기본 사운드 편집, 값이 있으면 해당 알람의 사운드 편집. */
    private val alarmId: String? = savedStateHandle.get<String>(ARG_ALARM_ID)?.takeIf { it.isNotBlank() }

    private val transientState = MutableStateFlow(SoundListUiState(isPerAlarm = alarmId != null))

    private var timerJob: Job? = null

    private val selectionUri = if (alarmId != null) {
        alarmRepository.observeAlarms().map { alarms ->
            alarms.firstOrNull { it.id == alarmId }?.soundUri
        }
    } else {
        soundRepository.observeSelectedDefaultSound().map { it?.uri }
    }

    val uiState: StateFlow<SoundListUiState> = combine(
        soundRepository.observeSounds(),
        selectionUri,
        transientState
    ) { sounds, selectedUri, transient ->
        transient.copy(sounds = sounds, selectedUri = selectedUri)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SoundListUiState(isPerAlarm = alarmId != null)
    )

    fun selectSound(sound: AlarmSound) {
        SoundPreviewPlayer.play(context, sound.uri)
        viewModelScope.launch {
            val result = runCatching {
                if (alarmId != null) {
                    val alarm = alarmRepository.observeAlarms().first()
                        .firstOrNull { it.id == alarmId }
                        ?: error("알람을 찾을 수 없습니다.")
                    alarmRepository.updateAlarm(
                        alarm.copy(soundType = sound.soundType, soundUri = sound.uri)
                    )
                } else {
                    soundRepository.setDefaultSound(sound)
                }
            }
            result.onFailure { error ->
                transientState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun openRecordSheet() {
        transientState.update {
            it.copy(
                recordSheetVisible = true,
                recordingPhase = RecordingPhase.Idle,
                elapsedSeconds = 0,
                errorMessage = null
            )
        }
    }

    fun closeRecordSheet() {
        timerJob?.cancel()
        timerJob = null
        soundRepository.discardRecording()
        transientState.update {
            it.copy(
                recordSheetVisible = false,
                recordingPhase = RecordingPhase.Idle,
                elapsedSeconds = 0
            )
        }
    }

    fun startRecording() {
        val result = soundRepository.startRecording()
        result.onSuccess {
            transientState.update {
                it.copy(recordingPhase = RecordingPhase.Recording, elapsedSeconds = 0, errorMessage = null)
            }
            startTimer()
        }.onFailure { error ->
            transientState.update { it.copy(errorMessage = error.message) }
        }
    }

    fun stopRecording() {
        timerJob?.cancel()
        timerJob = null
        val result = soundRepository.stopRecording()
        result.onSuccess {
            transientState.update { it.copy(recordingPhase = RecordingPhase.Recorded) }
        }.onFailure { error ->
            transientState.update {
                it.copy(recordingPhase = RecordingPhase.Idle, elapsedSeconds = 0, errorMessage = error.message)
            }
        }
    }

    fun retryRecording() {
        soundRepository.discardRecording()
        transientState.update {
            it.copy(recordingPhase = RecordingPhase.Idle, elapsedSeconds = 0, errorMessage = null)
        }
    }

    fun saveRecording() {
        viewModelScope.launch {
            val result = soundRepository.saveRecordedSound()
            result.onSuccess {
                transientState.update {
                    it.copy(
                        recordSheetVisible = false,
                        recordingPhase = RecordingPhase.Idle,
                        elapsedSeconds = 0
                    )
                }
            }.onFailure { error ->
                transientState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun importSound(uri: Uri) {
        viewModelScope.launch {
            val result = soundRepository.importSound(uri)
            result.onFailure { error ->
                transientState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    /** 기기 시스템 알람음을 카탈로그에 추가하고 곧바로 선택한다. */
    fun addSystemSound(uri: Uri) {
        viewModelScope.launch {
            soundRepository.addSystemSound(uri.toString())
                .onSuccess { sound -> selectSound(sound) }
                .onFailure { error ->
                    transientState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun deleteSound(sound: AlarmSound) {
        viewModelScope.launch {
            val result = soundRepository.deleteSound(sound)
            result.onFailure { error ->
                transientState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun consumeError() {
        transientState.update { it.copy(errorMessage = null) }
    }

    fun reportError(message: String) {
        transientState.update { it.copy(errorMessage = message) }
    }

    fun stopPreview() {
        SoundPreviewPlayer.stop()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                transientState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundRepository.discardRecording()
        SoundPreviewPlayer.stop()
    }

    companion object {
        const val ARG_ALARM_ID = "alarmId"
    }
}
