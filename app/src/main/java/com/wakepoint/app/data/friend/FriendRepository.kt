package com.wakepoint.app.data.friend

import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.PermissionStatus
import com.wakepoint.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

data class FriendSearchResult(
    val profile: UserProfile,
    val existingFriendStatus: FriendStatus?
)

data class AlarmPermissionRequest(
    val id: String,
    val requesterId: String,
    val targetId: String,
    val status: PermissionStatus,
    val requesterProfile: UserProfile?,
    val targetProfile: UserProfile?,
    val expiresAt: String?,
    val createdAt: String?
)

interface FriendRepository {
    fun observeFriends(): Flow<List<Friend>>
    fun observeAcceptedFriends(): Flow<List<Friend>>
    suspend fun refreshFriends()
    suspend fun fetchAlarmPermissionRequests(): List<AlarmPermissionRequest>
    suspend fun searchUsers(query: String): List<FriendSearchResult>
    suspend fun sendFriendRequest(friendUserId: String)
    suspend fun acceptFriendRequest(friendshipId: String)
    suspend fun rejectFriendRequest(friendshipId: String)
    suspend fun blockFriend(friendshipId: String)
    suspend fun deleteFriend(friendshipId: String)
    suspend fun requestAlarmPermission(targetUserId: String)
    suspend fun acceptAlarmPermission(permissionId: String)
    suspend fun rejectAlarmPermission(permissionId: String)
}
