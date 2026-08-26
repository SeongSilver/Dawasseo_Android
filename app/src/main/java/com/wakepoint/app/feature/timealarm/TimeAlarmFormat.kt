package com.wakepoint.app.feature.timealarm

import com.wakepoint.app.core.alarm.RepeatMode
import com.wakepoint.app.core.alarm.TimeAlarmSchedule
import com.wakepoint.app.domain.model.TimeAlarm
import java.time.DayOfWeek
import java.time.ZonedDateTime

/**
 * 시간 알람을 목록/시트에 표시할 한국어 문구로 바꾸는 순수 포맷터(명세 1d 메타줄).
 *
 * 시계를 파라미터로 받아 결정적으로 테스트한다. 발화 시각 계산은
 * [TimeAlarmSchedule] 을 그대로 사용해 스케줄러와 표기가 어긋나지 않게 한다.
 */
object TimeAlarmFormat {

    /** 요일 표기 순서는 한국 관례(월~일)를 따른다. */
    val WEEK_ORDER: List<DayOfWeek> = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
    )

    fun dayLabel(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

    /** "오전 7:00" / "오후 6:30". 정오(12:00)는 오후, 자정(0:00)은 오전 12:00. */
    fun timeText(hour: Int, minute: Int): String {
        require(hour in 0..23 && minute in 0..59) { "invalid time: $hour:$minute" }
        val meridiem = if (hour < 12) "오전" else "오후"
        val displayHour = when (hour % 12) {
            0 -> 12
            else -> hour % 12
        }
        return "$meridiem $displayHour:%02d".format(minute)
    }

    /**
     * 반복 표기. NONE 은 (반복이 없으므로) null, CUSTOM 은 "월·수·금" 식으로 요일 나열.
     * CUSTOM 이 매일/평일/주말과 동일 집합이면 해당 이름으로 줄인다.
     */
    fun repeatText(mode: RepeatMode, customDays: Set<DayOfWeek> = emptySet()): String? =
        when (mode) {
            RepeatMode.NONE -> null
            RepeatMode.DAILY, RepeatMode.WEEKDAY, RepeatMode.WEEKEND -> mode.label
            RepeatMode.CUSTOM -> when (customDays) {
                TimeAlarmSchedule.daysFor(RepeatMode.DAILY) -> RepeatMode.DAILY.label
                TimeAlarmSchedule.daysFor(RepeatMode.WEEKDAY) -> RepeatMode.WEEKDAY.label
                TimeAlarmSchedule.daysFor(RepeatMode.WEEKEND) -> RepeatMode.WEEKEND.label
                else -> WEEK_ORDER.filter { it in customDays }
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString("·") { dayLabel(it) }
            }
        }

    /**
     * 목록 메타줄. 일회성은 다음 발화 날짜("오늘"/"내일") + 시각, 반복은 반복 표기 + 시각.
     * CUSTOM 인데 요일이 비어 발화가 불가능하면 시각만 표기한다.
     */
    fun metaText(alarm: TimeAlarm, now: ZonedDateTime): String {
        val time = timeText(alarm.hour, alarm.minute)
        if (alarm.repeatMode == RepeatMode.NONE) {
            return "${oneShotDayText(alarm.hour, alarm.minute, now)} $time"
        }
        val repeat = repeatText(alarm.repeatMode, alarm.repeatDays) ?: return time
        return "$repeat $time"
    }

    /** 일회성 알람의 다음 발화 날짜 표기: 오늘 안이면 "오늘", 넘어가면 "내일". */
    fun oneShotDayText(hour: Int, minute: Int, now: ZonedDateTime): String {
        val next = TimeAlarmSchedule.nextTrigger(now, hour, minute, RepeatMode.NONE)
        return if (next != null && next.toLocalDate() == now.toLocalDate()) "오늘" else "내일"
    }
}
