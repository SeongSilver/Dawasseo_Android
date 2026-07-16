package com.wakepoint.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RadiusOptionsTest {

    // --- radiusOptionToKm: 파싱 ---
    @Test
    fun radiusOptionToKm_parsesMeters() {
        assertEquals(0.1, radiusOptionToKm("100m"), 1e-9)
        assertEquals(0.3, radiusOptionToKm("300m"), 1e-9)
        assertEquals(0.5, radiusOptionToKm("500m"), 1e-9)
    }

    @Test
    fun radiusOptionToKm_parsesKilometers() {
        assertEquals(1.0, radiusOptionToKm("1km"), 1e-9)
        assertEquals(3.0, radiusOptionToKm("3km"), 1e-9)
        assertEquals(10.0, radiusOptionToKm("10km"), 1e-9)
    }

    // --- radiusOptionToKm: 클램프 ---
    @Test
    fun radiusOptionToKm_clampsBelowMinToFloor() {
        // 10m 는 GPS 정확도 이하 → 최소 100m 로 클램프
        assertEquals(MIN_RADIUS_KM, radiusOptionToKm("10m"), 1e-9)
        assertEquals(MIN_RADIUS_KM, radiusOptionToKm("50m"), 1e-9)
    }

    @Test
    fun radiusOptionToKm_clampsAboveMaxToCeiling() {
        assertEquals(MAX_RADIUS_KM, radiusOptionToKm("100km"), 1e-9)
    }

    @Test
    fun radiusOptionToKm_fallsBackToDefaultForGarbage() {
        assertEquals(DEFAULT_RADIUS_KM, radiusOptionToKm("abc"), 1e-9)
        assertEquals(DEFAULT_RADIUS_KM, radiusOptionToKm(""), 1e-9)
    }

    @Test
    fun standardOptions_excludeSubGpsRadii() {
        assertFalse("10m 옵션은 GPS 정확도 이하라 제외돼야 함", STANDARD_RADIUS_OPTIONS.contains("10m"))
        assertFalse("50m 옵션은 GPS 정확도 이하라 제외돼야 함", STANDARD_RADIUS_OPTIONS.contains("50m"))
        assertTrue(STANDARD_RADIUS_OPTIONS.contains("100m"))
    }

    // --- accuracyBufferKm ---
    @Test
    fun accuracyBuffer_isZeroWhenNoAccuracy() {
        assertEquals(0.0, accuracyBufferKm(30f, hasAccuracy = false), 1e-9)
    }

    @Test
    fun accuracyBuffer_convertsMetersToKm() {
        assertEquals(0.03, accuracyBufferKm(30f, hasAccuracy = true), 1e-9)
    }

    @Test
    fun accuracyBuffer_capsAtMax() {
        assertEquals(MAX_ACCURACY_BUFFER_KM, accuracyBufferKm(500f, hasAccuracy = true), 1e-9)
    }

    // --- isWithinRadius: 도착 판정 경계 ---
    @Test
    fun isWithinRadius_inclusiveAtBoundary() {
        assertTrue(isWithinRadius(distanceKm = 0.3, radiusKm = 0.3, accuracyBufferKm = 0.0))
    }

    @Test
    fun isWithinRadius_falseJustOutside() {
        assertFalse(isWithinRadius(distanceKm = 0.31, radiusKm = 0.3, accuracyBufferKm = 0.0))
    }

    @Test
    fun isWithinRadius_bufferExtendsReach() {
        assertTrue(isWithinRadius(distanceKm = 0.34, radiusKm = 0.3, accuracyBufferKm = 0.05))
        assertFalse(isWithinRadius(distanceKm = 0.36, radiusKm = 0.3, accuracyBufferKm = 0.05))
    }
}
