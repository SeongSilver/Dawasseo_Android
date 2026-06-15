package com.wakepoint.app.data.alarm.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "created_by") val createdBy: String,
    val label: String,
    @ColumnInfo(name = "target_lat") val targetLat: Double,
    @ColumnInfo(name = "target_lng") val targetLng: Double,
    @ColumnInfo(name = "target_address") val targetAddress: String,
    @ColumnInfo(name = "radius_km") val radiusKm: Double,
    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "triggered_at") val triggeredAt: String?,
    @ColumnInfo(name = "sound_type") val soundType: String,
    @ColumnInfo(name = "sound_uri") val soundUri: String?
)

fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    ownerId = ownerId,
    createdBy = createdBy,
    label = label,
    targetLat = targetLat,
    targetLng = targetLng,
    targetAddress = targetAddress,
    radiusKm = radiusKm,
    isActive = isActive,
    triggeredAt = triggeredAt,
    soundType = when (soundType) {
        SoundType.Custom.name -> SoundType.Custom
        else -> SoundType.Default
    },
    soundUri = soundUri
)

fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    ownerId = ownerId,
    createdBy = createdBy,
    label = label,
    targetLat = targetLat,
    targetLng = targetLng,
    targetAddress = targetAddress,
    radiusKm = radiusKm,
    isActive = isActive,
    triggeredAt = triggeredAt,
    soundType = soundType.name,
    soundUri = soundUri
)
