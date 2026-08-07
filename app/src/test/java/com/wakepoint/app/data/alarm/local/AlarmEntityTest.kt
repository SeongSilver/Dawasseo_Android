package com.wakepoint.app.data.alarm.local

import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Room AlarmEntity <-> 도메인 Alarm 매핑 테스트.
 *
 * 핵심 회귀 방지: 로컬 전용 필드(repeating, pendingSync, soundEnabled, vibrate,
 * isFavorite)가 왕복 변환에서 유실되면 오프라인 동기화/재발동 로직이 깨진다.
 */
class AlarmEntityTest {

    private fun sampleAlarm(
        soundType: SoundType = SoundType.Custom,
        repeating: Boolean = true,
        pendingSync: Boolean = true
    ): Alarm = Alarm(
        id = "alarm-1",
        ownerId = "owner-1",
        createdBy = "creator-1",
        label = "회사 앞",
        targetLat = 37.5665,
        targetLng = 126.9780,
        targetAddress = "서울 중구 세종대로 110",
        radiusKm = 0.5,
        isActive = true,
        soundType = soundType,
        soundUri = "content://media/custom/1",
        soundEnabled = false,
        vibrate = false,
        isFavorite = true,
        repeating = repeating,
        pendingSync = pendingSync
    )

    @Test
    fun `도메인 - 엔티티 왕복 변환은 모든 필드를 보존한다`() {
        val original = sampleAlarm()
        val roundTripped = original.toEntity().toDomain()
        assertEquals(original, roundTripped)
    }

    @Test
    fun `soundType은 enum name 문자열로 저장된다`() {
        assertEquals(SoundType.Custom.name, sampleAlarm(soundType = SoundType.Custom).toEntity().soundType)
        assertEquals(SoundType.Default.name, sampleAlarm(soundType = SoundType.Default).toEntity().soundType)
    }

    @Test
    fun `알 수 없는 soundType 문자열은 Default로 폴백한다`() {
        val entity = sampleAlarm().toEntity().copy(soundType = "legacy-unknown")
        assertEquals(SoundType.Default, entity.toDomain().soundType)
    }

    @Test
    fun `로컬 전용 동기화 플래그가 왕복에서 유실되지 않는다`() {
        val synced = sampleAlarm(repeating = false, pendingSync = false).toEntity().toDomain()
        assertFalse(synced.repeating)
        assertFalse(synced.pendingSync)

        val pending = sampleAlarm(repeating = true, pendingSync = true).toEntity().toDomain()
        assertTrue(pending.repeating)
        assertTrue(pending.pendingSync)
    }

    @Test
    fun `엔티티 기본값은 소리와 진동이 켜진 일반 알람이다`() {
        val entity = AlarmEntity(
            id = "alarm-2",
            ownerId = "owner-1",
            createdBy = "creator-1",
            label = "집",
            targetLat = 37.0,
            targetLng = 127.0,
            targetAddress = "주소",
            radiusKm = 1.0,
            isActive = true,
            soundType = SoundType.Default.name,
            soundUri = null
        )
        val domain = entity.toDomain()
        assertTrue(domain.soundEnabled)
        assertTrue(domain.vibrate)
        assertFalse(domain.isFavorite)
        assertFalse(domain.repeating)
        assertFalse(domain.pendingSync)
    }
}
