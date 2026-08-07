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
    @ColumnInfo(name = "sound_type") val soundType: String,
    @ColumnInfo(name = "sound_uri") val soundUri: String?,
    @ColumnInfo(name = "sound_enabled", defaultValue = "1") val soundEnabled: Boolean = true,
    @ColumnInfo(name = "vibrate", defaultValue = "1") val vibrate: Boolean = true,
    @ColumnInfo(name = "is_favorite", defaultValue = "0") val isFavorite: Boolean = false,
    @ColumnInfo(name = "repeating", defaultValue = "0") val repeating: Boolean = false,
    @ColumnInfo(name = "pending_sync", defaultValue = "0") val pendingSync: Boolean = false
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
    soundType = when (soundType) {
        SoundType.Custom.name -> SoundType.Custom
        else -> SoundType.Default
    },
    soundUri = soundUri,
    soundEnabled = soundEnabled,
    vibrate = vibrate,
    isFavorite = isFavorite,
    repeating = repeating,
    pendingSync = pendingSync
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
    soundType = soundType.name,
    soundUri = soundUri,
    soundEnabled = soundEnabled,
    vibrate = vibrate,
    isFavorite = isFavorite,
    repeating = repeating,
    pendingSync = pendingSync
)
