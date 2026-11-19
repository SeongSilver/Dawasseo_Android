package com.wakepoint.app.feature.friends

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.R
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.friend.AlarmPermissionRequest
import com.wakepoint.app.data.friend.FriendRepository
import com.wakepoint.app.data.friend.FriendSearchResult
import com.wakepoint.app.data.location.KakaoLocalRepository
import com.wakepoint.app.data.location.PlaceSearchResult
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.AlarmInvitation
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.InvitationStatus
import com.wakepoint.app.domain.model.PermissionStatus
import com.wakepoint.app.domain.model.SoundType
import com.wakepoint.app.feature.home.DEFAULT_ALARM_RADIUS_OPTION
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 화면에 보여줄 메시지. 리소스 ID 또는 서버/예외에서 온 텍스트 중 하나를 담는다. */
data class FriendsMessage(
    @StringRes val resId: Int? = null,
    val text: String? = null
) {
    companion object {
        fun of(@StringRes resId: Int) = FriendsMessage(resId = resId)
        fun of(text: String?, @StringRes fallbackResId: Int): FriendsMessage {
            return if (text.isNullOrBlank()) FriendsMessage(resId = fallbackResId) else FriendsMessage(text = text)
        }
    }
}

data class FriendsUiState(
    val friends: List<Friend> = emptyList(),
    val alarmPermissionRequests: List<AlarmPermissionRequest> = emptyList(),
    val alarmInvitations: List<AlarmInvitation> = emptyList(),
    val currentUserId: String = "",
    val searchQuery: String = "",
    val searchResults: List<FriendSearchResult> = emptyList(),
    val selectedSearchUserId: String? = null,
    val isRefreshing: Boolean = false,
    val isSearching: Boolean = false,
    val updatingFriendIds: Set<String> = emptySet(),
    val updatingPermissionIds: Set<String> = emptySet(),
    val updatingInvitationIds: Set<String> = emptySet(),
    val requestingUserIds: Set<String> = emptySet(),
    val errorMessage: FriendsMessage? = null,
    val searchMessage: FriendsMessage? = null,
    // 알람 보내기 시트
    val sendTarget: Friend? = null,
    val sendLabel: String = "",
    val sendRadiusOption: String = DEFAULT_ALARM_RADIUS_OPTION,
    val sendSearchQuery: String = "",
    val sendSearchResults: List<PlaceSearchResult> = emptyList(),
    val sendDestination: PlaceSearchResult? = null,
    val isSendSearching: Boolean = false,
    val isSendingInvitation: Boolean = false,
    val sendMessage: FriendsMessage? = null
) {
    val acceptedFriends: List<Friend> = friends.filter { it.status == FriendStatus.Accepted }
    val receivedFriendRequests: List<Friend> = friends.filter {
        it.status == FriendStatus.Pending && it.friendId == currentUserId
    }
    val sentFriendRequests: List<Friend> = friends.filter {
        it.status == FriendStatus.Pending && it.userId == currentUserId
    }
    val receivedAlarmPermissionRequests: List<AlarmPermissionRequest> = alarmPermissionRequests.filter {
        it.status == PermissionStatus.Pending && it.targetId == currentUserId
    }
    val sentAlarmPermissionRequests: List<AlarmPermissionRequest> = alarmPermissionRequests.filter {
        it.status == PermissionStatus.Pending && it.requesterId == currentUserId
    }
    val receivedInvitations: List<AlarmInvitation> = alarmInvitations.filter {
        it.status == InvitationStatus.Pending && it.targetId == currentUserId
    }
    val sentInvitations: List<AlarmInvitation> = alarmInvitations.filter {
        it.requesterId == currentUserId
    }
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val authRepository: AuthRepository,
    private val alarmRepository: AlarmRepository,
    private val kakaoLocalRepository: KakaoLocalRepository
) : ViewModel() {
    private val transientState = MutableStateFlow(FriendsUiState())

    val uiState: StateFlow<FriendsUiState> = combine(
        friendRepository.observeFriends(),
        transientState
    ) { friends, state ->
        state.copy(friends = friends)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FriendsUiState()
    )

    init {
        refreshAll()
    }

    /** 친구, 권한 요청, 알람 초대를 모두 다시 불러온다. 수동 새로고침과 주기 갱신이 함께 쓴다. */
    fun refreshAll() {
        viewModelScope.launch {
            transientState.update {
                it.copy(isRefreshing = true, errorMessage = null)
            }
            val result = runCatching {
                val session = authRepository.requireValidSession()
                friendRepository.refreshFriends()
                RefreshResult(
                    currentUserId = session.userId,
                    permissionRequests = friendRepository.fetchAlarmPermissionRequests(),
                    invitations = friendRepository.fetchAlarmInvitations()
                )
            }
            transientState.update { current ->
                val value = result.getOrNull()
                current.copy(
                    currentUserId = value?.currentUserId.orEmpty().ifBlank { current.currentUserId },
                    alarmPermissionRequests = value?.permissionRequests ?: current.alarmPermissionRequests,
                    alarmInvitations = value?.invitations ?: current.alarmInvitations,
                    isRefreshing = false,
                    errorMessage = result.exceptionOrNull()?.message?.let { FriendsMessage(text = it) }
                )
            }
        }
    }

    fun refreshFriends() = refreshAll()

    fun updateSearchQuery(value: String) {
        transientState.update {
            it.copy(
                searchQuery = value,
                selectedSearchUserId = null,
                searchMessage = null
            )
        }
    }

    fun searchUsers() {
        val query = transientState.value.searchQuery.trim()
        viewModelScope.launch {
            if (query.length < MIN_SEARCH_QUERY_LENGTH) {
                transientState.update {
                    it.copy(
                        searchResults = emptyList(),
                        searchMessage = FriendsMessage.of(R.string.friends_search_min_length)
                    )
                }
                return@launch
            }

            transientState.update {
                it.copy(
                    isSearching = true,
                    searchMessage = null,
                    errorMessage = null
                )
            }
            val result = runCatching {
                friendRepository.searchUsers(query)
            }
            transientState.update { current ->
                result.fold(
                    onSuccess = { results ->
                        current.copy(
                            searchResults = results,
                            selectedSearchUserId = null,
                            isSearching = false,
                            searchMessage = if (results.isEmpty()) {
                                FriendsMessage.of(R.string.friends_search_empty)
                            } else {
                                null
                            }
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSearching = false,
                            searchMessage = FriendsMessage.of(error.message, R.string.friends_search_failed)
                        )
                    }
                )
            }
        }
    }

    fun selectSearchResult(userId: String) {
        transientState.update {
            it.copy(selectedSearchUserId = userId)
        }
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    requestingUserIds = it.requestingUserIds + userId,
                    searchMessage = null,
                    errorMessage = null
                )
            }
            val result = runCatching {
                friendRepository.sendFriendRequest(userId)
                friendRepository.searchUsers(transientState.value.searchQuery)
            }
            transientState.update { current ->
                val requestingUserIds = current.requestingUserIds - userId
                result.fold(
                    onSuccess = { results ->
                        current.copy(
                            searchResults = results,
                            requestingUserIds = requestingUserIds,
                            selectedSearchUserId = null,
                            searchMessage = FriendsMessage.of(R.string.friends_request_sent)
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            requestingUserIds = requestingUserIds,
                            searchMessage = FriendsMessage.of(error.message, R.string.friends_request_failed)
                        )
                    }
                )
            }
        }
    }

    fun deleteFriend(friendshipId: String) {
        runFriendAction(friendshipId) {
            friendRepository.deleteFriend(friendshipId)
        }
    }

    fun acceptFriendRequest(friendshipId: String) {
        runFriendAction(friendshipId) {
            friendRepository.acceptFriendRequest(friendshipId)
        }
    }

    fun rejectFriendRequest(friendshipId: String) {
        runFriendAction(friendshipId) {
            friendRepository.rejectFriendRequest(friendshipId)
        }
    }

    fun blockFriend(friendshipId: String) {
        runFriendAction(friendshipId) {
            friendRepository.blockFriend(friendshipId)
        }
    }

    fun requestAlarmPermission(friendUserId: String) {
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    requestingUserIds = it.requestingUserIds + friendUserId,
                    errorMessage = null
                )
            }
            val result = runCatching {
                friendRepository.requestAlarmPermission(friendUserId)
                friendRepository.fetchAlarmPermissionRequests()
            }
            transientState.update { current ->
                val requestingUserIds = current.requestingUserIds - friendUserId
                result.fold(
                    onSuccess = { requests ->
                        current.copy(
                            alarmPermissionRequests = requests,
                            requestingUserIds = requestingUserIds
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            requestingUserIds = requestingUserIds,
                            errorMessage = FriendsMessage.of(error.message, R.string.friends_permission_request_failed)
                        )
                    }
                )
            }
        }
    }

    fun acceptAlarmPermission(permissionId: String) {
        runPermissionAction(permissionId) {
            friendRepository.acceptAlarmPermission(permissionId)
        }
    }

    fun rejectAlarmPermission(permissionId: String) {
        runPermissionAction(permissionId) {
            friendRepository.rejectAlarmPermission(permissionId)
        }
    }

    // ----- 알람 보내기 시트 -----

    fun openSendAlarm(friend: Friend) {
        transientState.update {
            it.copy(
                sendTarget = friend,
                sendLabel = "",
                sendRadiusOption = DEFAULT_ALARM_RADIUS_OPTION,
                sendSearchQuery = "",
                sendSearchResults = emptyList(),
                sendDestination = null,
                isSendSearching = false,
                isSendingInvitation = false,
                sendMessage = null
            )
        }
    }

    fun dismissSendAlarm() {
        transientState.update { it.copy(sendTarget = null) }
    }

    fun updateSendLabel(value: String) {
        transientState.update { it.copy(sendLabel = value) }
    }

    fun updateSendRadiusOption(value: String) {
        transientState.update { it.copy(sendRadiusOption = value) }
    }

    fun updateSendSearchQuery(value: String) {
        transientState.update { it.copy(sendSearchQuery = value, sendMessage = null) }
    }

    fun searchSendDestination() {
        val query = transientState.value.sendSearchQuery.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            transientState.update { it.copy(isSendSearching = true, sendMessage = null) }
            val result = kakaoLocalRepository.searchKeyword(query)
            transientState.update { current ->
                result.fold(
                    onSuccess = { places ->
                        current.copy(
                            isSendSearching = false,
                            sendSearchResults = places,
                            sendMessage = if (places.isEmpty()) {
                                FriendsMessage.of(R.string.friends_search_empty)
                            } else {
                                null
                            }
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSendSearching = false,
                            sendMessage = FriendsMessage.of(error.message, R.string.invite_search_failed)
                        )
                    }
                )
            }
        }
    }

    fun selectSendDestination(place: PlaceSearchResult) {
        transientState.update {
            it.copy(
                sendDestination = place,
                sendSearchResults = emptyList(),
                sendMessage = null
            )
        }
    }

    fun sendAlarmInvitation(fallbackLabel: String) {
        val state = transientState.value
        val target = state.sendTarget ?: return
        val destination = state.sendDestination
        if (destination == null) {
            transientState.update { it.copy(sendMessage = FriendsMessage.of(R.string.invite_select_destination)) }
            return
        }
        viewModelScope.launch {
            transientState.update { it.copy(isSendingInvitation = true, sendMessage = null) }
            val result = runCatching {
                friendRepository.sendAlarmInvitation(
                    targetUserId = target.otherUserId(state.currentUserId),
                    label = state.sendLabel.trim().ifBlank { fallbackLabel },
                    targetAddress = destination.address.ifBlank { destination.name },
                    lat = destination.lat,
                    lng = destination.lng,
                    radiusKm = radiusOptionToKm(state.sendRadiusOption)
                )
                friendRepository.fetchAlarmInvitations()
            }
            transientState.update { current ->
                result.fold(
                    onSuccess = { invitations ->
                        current.copy(
                            isSendingInvitation = false,
                            sendTarget = null,
                            alarmInvitations = invitations,
                            errorMessage = FriendsMessage.of(R.string.invite_sent)
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSendingInvitation = false,
                            sendMessage = FriendsMessage.of(error.message, R.string.invite_send_failed)
                        )
                    }
                )
            }
        }
    }

    // ----- 초대 처리 -----

    fun cancelAlarmInvitation(invitationId: String) {
        runInvitationAction(invitationId) {
            friendRepository.cancelAlarmInvitation(invitationId)
        }
    }

    fun rejectAlarmInvitation(invitationId: String) {
        runInvitationAction(invitationId) {
            friendRepository.rejectAlarmInvitation(invitationId)
        }
    }

    /**
     * 초대를 수락하고 내 기기에 알람을 만든다.
     * 알람의 소유자는 나, 생성자는 요청자다. 이후 위치 판단과 알람 실행은 내 기기에서만 일어난다.
     */
    fun acceptAlarmInvitation(invitation: AlarmInvitation) {
        runInvitationAction(invitation.id, successMessageResId = R.string.invite_accepted) {
            val session = authRepository.requireValidSession()
            friendRepository.acceptAlarmInvitation(invitation.id)
            alarmRepository.saveAlarm(
                Alarm(
                    id = UUID.randomUUID().toString(),
                    ownerId = session.userId,
                    createdBy = invitation.requesterId,
                    label = invitation.label,
                    targetLat = invitation.targetLat,
                    targetLng = invitation.targetLng,
                    targetAddress = invitation.targetAddress,
                    radiusKm = invitation.radiusKm,
                    isActive = true,
                    soundType = SoundType.Default,
                    soundUri = null
                )
            )
        }
    }

    private fun runInvitationAction(
        invitationId: String,
        @StringRes successMessageResId: Int? = null,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    updatingInvitationIds = it.updatingInvitationIds + invitationId,
                    errorMessage = null
                )
            }
            val result = runCatching {
                action()
                friendRepository.fetchAlarmInvitations()
            }
            transientState.update { current ->
                val updatingInvitationIds = current.updatingInvitationIds - invitationId
                result.fold(
                    onSuccess = { invitations ->
                        current.copy(
                            alarmInvitations = invitations,
                            updatingInvitationIds = updatingInvitationIds,
                            errorMessage = successMessageResId?.let { FriendsMessage.of(it) }
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            updatingInvitationIds = updatingInvitationIds,
                            errorMessage = FriendsMessage.of(error.message, R.string.invite_action_failed)
                        )
                    }
                )
            }
        }
    }

    private fun runFriendAction(
        friendshipId: String,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    updatingFriendIds = it.updatingFriendIds + friendshipId,
                    errorMessage = null
                )
            }
            val result = runCatching { action() }
            transientState.update { current ->
                val updatingFriendIds = current.updatingFriendIds - friendshipId
                current.copy(
                    updatingFriendIds = updatingFriendIds,
                    errorMessage = result.exceptionOrNull()?.message?.let { FriendsMessage(text = it) }
                )
            }
            if (result.isSuccess) {
                refreshAll()
            }
        }
    }

    private fun runPermissionAction(
        permissionId: String,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    updatingPermissionIds = it.updatingPermissionIds + permissionId,
                    errorMessage = null
                )
            }
            val result = runCatching {
                action()
                friendRepository.fetchAlarmPermissionRequests()
            }
            transientState.update { current ->
                val updatingPermissionIds = current.updatingPermissionIds - permissionId
                result.fold(
                    onSuccess = { requests ->
                        current.copy(
                            alarmPermissionRequests = requests,
                            updatingPermissionIds = updatingPermissionIds
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            updatingPermissionIds = updatingPermissionIds,
                            errorMessage = FriendsMessage.of(error.message, R.string.friends_permission_action_failed)
                        )
                    }
                )
            }
        }
    }

    private data class RefreshResult(
        val currentUserId: String,
        val permissionRequests: List<AlarmPermissionRequest>,
        val invitations: List<AlarmInvitation>
    )
}

internal fun Friend.otherUserId(currentUserId: String): String {
    return if (friendId == currentUserId) userId else friendId
}

private const val MIN_SEARCH_QUERY_LENGTH = 2
