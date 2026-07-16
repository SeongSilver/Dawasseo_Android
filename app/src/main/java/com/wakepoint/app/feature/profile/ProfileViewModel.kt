package com.wakepoint.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.friend.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val nickname: String = "",
    val email: String = "",
    val wakeupCount: Int = 0,
    val activeAlarmCount: Int = 0,
    val friendCount: Int = 0,
    val isSavingNickname: Boolean = false,
    val nicknameErrorMessage: String? = null
)

private data class ProfileTransientState(
    val isSavingNickname: Boolean = false,
    val nicknameErrorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    alarmRepository: AlarmRepository,
    friendRepository: FriendRepository,
    userPreferences: UserPreferencesDataStore
) : ViewModel() {

    private val transientState = MutableStateFlow(ProfileTransientState())

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.authSession,
        alarmRepository.observeActiveAlarms(),
        friendRepository.observeAcceptedFriends(),
        userPreferences.wakeupCount,
        transientState
    ) { session, activeAlarms, friends, wakeups, transient ->
        ProfileUiState(
            nickname = session?.nickname?.ifBlank { session.email.substringBefore("@") }.orEmpty(),
            email = session?.email.orEmpty(),
            wakeupCount = wakeups,
            activeAlarmCount = activeAlarms.size,
            friendCount = friends.size,
            isSavingNickname = transient.isSavingNickname,
            nicknameErrorMessage = transient.nicknameErrorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    fun updateNickname(nickname: String) {
        viewModelScope.launch {
            transientState.update { it.copy(isSavingNickname = true, nicknameErrorMessage = null) }
            val result = authRepository.updateNickname(nickname)
            transientState.update {
                it.copy(
                    isSavingNickname = false,
                    nicknameErrorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun consumeNicknameError() {
        transientState.update { it.copy(nicknameErrorMessage = null) }
    }
}
