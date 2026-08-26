package com.wakepoint.app.feature.timealarm

import com.wakepoint.app.core.alarm.RepeatMode
import com.wakepoint.app.domain.model.TimeAlarm
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeAlarmFormatTest {

    private val zone = ZoneId.of("Asia/Seoul")

    /** 2026-08-26(수) 오후 3시. */
    private val now: ZonedDateTime = ZonedDateTime.of(2026, 8, 26, 15, 0, 0, 0, zone)

    private fun alarm(
        hour: Int,
        minute: Int,
        mode: RepeatMode = RepeatMode.NONE,
        days: Set<DayOfWeek> = emptySet(),
    ) = TimeAlarm(id = "t1", label = "테스트", hour = hour, minute = minute, repeatMode = mode, repeatDays = days)

    // --- timeText ---

    @Test
    fun `오전 오후 표기`() {
        assertEquals("오전 7:00", TimeAlarmFormat.timeText(7, 0))
        assertEquals("오후 6:30", TimeAlarmFormat.timeText(18, 30))
    }

    @Test
    fun `정오는 오후 12시 자정은 오전 12시`() {
        assertEquals("오후 12:00", TimeAlarmFormat.timeText(12, 0))
        assertEquals("오전 12:05", TimeAlarmFormat.timeText(0, 5))
    }

    @Test
    fun `분은 두 자리로 패딩`() {
        assertEquals("오전 9:05", TimeAlarmFormat.timeText(9, 5))
    }

    // --- repeatText ---

    @Test
    fun `NONE 은 반복 표기가 없다`() {
        assertEquals(null, TimeAlarmFormat.repeatText(RepeatMode.NONE))
    }

    @Test
    fun `프리셋 반복은 라벨 그대로`() {
        assertEquals("매일", TimeAlarmFormat.repeatText(RepeatMode.DAILY))
        assertEquals("평일", TimeAlarmFormat.repeatText(RepeatMode.WEEKDAY))
        assertEquals("주말", TimeAlarmFormat.repeatText(RepeatMode.WEEKEND))
    }

    @Test
    fun `요일 직접은 월화수 순서로 나열`() {
        assertEquals(
            "월·수·금",
            TimeAlarmFormat.repeatText(
                RepeatMode.CUSTOM,
                setOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
            )
        )
    }

    @Test
    fun `요일 직접이 프리셋과 같으면 프리셋 이름으로 축약`() {
        assertEquals("매일", TimeAlarmFormat.repeatText(RepeatMode.CUSTOM, DayOfWeek.entries.toSet()))
        assertEquals(
            "주말",
            TimeAlarmFormat.repeatText(RepeatMode.CUSTOM, setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        )
    }

    @Test
    fun `요일 직접인데 요일이 비면 null`() {
        assertEquals(null, TimeAlarmFormat.repeatText(RepeatMode.CUSTOM, emptySet()))
    }

    // --- metaText / oneShotDayText ---

    @Test
    fun `일회성 미래 시각은 오늘로 표기`() {
        assertEquals("오늘 오후 6:30", TimeAlarmFormat.metaText(alarm(18, 30), now))
    }

    @Test
    fun `일회성 지난 시각은 내일로 표기`() {
        assertEquals("내일 오전 7:00", TimeAlarmFormat.metaText(alarm(7, 0), now))
    }

    @Test
    fun `일회성 현재 시각과 같으면 내일로 표기`() {
        // TimeAlarmSchedule 은 정확히 같은 시각을 "지난 것"으로 본다(엄격 미래).
        assertEquals("내일 오후 3:00", TimeAlarmFormat.metaText(alarm(15, 0), now))
    }

    @Test
    fun `반복 알람은 반복 표기와 시각`() {
        assertEquals("매일 오전 7:00", TimeAlarmFormat.metaText(alarm(7, 0, RepeatMode.DAILY), now))
        assertEquals(
            "월·수 오후 10:00",
            TimeAlarmFormat.metaText(
                alarm(22, 0, RepeatMode.CUSTOM, setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)),
                now
            )
        )
    }

    @Test
    fun `요일 없는 CUSTOM 은 시각만 표기`() {
        assertEquals("오전 7:00", TimeAlarmFormat.metaText(alarm(7, 0, RepeatMode.CUSTOM), now))
    }
}
