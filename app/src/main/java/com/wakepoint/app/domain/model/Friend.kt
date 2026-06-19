package com.wakepoint.app.domain.model

data class Friend(
    val id: String,
    val userId: String,
    val friendId: String,
    val nickname: String,
    val email: String,
    val avatarUrl: String? = null,
    val status: FriendStatus = FriendStatus.Accepted,
    val permissionStatus: PermissionStatus = PermissionStatus.Pending,
    val createdAt: String? = null
)

enum class FriendStatus {
    Pending,
    Accepted,
    Rejected,
    Blocked
}
