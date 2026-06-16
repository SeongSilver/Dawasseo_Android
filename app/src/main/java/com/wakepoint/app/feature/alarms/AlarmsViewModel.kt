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
            transientState.update { it.copy(isUpdating = true, errorMessage = null) }
            val result = runCatching {
                alarmRepository.setAlarmActive(alarmId, isActive)
            }
            transientState.update { current ->
                current.copy(
                    isUpdating = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            transientState.update { it.copy(isUpdating = true, errorMessage = null) }
            val result = runCatching {
                alarmRepository.deleteAlarm(alarmId)
            }
            transientState.update { current ->
                current.copy(
                    isUpdating = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
