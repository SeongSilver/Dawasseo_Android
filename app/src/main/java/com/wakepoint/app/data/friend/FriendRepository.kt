package com.wakepoint.app.data.friend

import com.wakepoint.app.domain.model.Friend
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun observeFriends(): Flow<List<Friend>>
    fun observeAcceptedFriends(): Flow<List<Friend>>
    suspend fun refreshFriends()
    suspend fun sendFriendRequest(friendUserId: String)
    suspend fun acceptFriendRequest(friendshipId: String)
    suspend fun rejectFriendRequest(friendshipId: String)
    suspend fun blockFriend(friendshipId: String)
    suspend fun deleteFriend(friendshipId: String)
}
