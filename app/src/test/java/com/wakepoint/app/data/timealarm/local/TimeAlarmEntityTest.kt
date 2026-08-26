package com.wakepoint.app.data.timealarm.local

import com.wakepoint.app.core.alarm.RepeatMode
import com.wakepoint.app.domain.model.TimeAlarm
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 시간 알람 엔티티 ↔ 도메인 매핑(요일 CSV·반복모드) 검증. */
class TimeAlarmEntityTest {

    @Test
    fun `요일 집합은 정렬된 CSV 로 인코딩된다`() {
        val days = setOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        assertEquals("1,3,5", days.encodeDays())
        assertEquals("", emptySet<DayOfWeek>().encodeDays())
    }

    @Test
    fun `CSV 는 요일 집합으로 디코딩되고 잘못된 토큰은 무시된다`() {
        assertEquals(
            setOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY),
            "1,7".decodeDays()
        )
        assertEquals(emptySet<DayOfWeek>(), "".decodeDays())
        assertEquals(emptySet<DayOfWeek>(), "  ".decodeDays())
        // 범위 밖(0,8)·비숫자는 무시
        assertEquals(setOf(DayOfWeek.TUESDAY), "0,2,8,x".decodeDays())
    }

    @Test
    fun `도메인 왕복이 값을 보존한다`() {
        val alarm = TimeAlarm(
            id = "ta-1",
            label = "기상",
            hour = 7,
            minute = 5,
            repeatMode = RepeatMode.CUSTOM,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            enabled = false,
            soundEnabled = false,
            vibrate = true,
            soundUri = "content://sound/1",
        )
        val round = alarm.toEntity().toDomain()
        assertEquals(alarm, round)
    }

    @Test
    fun `알 수 없는 반복모드 문자열은 NONE 으로 폴백된다`() {
        val entity = TimeAlarmEntity(
            id = "ta-2", label = "x", hour = 9, minute = 0,
            repeatMode = "GARBAGE", repeatDays = "",
        )
        assertEquals(RepeatMode.NONE, entity.toDomain().repeatMode)
    }

    @Test
    fun `기본값 - enabled·sound·vibrate 는 참, soundUri 는 null`() {
        val entity = TimeAlarmEntity(
            id = "ta-3", label = "y", hour = 6, minute = 30,
            repeatMode = RepeatMode.DAILY.name, repeatDays = "",
        )
        val domain = entity.toDomain()
        assertTrue(domain.enabled && domain.soundEnabled && domain.vibrate)
        assertEquals(null, domain.soundUri)
    }
}
