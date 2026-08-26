package com.wakepoint.app.data.alarm.remote

import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import org.json.JSONObject

/**
 * Supabase `alarms` 테이블 왕복 DTO.
 *
 * 정책: 도메인 [Alarm]의 soundEnabled/vibrate/isFavorite/repeating 은 **기기 로컬 전용**이라
 * 이 DTO에 없다(원격 스키마 미변경 결정). 따라서 서버 왕복 시 이 필드들은 로컬 DB 값이
 * 유지되고, 다른 기기에는 전파되지 않는다. 컬럼 확장 시 여기와 SCHEMA.md 를 함께 갱신할 것.
 */
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

/**
 * 원격 DTO → 도메인. 로컬 전용 필드(soundEnabled/vibrate/isFavorite/repeating)는 기본값.
 * 로컬 캐시를 거치지 않고 읽는 화면(보낸 알람 목록 3-6)에서 사용한다.
 */
fun AlarmDto.toDomain(): Alarm = Alarm(
    id = id,
    ownerId = ownerId,
    createdBy = createdBy,
    label = label,
    targetLat = targetLat,
    targetLng = targetLng,
    targetAddress = targetAddress,
    radiusKm = radiusKm,
    isActive = isActive,
    soundType = if (soundType == "custom") SoundType.Custom else SoundType.Default,
    soundUri = soundUri
)

fun alarmDtoFromJson(json: JSONObject): AlarmDto = AlarmDto(
    id = json.getString("id"),
    ownerId = json.optString("owner_id"),
    createdBy = json.optString("created_by"),
    label = json.optString("label"),
    targetLat = json.optDouble("target_lat", 0.0),
    targetLng = json.optDouble("target_lng", 0.0),
    targetAddress = json.optString("target_address"),
    radiusKm = json.optDouble("radius_km", 0.5),
    isActive = json.optBoolean("is_active", true),
    soundType = json.optString("sound_type", "default"),
    soundUri = if (json.isNull("sound_uri")) null else json.optString("sound_uri").takeIf { it.isNotBlank() }
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
