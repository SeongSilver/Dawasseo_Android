package com.wakepoint.app.feature.friends

import com.wakepoint.app.feature.home.MAX_ALARM_RADIUS_KM
import com.wakepoint.app.feature.home.MIN_ALARM_RADIUS_KM

/** "500m" / "1km" 같은 반경 옵션 문자열을 km 값으로 바꾼다. 파싱 실패 시 0.5km. */
fun radiusOptionToKm(option: String): Double {
    val trimmed = option.trim()
    val radiusKm = when {
        trimmed.endsWith("km") -> trimmed.removeSuffix("km").toDoubleOrNull() ?: DEFAULT_RADIUS_KM
        trimmed.endsWith("m") -> trimmed.removeSuffix("m").toDoubleOrNull()?.div(1000.0) ?: DEFAULT_RADIUS_KM
        else -> DEFAULT_RADIUS_KM
    }
    return radiusKm.coerceIn(MIN_ALARM_RADIUS_KM, MAX_ALARM_RADIUS_KM)
}

/** km 값을 "500m" / "1km" 형태의 표시 문자열로 바꾼다. */
fun radiusKmToOption(radiusKm: Double): String {
    return if (radiusKm < 1.0) {
        "${(radiusKm * 1000).toInt()}m"
    } else {
        "${radiusKm.toInt()}km"
    }
}

private const val DEFAULT_RADIUS_KM = 0.5
