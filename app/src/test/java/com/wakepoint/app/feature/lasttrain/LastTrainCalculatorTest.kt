package com.wakepoint.app.feature.lasttrain

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LastTrainCalculatorTest {

    private val zone = ZoneId.of("Asia/Seoul")
    private val calculator = MockLastTrainCalculator()

    private fun at(hour: Int, minute: Int): ZonedDateTime =
        ZonedDateTime.of(2026, 8, 26, hour, minute, 0, 0, zone)

    // --- 단계 정책 ---

    @Test
    fun `15분 초과면 여유`() {
        assertEquals(LastTrainLevel.RELAXED, LastTrainPolicy.levelFor(16))
        assertEquals(LastTrainLevel.RELAXED, LastTrainPolicy.levelFor(120))
    }

    @Test
    fun `15분 이하면 임박`() {
        assertEquals(LastTrainLevel.IMMINENT, LastTrainPolicy.levelFor(15))
        assertEquals(LastTrainLevel.IMMINENT, LastTrainPolicy.levelFor(1))
    }

    @Test
    fun `0분 이하면 아슬`() {
        assertEquals(LastTrainLevel.CRITICAL, LastTrainPolicy.levelFor(0))
        assertEquals(LastTrainLevel.CRITICAL, LastTrainPolicy.levelFor(-4))
    }

    // --- 모의 계산기 ---

    @Test
    fun `막차 한참 전이면 여유 + 택시비 지연 조회`() {
        val result = calculator.lookup(at(22, 0)) as LastTrainResult.Available
        assertEquals(105, result.remainingMinutes)
        assertEquals(LastTrainLevel.RELAXED, result.level)
        assertEquals(null, result.taxiFare)
        assertEquals(23, result.deadlineHour)
        assertEquals(45, result.deadlineMinute)
    }

    @Test
    fun `막차 10분 전이면 임박 + 택시비 노출`() {
        val result = calculator.lookup(at(23, 35)) as LastTrainResult.Available
        assertEquals(10, result.remainingMinutes)
        assertEquals(LastTrainLevel.IMMINENT, result.level)
        assertTrue(result.taxiFare != null)
    }

    @Test
    fun `막차 지나면 끊김 + 첫차 안내`() {
        val result = calculator.lookup(at(23, 50))
        assertTrue(result is LastTrainResult.Ended)
        assertEquals(5, (result as LastTrainResult.Ended).firstTrainHour)
        assertEquals(34, result.firstTrainMinute)
    }

    @Test
    fun `심야에는 끊김`() {
        assertTrue(calculator.lookup(at(2, 0)) is LastTrainResult.Ended)
    }

    @Test
    fun `첫차 이후 낮에는 다음 막차 기준 여유`() {
        val result = calculator.lookup(at(9, 0))
        assertTrue(result is LastTrainResult.Available)
        assertEquals(LastTrainLevel.RELAXED, (result as LastTrainResult.Available).level)
    }
}
