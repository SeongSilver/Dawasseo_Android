package com.wakepoint.app.domain.model

data class Friend(
    val id: String,
    val userId: String,
    val friendId: String,
    val nickname: String,
    val email: String,
    val permissionStatus: PermissionStatus
)
