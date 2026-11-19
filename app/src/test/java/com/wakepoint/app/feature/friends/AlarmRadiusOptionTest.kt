package com.wakepoint.app.feature.friends

import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmRadiusOptionTest {
    @Test
    fun radiusOptionToKm_parsesMetersAndKilometers() {
        assertEquals(0.5, radiusOptionToKm("500m"), 0.0001)
        assertEquals(0.01, radiusOptionToKm("10m"), 0.0001)
        assertEquals(3.0, radiusOptionToKm("3km"), 0.0001)
    }

    @Test
    fun radiusOptionToKm_clampsToAllowedRange() {
        assertEquals(0.01, radiusOptionToKm("1m"), 0.0001)
        assertEquals(50.0, radiusOptionToKm("999km"), 0.0001)
    }

    @Test
    fun radiusOptionToKm_fallsBackOnGarbage() {
        assertEquals(0.5, radiusOptionToKm("far"), 0.0001)
        assertEquals(0.5, radiusOptionToKm(""), 0.0001)
    }

    @Test
    fun radiusKmToOption_formatsForDisplay() {
        assertEquals("500m", radiusKmToOption(0.5))
        assertEquals("10m", radiusKmToOption(0.01))
        assertEquals("1km", radiusKmToOption(1.0))
        assertEquals("10km", radiusKmToOption(10.0))
    }
}
