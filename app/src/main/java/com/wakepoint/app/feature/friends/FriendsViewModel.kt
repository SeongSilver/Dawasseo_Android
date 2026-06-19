package com.wakepoint.app.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.friend.AlarmPermissionRequest
import com.wakepoint.app.data.friend.FriendRepository
import com.wakepoint.app.data.friend.FriendSearchResult
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.PermissionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendsUiState(
    val friends: List<Friend> = emptyList(),
    val alarmPermissionRequests: List<AlarmPermissionRequest> = emptyList(),
    val currentUserId: String = "",
    val searchQuery: String = "",
    val searchResults: List<FriendSearchResult> = emptyList(),
    val selectedSearchUserId: String? = null,
    val isRefreshing: Boolean = false,
    val isSearching: Boolean = false,
    val updatingFriendIds: Set<String> = emptySet(),
    val updatingPermissionIds: Set<String> = emptySet(),
    val requestingUserIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val searchMessage: String? = null
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
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val authRepository: AuthRepository
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
        refreshFriends()
    }

    fun refreshFriends() {
        viewModelScope.launch {
            transientState.update {
                it.copy(isRefreshing = true, errorMessage = null)
            }
            val result = runCatching {
                val session = authRepository.requireValidSession()
                friendRepository.refreshFriends()
                session.userId to friendRepository.fetchAlarmPermissionRequests()
            }
            transientState.update { current ->
                val resultValue = result.getOrNull()
                current.copy(
                    currentUserId = resultValue?.first.orEmpty().ifBlank { current.currentUserId },
                    alarmPermissionRequests = resultValue?.second ?: current.alarmPermissionRequests,
                    isRefreshing = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

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
                        searchMessage = "두 글자 이상 입력해주세요."
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
                            searchMessage = if (results.isEmpty()) "검색 결과가 없습니다." else null
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSearching = false,
                            searchMessage = error.message ?: "친구 검색에 실패했습니다."
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
                            searchMessage = "친구 요청을 보냈습니다."
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            requestingUserIds = requestingUserIds,
                            searchMessage = error.message ?: "친구 요청에 실패했습니다."
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
                            errorMessage = error.message ?: "알람 권한 요청에 실패했습니다."
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
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            if (result.isSuccess) {
                refreshFriends()
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
                            errorMessage = error.message ?: "알람 권한 요청 처리에 실패했습니다."
                        )
                    }
                )
            }
        }
    }
}

private const val MIN_SEARCH_QUERY_LENGTH = 2
