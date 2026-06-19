package com.wakepoint.app.data.friend.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.PermissionStatus

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "friend_id") val friendId: String,
    val nickname: String,
    val email: String,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String?,
    val status: String,
    @ColumnInfo(name = "permission_status") val permissionStatus: String,
    @ColumnInfo(name = "permission_expires_at") val permissionExpiresAt: String?,
    @ColumnInfo(name = "created_at") val createdAt: String?
)

fun FriendEntity.toDomain(): Friend = Friend(
    id = id,
    userId = userId,
    friendId = friendId,
    nickname = nickname,
    email = email,
    avatarUrl = avatarUrl,
    status = status.toFriendStatus(),
    permissionStatus = permissionStatus.toPermissionStatus(),
    permissionExpiresAt = permissionExpiresAt,
    createdAt = createdAt
)

fun Friend.toEntity(): FriendEntity = FriendEntity(
    id = id,
    userId = userId,
    friendId = friendId,
    nickname = nickname,
    email = email,
    avatarUrl = avatarUrl,
    status = status.name,
    permissionStatus = permissionStatus.name,
    permissionExpiresAt = permissionExpiresAt,
    createdAt = createdAt
)

private fun String.toFriendStatus(): FriendStatus {
    return FriendStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: FriendStatus.Pending
}

private fun String.toPermissionStatus(): PermissionStatus {
    return PermissionStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: PermissionStatus.None
}
