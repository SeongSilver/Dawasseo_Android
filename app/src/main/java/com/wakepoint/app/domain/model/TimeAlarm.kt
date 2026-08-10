package com.wakepoint.app.domain.model

import com.wakepoint.app.core.alarm.RepeatMode
import java.time.DayOfWeek

/**
 * v2 STEP1 시간 알람(위치 알람과 별개의 로컬 전용 개념).
 *
 * 기존 [Alarm](위치/지오펜스·Supabase 동기화)과 완전히 분리된 도메인이다.
 * 발화 스케줄은 [com.wakepoint.app.core.alarm.TimeAlarmSchedule] 가 계산한다.
 *
 * @param hour 0..23, [minute] 0..59.
 * @param repeatDays [RepeatMode.CUSTOM] 일 때 사용하는 요일 집합.
 */
data class TimeAlarm(
    val id: String,
    val label: String,
    val hour: Int,
    val minute: Int,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val enabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val soundUri: String? = null,
)
