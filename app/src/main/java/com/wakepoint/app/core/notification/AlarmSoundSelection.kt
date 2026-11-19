package com.wakepoint.app.core.notification

/**
 * 알람에 저장된 커스텀 사운드가 있으면 그것을, 없으면 시스템 기본 알람음을 고른다.
 * 순수 함수로 두어 Android 의존 없이 단위 테스트한다.
 */
fun resolveAlarmSoundUri(
    custom: String?,
    defaultAlarm: String?,
    defaultNotification: String?,
    defaultRingtone: String?
): String? {
    return listOf(custom, defaultAlarm, defaultNotification, defaultRingtone)
        .firstOrNull { !it.isNullOrBlank() }
}

/**
 * 커스텀 사운드 재생에 실패했을 때 쓸 시스템 기본음.
 */
fun resolveFallbackSoundUri(
    defaultAlarm: String?,
    defaultNotification: String?,
    defaultRingtone: String?
): String? {
    return resolveAlarmSoundUri(
        custom = null,
        defaultAlarm = defaultAlarm,
        defaultNotification = defaultNotification,
        defaultRingtone = defaultRingtone
    )
}
