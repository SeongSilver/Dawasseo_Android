package com.wakepoint.app.core.alarm

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 시간 알람 스케줄 순수 로직 검증(Phase 2, TDD).
 * 시계를 주입하므로 결정적이다. 기준 시각은 KST 고정.
 */
class TimeAlarmScheduleTest {

    private val kst: ZoneId = ZoneId.of("Asia/Seoul")

    /** 2026-08-10 은 월요일. 오후 3시 정각을 기준 now 로 쓴다. */
    private fun monday3pm(): ZonedDateTime =
        ZonedDateTime.of(2026, 8, 10, 15, 0, 0, 0, kst)

    private fun at(year: Int, month: Int, day: Int, h: Int, m: Int) =
        ZonedDateTime.of(year, month, day, h, m, 0, 0, kst)

    // ---- daysFor ----

    @Test
    fun `반복 모드별 요일 집합이 명세대로다`() {
        assertEquals(emptySet<DayOfWeek>(), TimeAlarmSchedule.daysFor(RepeatMode.NONE))
        assertEquals(7, TimeAlarmSchedule.daysFor(RepeatMode.DAILY).size)
        assertEquals(
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            TimeAlarmSchedule.daysFor(RepeatMode.WEEKDAY)
        )
        assertEquals(
            setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            TimeAlarmSchedule.daysFor(RepeatMode.WEEKEND)
        )
        assertEquals(
            setOf(DayOfWeek.WEDNESDAY),
            TimeAlarmSchedule.daysFor(RepeatMode.CUSTOM, setOf(DayOfWeek.WEDNESDAY))
        )
    }

    // ---- NONE (일회성) ----

    @Test
    fun `NONE - 오늘 시각이 아직 미래면 오늘 발화`() {
        val next = TimeAlarmSchedule.nextTrigger(monday3pm(), 22, 30, RepeatMode.NONE)
        assertEquals(at(2026, 8, 10, 22, 30), next)
    }

    @Test
    fun `NONE - 오늘 시각이 이미 지났으면 내일 발화`() {
        val next = TimeAlarmSchedule.nextTrigger(monday3pm(), 9, 0, RepeatMode.NONE)
        assertEquals(at(2026, 8, 11, 9, 0), next)
    }

    @Test
    fun `NONE - 발화 시각이 now 와 정확히 같으면 다음날로 넘긴다`() {
        val next = TimeAlarmSchedule.nextTrigger(monday3pm(), 15, 0, RepeatMode.NONE)
        assertEquals(at(2026, 8, 11, 15, 0), next)
    }

    // ---- DAILY ----

    @Test
    fun `DAILY - 오늘 시각 미래면 오늘, 과거면 내일`() {
        assertEquals(
            at(2026, 8, 10, 23, 0),
            TimeAlarmSchedule.nextTrigger(monday3pm(), 23, 0, RepeatMode.DAILY)
        )
        assertEquals(
            at(2026, 8, 11, 8, 0),
            TimeAlarmSchedule.nextTrigger(monday3pm(), 8, 0, RepeatMode.DAILY)
        )
    }

    // ---- WEEKDAY / WEEKEND ----

    @Test
    fun `WEEKDAY - 금요일 밤 시각 지났으면 다음 월요일`() {
        // 2026-08-14 는 금요일. 23:00 기준, 22:00 알람은 지났으므로 다음 평일=월(08-17).
        val friday = at(2026, 8, 14, 23, 0)
        val next = TimeAlarmSchedule.nextTrigger(friday, 22, 0, RepeatMode.WEEKDAY)
        assertEquals(at(2026, 8, 17, 22, 0), next)
        assertEquals(DayOfWeek.MONDAY, next!!.dayOfWeek)
    }

    @Test
    fun `WEEKEND - 수요일 기준 다음 토요일`() {
        val wed = at(2026, 8, 12, 10, 0) // 수요일
        val next = TimeAlarmSchedule.nextTrigger(wed, 9, 0, RepeatMode.WEEKEND)
        assertEquals(DayOfWeek.SATURDAY, next!!.dayOfWeek)
        assertEquals(at(2026, 8, 15, 9, 0), next)
    }

    // ---- CUSTOM ----

    @Test
    fun `CUSTOM - 월요일 기준 수요일 선택 시 이번 수요일`() {
        val next = TimeAlarmSchedule.nextTrigger(
            monday3pm(), 9, 0, RepeatMode.CUSTOM, setOf(DayOfWeek.WEDNESDAY)
        )
        assertEquals(at(2026, 8, 12, 9, 0), next)
    }

    @Test
    fun `CUSTOM - 오늘이 유일한 선택 요일이고 시각 지났으면 다음주 같은 요일`() {
        // 월요일 15:00 기준, 월요일만 선택 + 09:00(지남) → 다음 월요일(08-17).
        val next = TimeAlarmSchedule.nextTrigger(
            monday3pm(), 9, 0, RepeatMode.CUSTOM, setOf(DayOfWeek.MONDAY)
        )
        assertEquals(at(2026, 8, 17, 9, 0), next)
    }

    @Test
    fun `CUSTOM - 요일 미선택이면 발화 불가(null)`() {
        assertNull(
            TimeAlarmSchedule.nextTrigger(monday3pm(), 9, 0, RepeatMode.CUSTOM, emptySet())
        )
    }

    // ---- millis 편의 ----

    @Test
    fun `nextTriggerMillis - epoch 왕복이 nextTrigger 와 일치`() {
        val now = monday3pm()
        val millis = TimeAlarmSchedule.nextTriggerMillis(
            now.toInstant().toEpochMilli(), kst, 22, 30, RepeatMode.NONE
        )
        assertEquals(at(2026, 8, 10, 22, 30).toInstant().toEpochMilli(), millis)
    }

    // ---- 생성 가능/날짜 잠금 규칙 ----

    @Test
    fun `isCreatable - CUSTOM 은 요일 있어야 하고 나머지는 항상 가능`() {
        assertTrue(TimeAlarmSchedule.isCreatable(RepeatMode.NONE, emptySet()))
        assertTrue(TimeAlarmSchedule.isCreatable(RepeatMode.DAILY, emptySet()))
        assertFalse(TimeAlarmSchedule.isCreatable(RepeatMode.CUSTOM, emptySet()))
        assertTrue(TimeAlarmSchedule.isCreatable(RepeatMode.CUSTOM, setOf(DayOfWeek.MONDAY)))
    }

    @Test
    fun `isDateLocked - NONE 만 잠금 해제`() {
        assertFalse(TimeAlarmSchedule.isDateLocked(RepeatMode.NONE))
        assertTrue(TimeAlarmSchedule.isDateLocked(RepeatMode.DAILY))
        assertTrue(TimeAlarmSchedule.isDateLocked(RepeatMode.CUSTOM))
    }

    // ---- 입력 검증 ----

    @Test(expected = IllegalArgumentException::class)
    fun `시각 범위 밖이면 예외`() {
        TimeAlarmSchedule.nextTrigger(monday3pm(), 24, 0, RepeatMode.NONE)
    }
}
