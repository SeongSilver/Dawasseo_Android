package com.wakepoint.app.domain.model

data class AlarmPermission(
    val id: String,
    val requesterId: String,
    val targetId: String,
    val status: PermissionStatus
)

enum class PermissionStatus {
    Pending,
    Accepted,
    Rejected
}
