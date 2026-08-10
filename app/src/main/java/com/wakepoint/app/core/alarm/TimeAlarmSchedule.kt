package com.wakepoint.app.core.alarm

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * v2 STEP1 시간 알람 반복 모드(명세 1-2).
 * - [NONE] 반복 안 함(일회성): 다음 hh:mm 시각 1회.
 * - [DAILY] 매일 / [WEEKDAY] 평일(월~금) / [WEEKEND] 주말(토·일) / [CUSTOM] 요일 직접.
 */
enum class RepeatMode(val label: String) {
    NONE("안 함"),
    DAILY("매일"),
    WEEKDAY("평일"),
    WEEKEND("주말"),
    CUSTOM("요일 직접"),
}

/**
 * 시간 알람의 "다음 발화 시각"을 계산하는 순수 로직.
 *
 * 스케줄러(AlarmManager)·뷰모델과 완전히 분리돼 있어 시계 주입만으로 결정적 테스트가 가능하다.
 * 반복 모드별 요일 집합을 펼친 뒤, [now] 이후 가장 이른 hh:mm 발화 시각을 찾는다.
 */
object TimeAlarmSchedule {

    /** 반복 모드가 실제로 반복하는 요일 집합. NONE 은 (일회성이라) 빈 집합. */
    fun daysFor(mode: RepeatMode, customDays: Set<DayOfWeek> = emptySet()): Set<DayOfWeek> =
        when (mode) {
            RepeatMode.NONE -> emptySet()
            RepeatMode.DAILY -> DayOfWeek.entries.toSet()
            RepeatMode.WEEKDAY -> setOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            )
            RepeatMode.WEEKEND -> setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            RepeatMode.CUSTOM -> customDays
        }

    /**
     * [now] 이후 가장 이른 발화 시각. 발화 불가면 null.
     * - NONE: 오늘 hh:mm 이 아직 미래면 오늘, 지났으면 내일 1회.
     * - 반복: 오늘 포함 앞으로 최대 7일 안에서 요일이 맞고 [now] 이후인 첫 시각.
     * - CUSTOM 인데 요일이 하나도 없으면 발화 불가 → null.
     *
     * 경계: 발화 시각이 [now] 와 정확히 같으면 "이미 지난 것"으로 보고 다음 발화로 넘긴다(엄격 미래).
     */
    fun nextTrigger(
        now: ZonedDateTime,
        hour: Int,
        minute: Int,
        repeatMode: RepeatMode,
        customDays: Set<DayOfWeek> = emptySet(),
    ): ZonedDateTime? {
        require(hour in 0..23) { "hour out of range: $hour" }
        require(minute in 0..59) { "minute out of range: $minute" }

        val days = daysFor(repeatMode, customDays)
        if (repeatMode != RepeatMode.NONE && days.isEmpty()) return null

        val todayAtTime = now.toLocalDate().atTime(hour, minute).atZone(now.zone)

        if (repeatMode == RepeatMode.NONE) {
            return if (todayAtTime.isAfter(now)) todayAtTime else todayAtTime.plusDays(1)
        }

        // 오늘(offset 0)부터 7일 뒤(같은 요일 재도래)까지 훑어 첫 유효 발화를 찾는다.
        for (offset in 0..7) {
            val candidate = todayAtTime.plusDays(offset.toLong())
            if (candidate.dayOfWeek in days && candidate.isAfter(now)) return candidate
        }
        return null
    }

    /** 스케줄러 편의: epoch millis 로 다음 발화 시각. 발화 불가면 null. */
    fun nextTriggerMillis(
        nowMillis: Long,
        zone: ZoneId,
        hour: Int,
        minute: Int,
        repeatMode: RepeatMode,
        customDays: Set<DayOfWeek> = emptySet(),
    ): Long? {
        val now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zone)
        return nextTrigger(now, hour, minute, repeatMode, customDays)
            ?.toInstant()?.toEpochMilli()
    }

    /**
     * [알람 추가] 버튼 활성 조건(명세 1c).
     * - 일회성(NONE)은 항상 다음 발화가 존재하므로 활성.
     * - CUSTOM 은 요일을 하나 이상 골라야 활성.
     * - 그 외 반복 모드는 항상 활성.
     */
    fun isCreatable(repeatMode: RepeatMode, customDays: Set<DayOfWeek>): Boolean =
        repeatMode != RepeatMode.CUSTOM || customDays.isNotEmpty()

    /** 날짜 선택 잠금(명세 1c): 반복(NONE 외)을 고르면 날짜는 잠기고 시각만 적용. */
    fun isDateLocked(repeatMode: RepeatMode): Boolean = repeatMode != RepeatMode.NONE
}
