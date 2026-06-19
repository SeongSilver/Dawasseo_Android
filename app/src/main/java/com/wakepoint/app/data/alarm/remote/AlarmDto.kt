package com.wakepoint.app.data.alarm.remote

import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import org.json.JSONObject

data class AlarmDto(
    val id: String,
    val ownerId: String,
    val createdBy: String,
    val label: String,
    val targetLat: Double,
    val targetLng: Double,
    val targetAddress: String,
    val radiusKm: Double,
    val isActive: Boolean,
    val soundType: String,
    val soundUri: String?
)

fun Alarm.toDto(): AlarmDto = AlarmDto(
    id = id,
    ownerId = ownerId,
    createdBy = createdBy,
    label = label,
    targetLat = targetLat,
    targetLng = targetLng,
    targetAddress = targetAddress,
    radiusKm = radiusKm,
    isActive = isActive,
    soundType = when (soundType) {
        SoundType.Default -> "default"
        SoundType.Custom -> "custom"
    },
    soundUri = soundUri
)

fun AlarmDto.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("owner_id", ownerId)
        .put("created_by", createdBy)
        .put("label", label)
        .put("target_lat", targetLat)
        .put("target_lng", targetLng)
        .put("target_address", targetAddress)
        .put("radius_km", radiusKm)
        .put("is_active", isActive)
        .put("sound_type", soundType)
        .put("sound_uri", soundUri)
}
