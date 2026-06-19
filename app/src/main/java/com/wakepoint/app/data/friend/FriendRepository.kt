package com.wakepoint.app.data.friend

import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

data class FriendSearchResult(
    val profile: UserProfile,
    val existingFriendStatus: FriendStatus?
)

interface FriendRepository {
    fun observeFriends(): Flow<List<Friend>>
    fun observeAcceptedFriends(): Flow<List<Friend>>
    suspend fun refreshFriends()
    suspend fun searchUsers(query: String): List<FriendSearchResult>
    suspend fun sendFriendRequest(friendUserId: String)
    suspend fun acceptFriendRequest(friendshipId: String)
    suspend fun rejectFriendRequest(friendshipId: String)
    suspend fun blockFriend(friendshipId: String)
    suspend fun deleteFriend(friendshipId: String)
}
