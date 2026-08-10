package com.wakepoint.app.data.timealarm.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakepoint.app.core.alarm.RepeatMode
import com.wakepoint.app.domain.model.TimeAlarm
import java.time.DayOfWeek

/**
 * 시간 알람 로컬 테이블. 위치 알람(`alarms`)과 분리된 독립 테이블이라
 * 위치추적·Supabase 동기화 경로에 전혀 관여하지 않는다.
 *
 * [repeatDays] 는 요일 번호(1=월 … 7=일, [DayOfWeek.getValue])를 쉼표로 이은 문자열.
 * 빈 문자열이면 지정 요일 없음.
 */
@Entity(tableName = "time_alarms")
data class TimeAlarmEntity(
    @PrimaryKey val id: String,
    val label: String,
    val hour: Int,
    val minute: Int,
    @ColumnInfo(name = "repeat_mode") val repeatMode: String,
    @ColumnInfo(name = "repeat_days") val repeatDays: String,
    @ColumnInfo(name = "enabled", defaultValue = "1") val enabled: Boolean = true,
    @ColumnInfo(name = "sound_enabled", defaultValue = "1") val soundEnabled: Boolean = true,
    @ColumnInfo(name = "vibrate", defaultValue = "1") val vibrate: Boolean = true,
    @ColumnInfo(name = "sound_uri") val soundUri: String? = null,
)

/** 요일 집합 → "1,3,5" 형식 문자열(정렬 고정). */
internal fun Set<DayOfWeek>.encodeDays(): String =
    sortedBy { it.value }.joinToString(",") { it.value.toString() }

/** "1,3,5" → 요일 집합. 빈/공백은 빈 집합. 잘못된 토큰은 무시. */
internal fun String.decodeDays(): Set<DayOfWeek> =
    split(",")
        .mapNotNull { token -> token.trim().toIntOrNull()?.takeIf { it in 1..7 } }
        .map { DayOfWeek.of(it) }
        .toSet()

fun TimeAlarmEntity.toDomain(): TimeAlarm = TimeAlarm(
    id = id,
    label = label,
    hour = hour,
    minute = minute,
    repeatMode = runCatching { RepeatMode.valueOf(repeatMode) }.getOrDefault(RepeatMode.NONE),
    repeatDays = repeatDays.decodeDays(),
    enabled = enabled,
    soundEnabled = soundEnabled,
    vibrate = vibrate,
    soundUri = soundUri,
)

fun TimeAlarm.toEntity(): TimeAlarmEntity = TimeAlarmEntity(
    id = id,
    label = label,
    hour = hour,
    minute = minute,
    repeatMode = repeatMode.name,
    repeatDays = repeatDays.encodeDays(),
    enabled = enabled,
    soundEnabled = soundEnabled,
    vibrate = vibrate,
    soundUri = soundUri,
)
