package com.wakepoint.app.feature.lasttrain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.core.alarm.RepeatMode
import com.wakepoint.app.core.error.UserFacingError
import com.wakepoint.app.data.place.PlaceRepository
import com.wakepoint.app.data.timealarm.TimeAlarmRepository
import com.wakepoint.app.domain.model.Place
import com.wakepoint.app.domain.model.TimeAlarm
import com.wakepoint.app.feature.timealarm.TimeAlarmFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LastTrainUiState(
    val isLoading: Boolean = true,
    /** 집 고정 슬롯(4-3 판정). null 이면 등록 유도. */
    val homePlace: Place? = null,
    /** 조회 전 null. 조회 후 여유/임박/아슬·끊김·미지원 중 하나. */
    val result: LastTrainResult? = null,
    /** 알람 걸기 성공 토스트 문구(소비 후 null). */
    val alarmSetMessage: String? = null,
    val errorMessage: String? = null,
)

/**
 * v2 STEP4 막차 알람. 계산은 [LastTrainCalculator](현재 모의 구현)로, "알람 걸기"는
 * Phase 2 시간 알람(Room+AlarmManager)에 실제 연동된다.
 */
@HiltViewModel
class LastTrainViewModel @Inject constructor(
    placeRepository: PlaceRepository,
    private val timeAlarmRepository: TimeAlarmRepository,
    private val calculator: LastTrainCalculator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LastTrainUiState())
    val uiState: StateFlow<LastTrainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            placeRepository.observePlaces().collect { places ->
                _uiState.update {
                    it.copy(isLoading = false, homePlace = places.firstOrNull { p -> p.isHome })
                }
            }
        }
    }

    /** 동의 완료 후 호출. 모의 계산기라 즉시 결과가 나온다. */
    fun lookup() {
        _uiState.update { it.copy(result = calculator.lookup(ZonedDateTime.now())) }
    }

    /** 여유 단계 택시비 지연 조회(4-1 placeholder 탭). 모의 값으로 채운다. */
    fun loadTaxiFare() {
        _uiState.update { state ->
            val available = state.result as? LastTrainResult.Available ?: return@update state
            state.copy(result = available.copy(taxiFare = "약 12,000원"))
        }
    }

    /** 막차 데드라인 시각으로 일회성 시간 알람 등록(Phase 2 연동). */
    fun setDeadlineAlarm() {
        val available = _uiState.value.result as? LastTrainResult.Available ?: return
        saveOneShotAlarm(
            hour = available.deadlineHour,
            minute = available.deadlineMinute,
            label = "막차 ${available.stationName} ${TimeAlarmFormat.timeText(available.deadlineHour, available.deadlineMinute)}",
        )
    }

    /** 첫차 시각으로 일회성 시간 알람 등록(4-2). */
    fun setFirstTrainAlarm() {
        val ended = _uiState.value.result as? LastTrainResult.Ended ?: return
        saveOneShotAlarm(
            hour = ended.firstTrainHour,
            minute = ended.firstTrainMinute,
            label = "첫차 ${TimeAlarmFormat.timeText(ended.firstTrainHour, ended.firstTrainMinute)}",
        )
    }

    private fun saveOneShotAlarm(hour: Int, minute: Int, label: String) {
        viewModelScope.launch {
            val alarm = TimeAlarm(
                id = UUID.randomUUID().toString(),
                label = label,
                hour = hour,
                minute = minute,
                repeatMode = RepeatMode.NONE,
            )
            val result = runCatching { timeAlarmRepository.saveTimeAlarm(alarm) }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { current.copy(alarmSetMessage = "'$label' 알람을 걸었어요") },
                    onFailure = { error -> current.copy(errorMessage = UserFacingError.of(error)) },
                )
            }
        }
    }

    fun consumeAlarmSetMessage() {
        _uiState.update { it.copy(alarmSetMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
