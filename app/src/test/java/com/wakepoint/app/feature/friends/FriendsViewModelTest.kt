package com.wakepoint.app.feature.friends

import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.auth.AuthOperationResult
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.auth.AuthSession
import com.wakepoint.app.data.friend.AlarmPermissionRequest
import com.wakepoint.app.data.friend.FriendRepository
import com.wakepoint.app.data.friend.FriendSearchResult
import com.wakepoint.app.data.location.KakaoLocalRepository
import com.wakepoint.app.data.location.PlaceSearchResult
import com.wakepoint.app.data.sound.SoundRepository
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.AlarmSound
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.testing.MainDispatcherRule
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * FriendsViewModel 의 "친구 추가"(sendFriendRequest)와 "알람 전송"(sendAlarm) 동작 검증.
 *
 * 저장소는 인메모리 Fake 로 대체하고, viewModelScope 는 [MainDispatcherRule] 로 제어한다.
 */
class FriendsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val currentUserId = "me"
    private val currentEmail = "me@example.com"

    // ---- Fakes ----

    private val authRepository = FakeAuthRepository(
        session = AuthSession(
            userId = currentUserId,
            email = currentEmail,
            nickname = "나",
            accessToken = "token",
            refreshToken = null,
            expiresAtEpochSeconds = Long.MAX_VALUE
        )
    )
    private val friendRepository = FakeFriendRepository()
    private val alarmRepository = FakeAlarmRepository()
    private val soundRepository = FakeSoundRepository()
    private val kakaoLocalRepository = KakaoLocalRepository()

    /**
     * ViewModel 생성 후 uiState 에 활성 구독자를 붙인다.
     * uiState 는 WhileSubscribed 라 구독자가 없으면 combine 이 실행되지 않아
     * transientState 변화가 반영되지 않는다.
     */
    private fun TestScope.createViewModel(): FriendsViewModel {
        val viewModel = FriendsViewModel(
            friendRepository = friendRepository,
            authRepository = authRepository,
            alarmRepository = alarmRepository,
            kakaoLocalRepository = kakaoLocalRepository,
            soundRepository = soundRepository
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        return viewModel
    }

    private fun acceptedFriend(): Friend = Friend(
        id = "friendship-1",
        userId = currentUserId,
        friendId = "friend-1",
        nickname = "친구",
        email = "friend@example.com",
        status = FriendStatus.Accepted
    )

    private fun place(): PlaceSearchResult = PlaceSearchResult(
        id = "place-1",
        name = "강남역",
        address = "서울 강남구 강남대로",
        lat = 37.4979,
        lng = 127.0276
    )

    // ---- 친구 추가 ----

    @Test
    fun `친구 요청 성공 시 안내 메시지를 보여주고 진행 상태를 해제한다`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sendFriendRequest("friend-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("친구 요청을 보냈습니다.", state.searchMessage)
        assertFalse(state.requestingUserIds.contains("friend-1"))
        assertEquals(listOf("friend-1"), friendRepository.sentFriendRequests)
    }

    @Test
    fun `친구 요청 실패 시 오류 메시지를 노출하고 진행 상태를 해제한다`() = runTest {
        friendRepository.sendFriendRequestError = IllegalStateException("이미 친구 관계입니다.")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sendFriendRequest("friend-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("이미 친구 관계입니다.", state.searchMessage)
        assertFalse(state.requestingUserIds.contains("friend-1"))
    }

    // ---- 알람 전송 ----

    @Test
    fun `목적지를 선택하지 않고 전송하면 안내만 하고 저장소를 호출하지 않는다`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.openSendAlarm(acceptedFriend())

        viewModel.sendAlarm(fallbackLabel = "알람")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("먼저 목적지를 검색해 선택해 주세요.", state.sendAlarmErrorMessage)
        assertFalse(state.sendAlarmSucceeded)
        assertTrue(alarmRepository.sentAlarms.isEmpty())
    }

    @Test
    fun `목적지 선택 후 전송하면 상대에게 보낼 알람이 저장소로 전달된다`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.openSendAlarm(acceptedFriend())
        viewModel.selectAddress(place())
        viewModel.updateSendAlarmLabel("회사")
        viewModel.updateSendAlarmRadius("1km")

        viewModel.sendAlarm(fallbackLabel = "알람")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.sendAlarmSucceeded)
        assertNull(state.sendAlarmErrorMessage)

        assertEquals(1, alarmRepository.sentAlarms.size)
        val sent = alarmRepository.sentAlarms.single()
        // 알람의 주인은 상대(friend-1), 발신자는 나여야 한다.
        assertEquals("friend-1", sent.ownerId)
        assertEquals(currentUserId, sent.createdBy)
        assertEquals("회사", sent.label)
        assertEquals(1.0, sent.radiusKm, 0.0001)
        assertEquals(37.4979, sent.targetLat, 0.0001)
        assertEquals(127.0276, sent.targetLng, 0.0001)
    }

    @Test
    fun `별칭을 비우고 전송하면 선택한 장소명이 별칭으로 사용된다`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.openSendAlarm(acceptedFriend())
        viewModel.selectAddress(place())
        // 별칭 입력 없음

        viewModel.sendAlarm(fallbackLabel = "폴백")
        advanceUntilIdle()

        val sent = alarmRepository.sentAlarms.single()
        assertEquals("강남역", sent.label)
    }

    @Test
    fun `전송 실패 시 오류 메시지를 노출하고 성공 플래그를 세우지 않는다`() = runTest {
        alarmRepository.sendError = IllegalStateException("네트워크 오류")
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.openSendAlarm(acceptedFriend())
        viewModel.selectAddress(place())

        viewModel.sendAlarm(fallbackLabel = "알람")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("네트워크 오류", state.sendAlarmErrorMessage)
        assertFalse(state.sendAlarmSucceeded)
    }
}

