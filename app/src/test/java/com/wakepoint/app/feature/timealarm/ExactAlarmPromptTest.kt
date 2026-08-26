package com.wakepoint.app.feature.timealarm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 정확 알람 안내(1-5)는 "미허용 + 아직 안 물어봄"일 때만 뜬다(설치당 1회). */
class ExactAlarmPromptTest {

    @Test
    fun `미허용이고 처음이면 안내한다`() {
        assertTrue(shouldPromptExactAlarm(exactAlarmAllowed = false, alreadyPrompted = false))
    }

    @Test
    fun `이미 허용됐으면 안내하지 않는다`() {
        assertFalse(shouldPromptExactAlarm(exactAlarmAllowed = true, alreadyPrompted = false))
    }

    @Test
    fun `한 번 응답했으면 다시 묻지 않는다`() {
        assertFalse(shouldPromptExactAlarm(exactAlarmAllowed = false, alreadyPrompted = true))
        assertFalse(shouldPromptExactAlarm(exactAlarmAllowed = true, alreadyPrompted = true))
    }
}
