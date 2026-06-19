package com.wakepoint.app.core.location

import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
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

fun calculateDistanceToSegment(
    segmentStartLat: Double,
    segmentStartLng: Double,
    segmentEndLat: Double,
    segmentEndLng: Double,
    targetLat: Double,
    targetLng: Double
): Double {
    val segmentLengthKm = calculateDistance(
        startLat = segmentStartLat,
        startLng = segmentStartLng,
        endLat = segmentEndLat,
        endLng = segmentEndLng
    )
    if (segmentLengthKm == 0.0) {
        return calculateDistance(
            startLat = segmentStartLat,
            startLng = segmentStartLng,
            endLat = targetLat,
            endLng = targetLng
        )
    }

    val latScaleKm = EARTH_RADIUS_KM * PI / 180.0
    val lngScaleKm = latScaleKm * cos(Math.toRadians((segmentStartLat + segmentEndLat + targetLat) / 3.0))
    val startX = (segmentStartLng - targetLng) * lngScaleKm
    val startY = (segmentStartLat - targetLat) * latScaleKm
    val endX = (segmentEndLng - targetLng) * lngScaleKm
    val endY = (segmentEndLat - targetLat) * latScaleKm

    val deltaX = endX - startX
    val deltaY = endY - startY
    val projection = -(startX * deltaX + startY * deltaY) / (deltaX * deltaX + deltaY * deltaY)
    val clampedProjection = min(1.0, max(0.0, projection))
    val closestX = startX + clampedProjection * deltaX
    val closestY = startY + clampedProjection * deltaY

    return sqrt(closestX * closestX + closestY * closestY)
}
