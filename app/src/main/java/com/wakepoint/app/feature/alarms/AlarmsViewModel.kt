package com.wakepoint.app.feature.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.core.error.UserFacingError
import com.wakepoint.app.core.location.radiusOptionToKm
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.sound.SoundRepository
import com.wakepoint.app.domain.model.Alarm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlarmsUiState(
    val alarms: List<Alarm> = emptyList(),
    val isUpdating: Boolean = false,
    val updatingAlarmIds: Set<String> = emptySet(),
    val soundTitles: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
) {
    val favoriteAlarms: List<Alarm> = alarms.filter { it.isFavorite }
    val activeAlarms: List<Alarm> = alarms.filter { it.isActive && !it.isFavorite }
    val inactiveAlarms: List<Alarm> = alarms.filter { !it.isActive && !it.isFavorite }
}

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    soundRepository: SoundRepository
) : ViewModel() {
    private val transientState = MutableStateFlow(
        AlarmsUiState(
            isUpdating = false,
            errorMessage = null
        )
    )

    val uiState: StateFlow<AlarmsUiState> = combine(
        alarmRepository.observeAlarms(),
        soundRepository.observeSounds(),
        transientState
    ) { alarms, sounds, state ->
        state.copy(
            alarms = alarms,
            soundTitles = sounds.associate { it.uri to it.title }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlarmsUiState()
    )

    init {
        refreshAlarms()
    }

    /** 서버의 최신 알람(친구가 보낸 알람 포함)을 로컬에 반영한다. */
    fun refreshAlarms() {
        viewModelScope.launch {
            val result = runCatching { alarmRepository.refreshAlarms() }
            result.exceptionOrNull()?.let { error ->
                transientState.update { it.copy(errorMessage = UserFacingError.of(error)) }
            }
        }
    }

    /** 오류 팝업을 닫을 때 메시지를 소비한다. */
    fun clearError() {
        transientState.update { it.copy(errorMessage = null) }
    }

    fun setAlarmActive(alarmId: String, isActive: Boolean) {
        viewModelScope.launch {
            transientState.update { state ->
                state.copy(
                    isUpdating = true,
                    updatingAlarmIds = state.updatingAlarmIds + alarmId,
                    errorMessage = null
                )
            }
            val result = runCatching {
                alarmRepository.setAlarmActive(alarmId, isActive)
            }
            transientState.update { current ->
                val updatingAlarmIds = current.updatingAlarmIds - alarmId
                current.copy(
                    isUpdating = updatingAlarmIds.isNotEmpty(),
                    updatingAlarmIds = updatingAlarmIds,
                    errorMessage = result.exceptionOrNull()?.let { UserFacingError.of(it) }
                )
            }
        }
    }

    fun setFavorite(alarmId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            transientState.update { state ->
                state.copy(
                    updatingAlarmIds = state.updatingAlarmIds + alarmId,
                    errorMessage = null
                )
            }
            val result = runCatching {
                alarmRepository.setFavorite(alarmId, isFavorite)
            }
            transientState.update { current ->
                val updatingAlarmIds = current.updatingAlarmIds - alarmId
                current.copy(
                    isUpdating = updatingAlarmIds.isNotEmpty(),
                    updatingAlarmIds = updatingAlarmIds,
                    errorMessage = result.exceptionOrNull()?.let { UserFacingError.of(it) }
                )
            }
        }
    }

    fun updateAlarmSettings(
        alarm: Alarm,
        label: String,
        radiusOption: String,
        vibrate: Boolean
    ) {
        viewModelScope.launch {
            transientState.update { state ->
                state.copy(
                    isUpdating = true,
                    updatingAlarmIds = state.updatingAlarmIds + alarm.id,
                    errorMessage = null
                )
            }
            val updatedAlarm = alarm.copy(
                label = label.trim().ifBlank { alarm.label },
                radiusKm = radiusOption.toRadiusKm(),
                vibrate = vibrate
            )
            val result = runCatching {
                alarmRepository.updateAlarm(updatedAlarm)
            }
            transientState.update { current ->
                val updatingAlarmIds = current.updatingAlarmIds - alarm.id
                current.copy(
                    isUpdating = updatingAlarmIds.isNotEmpty(),
                    updatingAlarmIds = updatingAlarmIds,
                    errorMessage = result.exceptionOrNull()?.let { UserFacingError.of(it) }
                )
            }
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            transientState.update { state ->
                state.copy(
                    isUpdating = true,
                    updatingAlarmIds = state.updatingAlarmIds + alarmId,
                    errorMessage = null
                )
            }
            val result = runCatching {
                alarmRepository.deleteAlarm(alarmId)
            }
            transientState.update { current ->
                val updatingAlarmIds = current.updatingAlarmIds - alarmId
                current.copy(
                    isUpdating = updatingAlarmIds.isNotEmpty(),
                    updatingAlarmIds = updatingAlarmIds,
                    errorMessage = result.exceptionOrNull()?.let { UserFacingError.of(it) }
                )
            }
        }
    }
}

private fun String.toRadiusKm(): Double = radiusOptionToKm(this)
