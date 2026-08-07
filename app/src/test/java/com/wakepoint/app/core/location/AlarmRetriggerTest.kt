package com.wakepoint.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 계속 유지(반복) 알람의 발동/재무장 결정 로직(decideAlarmTriggerAction) 테스트.
 *
 * 요구사항: 알람이 한 번 울리면, 설정한 반경을 벗어났다가 다시 진입할 때 재발동한다.
 * 반경 안에 계속 있는 동안에는 반복해서 울리지 않는다.
 */
class AlarmRetriggerTest {

    @Test
    fun `반경 진입 & 아직 발동 전이면 발동한다`() {
        assertEquals(
            AlarmTriggerAction.FIRE,
            decideAlarmTriggerAction(reached = true, repeating = true, currentlyTriggering = false)
        )
        // 일반 알람도 첫 진입엔 발동한다.
        assertEquals(
            AlarmTriggerAction.FIRE,
            decideAlarmTriggerAction(reached = true, repeating = false, currentlyTriggering = false)
        )
    }

    @Test
    fun `반경 안에 이미 발동 중이면 다시 울리지 않는다`() {
        assertEquals(
            AlarmTriggerAction.NONE,
            decideAlarmTriggerAction(reached = true, repeating = true, currentlyTriggering = true)
        )
    }

    @Test
    fun `반복 알람이 반경을 벗어나면 재무장한다`() {
        assertEquals(
            AlarmTriggerAction.REARM,
            decideAlarmTriggerAction(reached = false, repeating = true, currentlyTriggering = true)
        )
    }

    @Test
    fun `비반복 알람은 반경을 벗어나도 재무장하지 않는다`() {
        assertEquals(
            AlarmTriggerAction.NONE,
            decideAlarmTriggerAction(reached = false, repeating = false, currentlyTriggering = true)
        )
    }

    @Test
    fun `반경 밖에서 발동 상태가 아니면 아무 것도 하지 않는다`() {
        assertEquals(
            AlarmTriggerAction.NONE,
            decideAlarmTriggerAction(reached = false, repeating = true, currentlyTriggering = false)
        )
        assertEquals(
            AlarmTriggerAction.NONE,
            decideAlarmTriggerAction(reached = false, repeating = false, currentlyTriggering = false)
        )
    }

    @Test
    fun `반복 알람 전체 사이클 - 진입 발동 유지 이탈 재무장 재진입 재발동`() {
        // 상태 머신을 실제 서비스와 동일하게 모사: currentlyTriggering 을 액션에 따라 갱신.
        var triggering = false
        fun step(reached: Boolean): AlarmTriggerAction {
            val action = decideAlarmTriggerAction(
                reached = reached,
                repeating = true,
                currentlyTriggering = triggering
            )
            when (action) {
                AlarmTriggerAction.FIRE -> triggering = true
                AlarmTriggerAction.REARM -> triggering = false
                AlarmTriggerAction.NONE -> Unit
            }
            return action
        }

        assertEquals(AlarmTriggerAction.FIRE, step(reached = true))   // 최초 진입 → 발동
        assertEquals(AlarmTriggerAction.NONE, step(reached = true))   // 계속 반경 안 → 재발동 안 함
        assertEquals(AlarmTriggerAction.NONE, step(reached = true))   // 여전히 안 → 조용
        assertEquals(AlarmTriggerAction.REARM, step(reached = false)) // 반경 이탈 → 재무장
        assertEquals(AlarmTriggerAction.NONE, step(reached = false))  // 밖에서 대기
        assertEquals(AlarmTriggerAction.FIRE, step(reached = true))   // 재진입 → 다시 발동
    }
}