// ---------- Fakes ----------

private class FakeFriendRepository : FriendRepository {
    val friends = MutableStateFlow<List<Friend>>(emptyList())
    val sentFriendRequests = mutableListOf<String>()
    var sendFriendRequestError: Throwable? = null

    override fun observeFriends(): Flow<List<Friend>> = friends
    override fun observeAcceptedFriends(): Flow<List<Friend>> = friends
    override suspend fun refreshFriends() = Unit
    override suspend fun fetchAlarmPermissionRequests(): List<AlarmPermissionRequest> = emptyList()
    override suspend fun searchUsers(query: String): List<FriendSearchResult> = emptyList()

    override suspend fun sendFriendRequest(friendUserId: String) {
        sendFriendRequestError?.let { throw it }
        sentFriendRequests += friendUserId
    }

    override suspend fun acceptFriendRequest(friendshipId: String) = Unit
    override suspend fun rejectFriendRequest(friendshipId: String) = Unit
    override suspend fun blockFriend(friendshipId: String) = Unit
    override suspend fun deleteFriend(friendshipId: String) = Unit
    override suspend fun requestAlarmPermission(targetUserId: String) = Unit
    override suspend fun acceptAlarmPermission(permissionId: String) = Unit
    override suspend fun rejectAlarmPermission(permissionId: String) = Unit
}

private class FakeAlarmRepository : AlarmRepository {
    val sentAlarms = mutableListOf<Alarm>()
    var sendError: Throwable? = null

    override fun observeAlarms(): Flow<List<Alarm>> = flowOf(emptyList())
    override fun observeActiveAlarms(): Flow<List<Alarm>> = flowOf(emptyList())
    override suspend fun refreshAlarms() = Unit
    override suspend fun saveAlarm(alarm: Alarm) = Unit

    override suspend fun sendAlarmToFriend(alarm: Alarm) {
        sendError?.let { throw it }
        sentAlarms += alarm
    }

    override suspend fun updateAlarm(alarm: Alarm) = Unit
    override suspend fun setAlarmActive(alarmId: String, isActive: Boolean) = Unit
    override suspend fun setFavorite(alarmId: String, isFavorite: Boolean) = Unit
    override suspend fun deleteAlarm(alarmId: String) = Unit
    override suspend fun syncLocationTracking() = Unit
    override suspend fun handleTriggeredAlarm(alarmId: String) = Unit
}

private class FakeAuthRepository(private val session: AuthSession) : AuthRepository {
    override val authSession: Flow<AuthSession?> = MutableStateFlow(session)
    override suspend fun requireValidSession(): AuthSession = session

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthOperationResult> =
        Result.success(AuthOperationResult(sessionStarted = true))

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        nickname: String
    ): Result<AuthOperationResult> = Result.success(AuthOperationResult(sessionStarted = true))

    override fun kakaoOAuthUrl(): String = ""
    override suspend fun completeOAuthSignIn(callbackUri: String): Result<AuthOperationResult> =
        Result.success(AuthOperationResult(sessionStarted = true))

    override suspend fun sendPasswordReset(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun refreshSessionIfNeeded(): Result<AuthSession?> = Result.success(session)
    override suspend fun updateNickname(nickname: String): Result<Unit> = Result.success(Unit)
    override suspend fun uploadPushToken(token: String): Result<Unit> = Result.success(Unit)
    override suspend fun signOut() = Unit
}

private class FakeSoundRepository : SoundRepository {
    override fun observeSounds(): Flow<List<AlarmSound>> = flowOf(emptyList())
    override fun observeSelectedDefaultSound(): Flow<AlarmSound?> = flowOf(null)
    override suspend fun setDefaultSound(sound: AlarmSound) = Unit
    override suspend fun importSound(uri: Uri): Result<AlarmSound> =
        Result.failure(UnsupportedOperationException())

    override suspend fun addSystemSound(uri: String): Result<AlarmSound> =
        Result.failure(UnsupportedOperationException())

    override fun startRecording(): Result<Unit> = Result.success(Unit)
    override fun stopRecording(): Result<Unit> = Result.success(Unit)
    override suspend fun saveRecordedSound(): Result<AlarmSound> =
        Result.failure(UnsupportedOperationException())

    override fun discardRecording() = Unit
    override suspend fun deleteSound(sound: AlarmSound): Result<Unit> = Result.success(Unit)
}
