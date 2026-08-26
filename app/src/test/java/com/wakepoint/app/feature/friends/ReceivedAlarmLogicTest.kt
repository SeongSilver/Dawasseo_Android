package com.wakepoint.app.feature.friends

import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceivedAlarmLogicTest {

    private val me = "user-me"
    private val friend = "user-friend"

    private fun alarm(id: String, createdBy: String) = Alarm(
        id = id, ownerId = me, createdBy = createdBy, label = "강남역",
        targetLat = 37.0, targetLng = 127.0, targetAddress = "서울 강남구",
        radiusKm = 0.3, isActive = true, soundType = SoundType.Default, soundUri = null,
    )

    @Test
    fun `친구가 만든 미확인 알람만 새 수신`() {
        val result = ReceivedAlarmLogic.newReceived(
            alarms = listOf(alarm("a", me), alarm("b", friend), alarm("c", friend)),
            currentUserId = me,
            acknowledgedIds = setOf("c"),
        )
        assertEquals(listOf("b"), result.map { it.id })
    }

    @Test
    fun `세션 미로딩이면 오탐 방지로 빈 목록`() {
        val result = ReceivedAlarmLogic.newReceived(
            alarms = listOf(alarm("b", friend)),
            currentUserId = "",
            acknowledgedIds = emptySet(),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `createdBy 빈 레거시 행은 제외`() {
        val result = ReceivedAlarmLogic.newReceived(
            alarms = listOf(alarm("legacy", "")),
            currentUserId = me,
            acknowledgedIds = emptySet(),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `최초 시드는 현재 수신분 전체`() {
        val seed = ReceivedAlarmLogic.idsToSeed(
            alarms = listOf(alarm("a", me), alarm("b", friend)),
            currentUserId = me,
        )
        assertEquals(setOf("b"), seed)
    }
}
