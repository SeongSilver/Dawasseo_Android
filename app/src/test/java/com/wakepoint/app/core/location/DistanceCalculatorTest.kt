package com.wakepoint.app.core.location

import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {
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
