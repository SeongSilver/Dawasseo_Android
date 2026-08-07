package com.wakepoint.app.data.alarm.remote

import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 도메인 Alarm -> 서버 AlarmDto 매핑(toDto) 테스트.
 *
 * 참고: alarmDtoFromJson/toJson 은 org.json 이 로컬 단위테스트에서 스텁이라
 * 여기서 검증하지 않는다(TASKS_NOW.md 8/17번 선행작업 이후 추가).
 */
class AlarmDtoTest {

    private fun sampleAlarm(soundType: SoundType, soundUri: String?): Alarm = Alarm(
        id = "alarm-1",
        ownerId = "owner-1",
        createdBy = "creator-1",
        label = "학원 앞",
        targetLat = 35.1796,
        targetLng = 129.0756,
        targetAddress = "부산 중구",
        radiusKm = 3.0,
        isActive = false,
        soundType = soundType,
        soundUri = soundUri,
        soundEnabled = false,
        vibrate = false,
        isFavorite = true,
        repeating = true,
        pendingSync = true
    )

    @Test
    fun `toDto는 soundType enum을 서버 소문자 문자열로 변환한다`() {
        assertEquals("default", sampleAlarm(SoundType.Default, null).toDto().soundType)
        assertEquals("custom", sampleAlarm(SoundType.Custom, "content://sound/1").toDto().soundType)
    }

    @Test
    fun `toDto는 식별자와 좌표, 반경, 활성 상태를 그대로 복사한다`() {
        val dto = sampleAlarm(SoundType.Custom, "content://sound/1").toDto()
        assertEquals("alarm-1", dto.id)
        assertEquals("owner-1", dto.ownerId)
        assertEquals("creator-1", dto.createdBy)
        assertEquals("학원 앞", dto.label)
        assertEquals(35.1796, dto.targetLat, 1e-9)
        assertEquals(129.0756, dto.targetLng, 1e-9)
        assertEquals("부산 중구", dto.targetAddress)
        assertEquals(3.0, dto.radiusKm, 1e-9)
        assertTrue(!dto.isActive)
        assertEquals("content://sound/1", dto.soundUri)
    }

    @Test
    fun `soundUri가 없으면 null로 전달된다`() {
        assertNull(sampleAlarm(SoundType.Default, null).toDto().soundUri)
    }
}
