package com.wakepoint.app.domain.model

data class AlarmPermission(
    val id: String,
    val requesterId: String,
    val targetId: String,
    val status: PermissionStatus,
    val expiresAt: String? = null,
    val createdAt: String? = null
)

enum class PermissionStatus {
    None,
    Pending,
    Accepted,
    Rejected
}
