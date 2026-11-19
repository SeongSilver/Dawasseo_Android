package com.wakepoint.app.feature.home

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationPermissionsTest {
    @Test
    fun belowApi29_neverRequestsBackground() {
        val action = backgroundLocationAction(
            sdkInt = 28,
            hasForeground = true,
            hasBackground = false
        )

        assertEquals(BackgroundLocationAction.NotNeeded, action)
    }

    @Test
    fun withoutForeground_doesNotRequestBackground() {
        val action = backgroundLocationAction(
            sdkInt = 33,
            hasForeground = false,
            hasBackground = false
        )

        assertEquals(BackgroundLocationAction.NotNeeded, action)
    }

    @Test
    fun alreadyGranted_doesNotRequestAgain() {
        val action = backgroundLocationAction(
            sdkInt = 33,
            hasForeground = true,
            hasBackground = true
        )

        assertEquals(BackgroundLocationAction.NotNeeded, action)
    }

    @Test
    fun api29PlusWithForegroundOnly_requestsBackground() {
        val action = backgroundLocationAction(
            sdkInt = 29,
            hasForeground = true,
            hasBackground = false
        )

        assertEquals(BackgroundLocationAction.RequestPermission, action)
    }
}
