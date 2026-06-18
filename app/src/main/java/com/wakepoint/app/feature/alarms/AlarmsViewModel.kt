package com.wakepoint.app.feature.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.data.alarm.AlarmRepository
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
    val errorMessage: String? = null
) {
    val activeAlarms: List<Alarm> = alarms.filter { it.isActive }
    val inactiveAlarms: List<Alarm> = alarms.filterNot { it.isActive }
}

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {
    private val transientState = MutableStateFlow(
        AlarmsUiState(
            isUpdating = false,
            errorMessage = null
        )
    )

    val uiState: StateFlow<AlarmsUiState> = combine(
        alarmRepository.observeAlarms(),
        transientState
    ) { alarms, state ->
        state.copy(alarms = alarms)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlarmsUiState()
    )

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
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun updateAlarmSettings(
        alarm: Alarm,
        label: String,
        radiusOption: String
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
                radiusKm = radiusOption.toRadiusKm()
            )
            val result = runCatching {
                alarmRepository.updateAlarm(updatedAlarm)
            }
            transientState.update { current ->
                val updatingAlarmIds = current.updatingAlarmIds - alarm.id
                current.copy(
                    isUpdating = updatingAlarmIds.isNotEmpty(),
                    updatingAlarmIds = updatingAlarmIds,
                    errorMessage = result.exceptionOrNull()?.message
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
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}

private fun String.toRadiusKm(): Double {
    val radiusKm = when {
        endsWith("km") -> removeSuffix("km").toDoubleOrNull() ?: DEFAULT_RADIUS_KM
        endsWith("m") -> removeSuffix("m").toDoubleOrNull()?.div(1000.0) ?: DEFAULT_RADIUS_KM
        else -> DEFAULT_RADIUS_KM
    }
    return radiusKm.coerceIn(MIN_RADIUS_KM, MAX_RADIUS_KM)
}

private const val DEFAULT_RADIUS_KM = 0.5
private const val MIN_RADIUS_KM = 0.01
private const val MAX_RADIUS_KM = 50.0
