package com.wakepoint.app.core.location

/**
 * 알람 반경(옵션 문자열 ↔ km)과 도착 판정에 대한 단일 진실 공급원.
 * 기존에 HomeViewModel/AlarmsViewModel/FriendsViewModel 에 흩어져 중복됐던 파싱·클램프를 통합한다.
 */

/** GPS 정확도 버퍼(최대 50m)보다 작은 반경은 사실상 울리지 않으므로, 최소 반경을 100m로 둔다. */
const val MIN_RADIUS_KM = 0.1
const val MAX_RADIUS_KM = 50.0
const val DEFAULT_RADIUS_KM = 0.5

/** 도착 판정 시 GPS 정확도로 더해줄 수 있는 버퍼 상한(km). */
const val MAX_ACCURACY_BUFFER_KM = 0.05

/**
 * 선택 가능한 반경 옵션. 10m·50m 는 GPS 정확도 버퍼(최대 50m) 이하라 실사용에서
 * 도착 판정이 안 되므로 제외하고, 신뢰 가능한 100m 이상만 제공한다.
 */
val STANDARD_RADIUS_OPTIONS = listOf("100m", "300m", "500m", "1km", "3km", "10km")

/** "300m"/"1km" 같은 옵션 문자열을 km 로 파싱하고 [MIN_RADIUS_KM]~[MAX_RADIUS_KM] 로 클램프한다. */
fun radiusOptionToKm(option: String): Double {
    val trimmed = option.trim().lowercase()
    val km = when {
        trimmed.endsWith("km") -> trimmed.removeSuffix("km").trim().toDoubleOrNull()
        trimmed.endsWith("m") -> trimmed.removeSuffix("m").trim().toDoubleOrNull()?.div(1000.0)
        else -> null
    } ?: DEFAULT_RADIUS_KM
    return km.coerceIn(MIN_RADIUS_KM, MAX_RADIUS_KM)
}

/** GPS 정확도(미터)를 도착 판정용 버퍼(km)로 변환. 정확도 정보가 없으면 0. */
fun accuracyBufferKm(accuracyMeters: Float, hasAccuracy: Boolean): Double {
    if (!hasAccuracy) return 0.0
    return (accuracyMeters / 1000.0).coerceAtMost(MAX_ACCURACY_BUFFER_KM)
}

/** 현재 거리(km)가 반경 + 버퍼 이내면 도착으로 판정한다. (순수 함수 — 단위 테스트 대상) */
fun isWithinRadius(distanceKm: Double, radiusKm: Double, accuracyBufferKm: Double = 0.0): Boolean {
    return distanceKm <= radiusKm + accuracyBufferKm
}
