package com.wakepoint.app.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.core.error.UserFacingError
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.friend.FriendRepository
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 보낸 알람 한 건(v2 STEP3 3-6).
 * [pending] true = 아직 서버에 못 올라가 재전송 대기(오프라인 등), false = 서버 전송 완료.
 * 그 이후 상태(상대의 끔/도착)는 명세대로 발신자에게 비가시.
 */
data class SentAlarmItem(
    val alarm: Alarm,
    val recipientName: String,
    val pending: Boolean,
)

data class SentAlarmsUiState(
    val isLoading: Boolean = true,
    val items: List<SentAlarmItem> = emptyList(),
    val cancelingIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
)

@HiltViewModel
class SentAlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val friendRepository: FriendRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SentAlarmsUiState())
    val uiState: StateFlow<SentAlarmsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val result = runCatching {
                val friends = friendRepository.observeFriends().first()
                val pending = alarmRepository.pendingSentAlarms()
                    .map { SentAlarmItem(it, it.recipientName(friends), pending = true) }
                val sent = alarmRepository.fetchSentAlarms()
                    .map { SentAlarmItem(it, it.recipientName(friends), pending = false) }
                // 전송 대기분을 위로 — 재시도 대상이 먼저 보이게.
                pending + sent
            }
            _uiState.update { current ->
                result.fold(
                    onSuccess = { items -> current.copy(isLoading = false, items = items) },
                    onFailure = { error ->
                        current.copy(isLoading = false, errorMessage = UserFacingError.of(error))
                    },
                )
            }
        }
    }

    fun cancel(alarmId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cancelingIds = it.cancelingIds + alarmId) }
            val result = runCatching { alarmRepository.cancelSentAlarm(alarmId) }
            _uiState.update { current ->
                current.copy(
                    cancelingIds = current.cancelingIds - alarmId,
                    items = if (result.isSuccess) {
                        current.items.filterNot { it.alarm.id == alarmId }
                    } else {
                        current.items
                    },
                    errorMessage = result.exceptionOrNull()?.let { UserFacingError.of(it) },
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

/** 수신자(owner) id 를 친구 목록에서 닉네임으로 변환. 못 찾으면 "친구". */
private fun Alarm.recipientName(friends: List<Friend>): String =
    friends.firstOrNull { it.userId == ownerId || it.friendId == ownerId }?.nickname ?: "친구"
