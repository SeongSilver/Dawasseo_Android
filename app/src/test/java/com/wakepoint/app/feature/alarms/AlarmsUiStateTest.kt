package com.wakepoint.app.feature.alarms

import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 알람 목록 화면 상태(AlarmsUiState)의 섹션 분류 로직 테스트.
 *
 * 규칙: 즐겨찾기는 활성 여부와 무관하게 즐겨찾기 섹션에만 노출되고,
 * 나머지는 활성/비활성 섹션으로 나뉜다. 어떤 알람도 두 섹션에 중복되지 않는다.
 */
class AlarmsUiStateTest {

    private fun alarm(id: String, isActive: Boolean, isFavorite: Boolean): Alarm = Alarm(
        id = id,
        ownerId = "owner-1",
        createdBy = "owner-1",
        label = "알람 $id",
        targetLat = 37.0,
        targetLng = 127.0,
        targetAddress = "주소",
        radiusKm = 0.5,
        isActive = isActive,
        soundType = SoundType.Default,
        soundUri = null,
        isFavorite = isFavorite
    )

    @Test
    fun `즐겨찾기 알람은 활성 여부와 무관하게 즐겨찾기 섹션에만 나타난다`() {
        val favoriteActive = alarm("fav-on", isActive = true, isFavorite = true)
        val favoriteInactive = alarm("fav-off", isActive = false, isFavorite = true)
        val state = AlarmsUiState(alarms = listOf(favoriteActive, favoriteInactive))

        assertEquals(listOf(favoriteActive, favoriteInactive), state.favoriteAlarms)
        assertTrue(state.activeAlarms.isEmpty())
        assertTrue(state.inactiveAlarms.isEmpty())
    }

    @Test
    fun `즐겨찾기가 아닌 알람은 활성과 비활성으로 나뉜다`() {
        val active = alarm("on", isActive = true, isFavorite = false)
        val inactive = alarm("off", isActive = false, isFavorite = false)
        val state = AlarmsUiState(alarms = listOf(active, inactive))

        assertEquals(listOf(active), state.activeAlarms)
        assertEquals(listOf(inactive), state.inactiveAlarms)
        assertTrue(state.favoriteAlarms.isEmpty())
    }

    @Test
    fun `세 섹션은 전체 목록을 중복 없이 분할한다`() {
        val alarms = listOf(
            alarm("a", isActive = true, isFavorite = false),
            alarm("b", isActive = false, isFavorite = false),
            alarm("c", isActive = true, isFavorite = true),
            alarm("d", isActive = false, isFavorite = true)
        )
        val state = AlarmsUiState(alarms = alarms)

        val union = state.favoriteAlarms + state.activeAlarms + state.inactiveAlarms
        assertEquals(alarms.size, union.size)
        assertEquals(alarms.map { it.id }.toSet(), union.map { it.id }.toSet())
    }

    @Test
    fun `빈 목록이면 모든 섹션이 비어 있다`() {
        val state = AlarmsUiState()
        assertTrue(state.favoriteAlarms.isEmpty())
        assertTrue(state.activeAlarms.isEmpty())
        assertTrue(state.inactiveAlarms.isEmpty())
    }
}
