package com.wakepoint.app.feature.v2.timealarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v2 STEP1 시간 알람 순수 로직 명세 잠금(P18 TDD).
 * 명세 근거: docs/v2-report.md §7, design/v2-spec.dc.html (1b·1c).
 */
class TimeAlarmLogicTest {

    // ---- isDateLocked: 반복(NONE 외)을 고르면 날짜 선택이 잠긴다(명세 1c) ----

    @Test
    fun `반복 안 함이면 날짜 선택이 잠기지 않는다`() {
        assertFalse(TimeAlarmLogic.isDateLocked(RepeatMode.NONE))
    }

    @Test
    fun `반복을 고르면 날짜 선택이 잠긴다 - NONE 외 전부`() {
        val locked = RepeatMode.entries.filter { it != RepeatMode.NONE }
        locked.forEach { mode ->
            assertTrue("$mode 는 날짜가 잠겨야 한다", TimeAlarmLogic.isDateLocked(mode))
        }
        // 전수 검증: NONE 하나만 unlocked
        assertEquals(1, RepeatMode.entries.count { !TimeAlarmLogic.isDateLocked(it) })
    }

    // ---- isAddEnabled: [알람 추가] 버튼 활성 조건 ----

    @Test
    fun `과거 시각이면 어떤 모드에서도 항상 비활성`() {
        RepeatMode.entries.forEach { mode ->
            val days = if (mode == RepeatMode.CUSTOM) setOf(0, 1) else emptySet()
            assertFalse(
                "$mode + 과거시각은 비활성이어야 한다",
                TimeAlarmLogic.isAddEnabled(timeIsPast = true, mode = mode, customDays = days)
            )
        }
    }

    @Test
    fun `요일 직접인데 선택 요일이 없으면 비활성`() {
        assertFalse(
            TimeAlarmLogic.isAddEnabled(
                timeIsPast = false,
                mode = RepeatMode.CUSTOM,
                customDays = emptySet()
            )
        )
    }

    @Test
    fun `요일 직접이고 하나 이상 선택하면 활성`() {
        assertTrue(
            TimeAlarmLogic.isAddEnabled(
                timeIsPast = false,
                mode = RepeatMode.CUSTOM,
                customDays = setOf(2)
            )
        )
    }

    @Test
    fun `비-CUSTOM이고 미래 시각이면 요일 선택과 무관하게 활성`() {
        val nonCustom = RepeatMode.entries.filter { it != RepeatMode.CUSTOM }
        nonCustom.forEach { mode ->
            assertTrue(
                "$mode + 미래시각은 활성이어야 한다",
                TimeAlarmLogic.isAddEnabled(timeIsPast = false, mode = mode, customDays = emptySet())
            )
        }
    }

    @Test
    fun `CUSTOM 여부가 customDays 유효성 판정을 좌우한다 - 진리표`() {
        // 미래시각 고정, (mode, days) 조합의 활성 여부
        data class Case(val mode: RepeatMode, val days: Set<Int>, val expected: Boolean)
        val cases = listOf(
            Case(RepeatMode.NONE, emptySet(), true),
            Case(RepeatMode.DAILY, emptySet(), true),
            Case(RepeatMode.WEEKDAY, emptySet(), true),
            Case(RepeatMode.WEEKEND, emptySet(), true),
            Case(RepeatMode.CUSTOM, emptySet(), false),
            Case(RepeatMode.CUSTOM, setOf(0), true),
            Case(RepeatMode.CUSTOM, setOf(0, 3, 6), true),
        )
        cases.forEach { (mode, days, expected) ->
            assertEquals(
                "mode=$mode days=$days",
                expected,
                TimeAlarmLogic.isAddEnabled(timeIsPast = false, mode = mode, customDays = days)
            )
        }
    }
}
