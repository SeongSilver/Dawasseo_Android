package com.wakepoint.app.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlarmSoundSelectionTest {
    @Test
    fun customSound_isPreferredWhenPresent() {
        val result = resolveAlarmSoundUri(
            custom = "content://media/custom/1",
            defaultAlarm = "content://settings/alarm",
            defaultNotification = "content://settings/notification",
            defaultRingtone = "content://settings/ringtone"
        )

        assertEquals("content://media/custom/1", result)
    }

    @Test
    fun blankCustomSound_fallsBackToDefaultAlarm() {
        val result = resolveAlarmSoundUri(
            custom = "   ",
            defaultAlarm = "content://settings/alarm",
            defaultNotification = "content://settings/notification",
            defaultRingtone = "content://settings/ringtone"
        )

        assertEquals("content://settings/alarm", result)
    }

    @Test
    fun missingAlarm_fallsBackToNotificationThenRingtone() {
        val notification = resolveAlarmSoundUri(
            custom = null,
            defaultAlarm = null,
            defaultNotification = "content://settings/notification",
            defaultRingtone = "content://settings/ringtone"
        )
        val ringtone = resolveAlarmSoundUri(
            custom = null,
            defaultAlarm = null,
            defaultNotification = null,
            defaultRingtone = "content://settings/ringtone"
        )

        assertEquals("content://settings/notification", notification)
        assertEquals("content://settings/ringtone", ringtone)
    }

    @Test
    fun nothingAvailable_returnsNull() {
        assertNull(resolveAlarmSoundUri(null, null, null, null))
    }

    @Test
    fun fallback_neverUsesCustomSound() {
        val result = resolveFallbackSoundUri(
            defaultAlarm = "content://settings/alarm",
            defaultNotification = null,
            defaultRingtone = null
        )

        assertEquals("content://settings/alarm", result)
    }
}
