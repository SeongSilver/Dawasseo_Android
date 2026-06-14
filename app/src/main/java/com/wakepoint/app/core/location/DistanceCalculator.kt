package com.wakepoint.app.core.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0
const val MIN_RADIUS_KM = 0.1
const val MAX_RADIUS_KM = 50.0

fun calculateDistance(
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double
): Double {
    val latDistance = Math.toRadians(endLat - startLat)
    val lngDistance = Math.toRadians(endLng - startLng)
    val a = sin(latDistance / 2) * sin(latDistance / 2) +
        cos(Math.toRadians(startLat)) *
        cos(Math.toRadians(endLat)) *
        sin(lngDistance / 2) *
        sin(lngDistance / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_KM * c
}
