package com.wakepoint.app.domain.model

data class Alarm(
    val id: String,
    val ownerId: String,
    val createdBy: String,
    val label: String,
    val targetLat: Double,
    val targetLng: Double,
    val targetAddress: String,
    val radiusKm: Double,
    val isActive: Boolean,
    val triggeredAt: String?,
    val soundType: SoundType,
    val soundUri: String?
)

enum class SoundType {
    Default,
    Custom
}
