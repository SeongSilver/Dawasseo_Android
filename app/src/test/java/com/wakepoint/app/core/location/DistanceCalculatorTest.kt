package com.wakepoint.app.core.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {
    @Test
    fun distance_isZeroForSamePoint() {
        val distanceKm = calculateDistance(37.5665, 126.9780, 37.5665, 126.9780)
        assertEquals(0.0, distanceKm, 1e-9)
    }

    @Test
    fun distance_oneDegreeOfLongitudeAtEquator_isAbout111km() {
        val distanceKm = calculateDistance(0.0, 0.0, 0.0, 1.0)
        assertTrue("expected ~111.3km but was $distanceKm", distanceKm in 111.0..111.7)
    }

    @Test
    fun distance_shortHopMatchesLatitudeScale() {
        // 0.01도 위도 ≈ 1.11km
        val distanceKm = calculateDistance(37.5665, 126.9780, 37.5765, 126.9780)
        assertTrue("expected ~1.11km but was $distanceKm", distanceKm in 1.05..1.15)
    }

    @Test
    fun distanceToSegment_detectsTargetBetweenTwoLocations() {
        val distanceKm = calculateDistanceToSegment(
            segmentStartLat = 37.0,
            segmentStartLng = 127.0,
            segmentEndLat = 37.002,
            segmentEndLng = 127.0,
            targetLat = 37.001,
            targetLng = 127.0
        )

        assertTrue(distanceKm < 0.01)
    }

    @Test
    fun distanceToSegment_usesNearestPointWhenTargetIsBesideRoute() {
        val distanceKm = calculateDistanceToSegment(
            segmentStartLat = 37.0,
            segmentStartLng = 127.0,
            segmentEndLat = 37.002,
            segmentEndLng = 127.0,
            targetLat = 37.001,
            targetLng = 127.001
        )

        assertTrue(distanceKm in 0.08..0.10)
    }
}
