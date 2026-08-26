package com.wakepoint.app.feature.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.core.alarm.TimeAlarmScheduler
import com.wakepoint.app.core.error.UserFacingError
import com.wakepoint.app.core.location.radiusOptionToKm
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.friend.FriendRepository
import com.wakepoint.app.data.sound.SoundRepository
import com.wakepoint.app.data.timealarm.TimeAlarmRepository
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.TimeAlarm
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
    /** 저장소 첫 방출 전 = true. "로딩 중"과 "정말 알람이 없음"을 구분해 빈 화면 깜빡임을 막는다. */
    val isLoading: Boolean = true,
    val alarms: List<Alarm> = emptyList(),
    /** v2 STEP1 시간 알람(로컬 전용) — 위치 알람과 같은 목록에 타입 배지로 함께 표시. */
    val timeAlarms: List<TimeAlarm> = emptyList(),
    /** 정확 알람(SCHEDULE_EXACT_ALARM) 허용 여부. false 면 시간 알람에 "정확도 제한" 배지. */
    val exactAlarmAllowed: Boolean = true,
    /** 수신 알람 감지용(3-7). 세션 미로딩 동안은 빈 값(감지 로직이 오탐을 막는다). */
    val currentUserId: String = "",
    /** 친구 userId → 닉네임(수신 다이얼로그 발신자 표기). */
    val friendNames: Map<String, String> = emptyMap(),
    val isUpdating: Boolean = false,
    val updatingAlarmIds: Set<String> = emptySet(),
    val soundTitles: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
) {
    val favoriteAlarms: List<Alarm> = alarms.filter { it.isFavorite }
    val activeAlarms: List<Alarm> = alarms.filter { it.isActive && !it.isFavorite }
    val inactiveAlarms: List<Alarm> = alarms.filter { !it.isActive && !it.isFavorite }
    val activeTimeAlarms: List<TimeAlarm> = timeAlarms.filter { it.enabled }
    val inactiveTimeAlarms: List<TimeAlarm> = timeAlarms.filter { !it.enabled }
    val isEmpty: Boolean = alarms.isEmpty() && timeAlarms.isEmpty()
}

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val timeAlarmRepository: TimeAlarmRepository,
    private val timeAlarmScheduler: TimeAlarmScheduler,
    private val authRepository: AuthRepository,
    friendRepository: FriendRepository,
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
        timeAlarmRepository.observeTimeAlarms(),
        soundRepository.observeSounds(),
        transientState
    ) { alarms, timeAlarms, sounds, state ->
        state.copy(
            isLoading = false,
            alarms = alarms,
            timeAlarms = timeAlarms,
            soundTitles = sounds.associate { it.uri to it.title }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlarmsUiState()
    )

    init {
        refreshAlarms()
        refreshExactAlarmState()
        viewModelScope.launch {
            val userId = runCatching { authRepository.requireValidSession().userId }.getOrNull()
            if (!userId.isNullOrBlank()) {
                transientState.update { it.copy(currentUserId = userId) }
            }
        }
        viewModelScope.launch {
            friendRepository.observeFriends().collect { friends ->
                val names = buildMap {
                    friends.forEach { friend ->
                        put(friend.userId, friend.nickname)
                        put(friend.friendId, friend.nickname)
                    }
                }
                transientState.update { it.copy(friendNames = names) }
            }
        }
    }

    /**
     * 정확 알람 허용 여부를 다시 읽는다. 설정 화면에 다녀온 뒤(onResume) 갱신해야
     * "정확도 제한" 배지가 실제 상태를 따라간다.
     */
    fun refreshExactAlarmState() {
        transientState.update { it.copy(exactAlarmAllowed = timeAlarmScheduler.canScheduleExact()) }
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
        vibrate: Boolean,
        repeating: Boolean
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
                vibrate = vibrate,
                repeating = repeating
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

    /** 선택 모드에서 고른 여러 알람을 한 번에 삭제한다. 하나라도 실패하면 사용자 오류를 노출한다. */
    fun deleteAlarms(alarmIds: Set<String>) {
        if (alarmIds.isEmpty()) return
        viewModelScope.launch {
            transientState.update { state ->
                state.copy(
                    isUpdating = true,
                    updatingAlarmIds = state.updatingAlarmIds + alarmIds,
                    errorMessage = null
                )
            }
            val result = runCatching {
                alarmIds.forEach { alarmRepository.deleteAlarm(it) }
            }
            transientState.update { current ->
                val updatingAlarmIds = current.updatingAlarmIds - alarmIds
                current.copy(
                    isUpdating = updatingAlarmIds.isNotEmpty(),
                    updatingAlarmIds = updatingAlarmIds,
                    errorMessage = result.exceptionOrNull()?.let { UserFacingError.of(it) }
                )
            }
        }
    }
    // --- 시간 알람(v2 STEP1) — 저장/토글/삭제 시 리포지토리가 AlarmManager (재)예약까지 처리한다. ---

    fun saveTimeAlarm(alarm: TimeAlarm) {
        viewModelScope.launch {
            transientState.update { it.copy(updatingAlarmIds = it.updatingAlarmIds + alarm.id) }
            val result = runCatching { timeAlarmRepository.saveTimeAlarm(alarm) }
            finishTimeAlarmUpdate(alarm.id, result.exceptionOrNull())
        }
    }

    fun setTimeAlarmEnabled(alarmId: String, enabled: Boolean) {
        viewModelScope.launch {
            transientState.update { it.copy(updatingAlarmIds = it.updatingAlarmIds + alarmId) }
            val result = runCatching { timeAlarmRepository.setEnabled(alarmId, enabled) }
            finishTimeAlarmUpdate(alarmId, result.exceptionOrNull())
        }
    }

    fun deleteTimeAlarm(alarmId: String) {
        viewModelScope.launch {
            transientState.update { it.copy(updatingAlarmIds = it.updatingAlarmIds + alarmId) }
            val result = runCatching { timeAlarmRepository.deleteTimeAlarm(alarmId) }
            finishTimeAlarmUpdate(alarmId, result.exceptionOrNull())
        }
    }

    private fun finishTimeAlarmUpdate(alarmId: String, error: Throwable?) {
        transientState.update { current ->
            val updatingAlarmIds = current.updatingAlarmIds - alarmId
            current.copy(
                isUpdating = updatingAlarmIds.isNotEmpty(),
                updatingAlarmIds = updatingAlarmIds,
                errorMessage = error?.let { UserFacingError.of(it) }
            )
        }
    }
}

private fun String.toRadiusKm(): Double = radiusOptionToKm(this)
