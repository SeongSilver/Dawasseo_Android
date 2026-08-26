package com.wakepoint.app.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.friend.AlarmPermissionRequest
import com.wakepoint.app.data.friend.FriendRepository
import com.wakepoint.app.data.friend.FriendSearchResult
import com.wakepoint.app.data.location.KakaoLocalRepository
import com.wakepoint.app.data.location.PlaceSearchResult
import com.wakepoint.app.data.sound.SoundRepository
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.PermissionStatus
import com.wakepoint.app.domain.model.SoundKind
import com.wakepoint.app.domain.model.SoundType
import com.wakepoint.app.core.error.UserFacingError
import com.wakepoint.app.core.location.radiusOptionToKm
import com.wakepoint.app.feature.home.DEFAULT_ALARM_RADIUS_OPTION
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendsUiState(
    /** 저장소 첫 방출 전 = true. "로딩 중"과 "정말 친구가 없음"을 구분해 빈 화면 깜빡임을 막는다. */
    val isLoading: Boolean = true,
    val friends: List<Friend> = emptyList(),
    val alarmPermissionRequests: List<AlarmPermissionRequest> = emptyList(),
    val currentUserId: String = "",
    val currentUserEmail: String = "",
    val searchQuery: String = "",
    val searchResults: List<FriendSearchResult> = emptyList(),
    val selectedSearchUserId: String? = null,
    val isRefreshing: Boolean = false,
    val isSearching: Boolean = false,
    val updatingFriendIds: Set<String> = emptySet(),
    val updatingPermissionIds: Set<String> = emptySet(),
    val requestingUserIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val searchMessage: String? = null,
    val sendAlarmFriend: Friend? = null,
    val sendAlarmLabel: String = "",
    val sendAlarmRadius: String = DEFAULT_ALARM_RADIUS_OPTION,
    val sendAlarmSoundEnabled: Boolean = true,
    val sendAlarmVibrate: Boolean = true,
    val addressQuery: String = "",
    val addressResults: List<PlaceSearchResult> = emptyList(),
    val isSearchingAddress: Boolean = false,
    val addressMessage: String? = null,
    val selectedTargetLat: Double? = null,
    val selectedTargetLng: Double? = null,
    val selectedTargetAddress: String = "",
    val isSendingAlarm: Boolean = false,
    val sendAlarmErrorMessage: String? = null,
    val sendAlarmSucceeded: Boolean = false
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
    private val authRepository: AuthRepository,
    private val alarmRepository: AlarmRepository,
    private val kakaoLocalRepository: KakaoLocalRepository,
    private val soundRepository: SoundRepository
) : ViewModel() {
    private val transientState = MutableStateFlow(FriendsUiState())

    val uiState: StateFlow<FriendsUiState> = combine(
        friendRepository.observeFriends(),
        transientState
    ) { friends, state ->
        state.copy(isLoading = false, friends = friends)
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
                Triple(
                    session.userId,
                    session.email,
                    friendRepository.fetchAlarmPermissionRequests()
                )
            }
            transientState.update { current ->
                val resultValue = result.getOrNull()
                current.copy(
                    currentUserId = resultValue?.first.orEmpty().ifBlank { current.currentUserId },
                    currentUserEmail = resultValue?.second.orEmpty().ifBlank { current.currentUserEmail },
                    alarmPermissionRequests = resultValue?.third ?: current.alarmPermissionRequests,
                    isRefreshing = false,
                    errorMessage = result.exceptionOrNull()?.let { UserFacingError.of(it) }
                )
            }
        }
    }

    /** 오류 팝업을 닫을 때 메시지를 소비한다. */
    fun clearError() {
        transientState.update { it.copy(errorMessage = null) }
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
                            errorMessage = UserFacingError.of(error)
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

    fun openSendAlarm(friend: Friend) {
        transientState.update {
            it.copy(
                sendAlarmFriend = friend,
                sendAlarmLabel = "",
                sendAlarmRadius = DEFAULT_ALARM_RADIUS_OPTION,
                sendAlarmSoundEnabled = true,
                sendAlarmVibrate = true,
                addressQuery = "",
                addressResults = emptyList(),
                isSearchingAddress = false,
                addressMessage = null,
                selectedTargetLat = null,
                selectedTargetLng = null,
                selectedTargetAddress = "",
                isSendingAlarm = false,
                sendAlarmErrorMessage = null,
                sendAlarmSucceeded = false
            )
        }
    }

    fun dismissSendAlarm() {
        transientState.update { it.copy(sendAlarmFriend = null) }
    }

    fun updateSendAlarmLabel(value: String) {
        transientState.update { it.copy(sendAlarmLabel = value, sendAlarmErrorMessage = null) }
    }

    fun updateSendAlarmRadius(value: String) {
        transientState.update { it.copy(sendAlarmRadius = value) }
    }

    fun updateSendAlarmSoundEnabled(value: Boolean) {
        transientState.update { it.copy(sendAlarmSoundEnabled = value) }
    }

    fun updateSendAlarmVibrate(value: Boolean) {
        transientState.update { it.copy(sendAlarmVibrate = value) }
    }

    fun updateAddressQuery(value: String) {
        transientState.update { it.copy(addressQuery = value, addressMessage = null) }
    }

    fun searchAddress() {
        val query = transientState.value.addressQuery.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            transientState.update { it.copy(isSearchingAddress = true, addressMessage = null) }
            val result = kakaoLocalRepository.searchKeyword(query)
            transientState.update { current ->
                result.fold(
                    onSuccess = { places ->
                        current.copy(
                            isSearchingAddress = false,
                            addressResults = places,
                            addressMessage = if (places.isEmpty()) "검색 결과가 없습니다." else null
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSearchingAddress = false,
                            addressMessage = error.message ?: "장소 검색에 실패했습니다."
                        )
                    }
                )
            }
        }
    }

    fun selectAddress(place: PlaceSearchResult) {
        transientState.update {
            it.copy(
                selectedTargetLat = place.lat,
                selectedTargetLng = place.lng,
                selectedTargetAddress = place.name.ifBlank { place.address }.ifBlank { "선택한 위치" },
                addressResults = emptyList(),
                addressQuery = "",
                sendAlarmErrorMessage = null
            )
        }
    }

    fun sendAlarm(fallbackLabel: String) {
        val state = transientState.value
        val friend = state.sendAlarmFriend ?: return
        val lat = state.selectedTargetLat
        val lng = state.selectedTargetLng
        if (lat == null || lng == null) {
            transientState.update { it.copy(sendAlarmErrorMessage = "먼저 목적지를 검색해 선택해 주세요.") }
            return
        }
        // 별칭 미입력 시 선택한 위치의 건물/도로명 주소를 별칭으로 사용한다.
        val label = state.sendAlarmLabel.trim()
            .ifBlank { state.selectedTargetAddress.trim() }
            .ifBlank { fallbackLabel }
        viewModelScope.launch {
            transientState.update {
                it.copy(
                    isSendingAlarm = true,
                    sendAlarmErrorMessage = null,
                    sendAlarmSucceeded = false
                )
            }
            // 상대 기기에서도 재생 가능한 번들(res/raw) 사운드만 전파한다.
            // 녹음/가져온 사운드는 발신자 기기의 로컬 파일 경로라 상대 기기에서 재생할 수 없으므로
            // 이 경우 null로 두어 상대 기기의 시스템 기본 알람음으로 폴백되게 한다.
            val bundledSoundUri = soundRepository.observeSelectedDefaultSound().first()
                ?.takeIf { it.kind == SoundKind.Bundled }
                ?.uri
            val result = runCatching {
                val session = authRepository.requireValidSession()
                alarmRepository.sendAlarmToFriend(
                    Alarm(
                        id = UUID.randomUUID().toString(),
                        ownerId = friend.otherUserId(session.userId),
                        createdBy = session.userId,
                        label = label,
                        targetLat = lat,
                        targetLng = lng,
                        targetAddress = state.selectedTargetAddress,
                        radiusKm = state.sendAlarmRadius.toRadiusKm(),
                        isActive = true,
                        soundType = SoundType.Default,
                        soundUri = bundledSoundUri,
                        soundEnabled = state.sendAlarmSoundEnabled,
                        vibrate = state.sendAlarmVibrate
                    )
                )
            }
            transientState.update { current ->
                result.fold(
                    onSuccess = {
                        current.copy(isSendingAlarm = false, sendAlarmSucceeded = true)
                    },
                    onFailure = { error ->
                        current.copy(
                            isSendingAlarm = false,
                            sendAlarmErrorMessage = error.message ?: "알람 전송에 실패했습니다."
                        )
                    }
                )
            }
        }
    }

    fun consumeSendAlarmSuccess() {
        transientState.update { it.copy(sendAlarmFriend = null, sendAlarmSucceeded = false) }
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
                    errorMessage = result.exceptionOrNull()?.let { UserFacingError.of(it) }
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
                            errorMessage = UserFacingError.of(error)
                        )
                    }
                )
            }
        }
    }
}

private const val MIN_SEARCH_QUERY_LENGTH = 2

private fun Friend.otherUserId(currentUserId: String): String {
    return if (friendId == currentUserId) userId else friendId
}

private fun String.toRadiusKm(): Double = radiusOptionToKm(this)
