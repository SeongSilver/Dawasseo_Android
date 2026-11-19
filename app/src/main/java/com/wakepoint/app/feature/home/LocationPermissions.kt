package com.wakepoint.app.feature.home

/**
 * 백그라운드 위치 권한을 추가로 요청해야 하는지 결정한다.
 * 위치 판단은 기기 안에서만 이루어지며, 이 결정은 알람 생성을 막지 않는다.
 */
enum class BackgroundLocationAction {
    NotNeeded,
    RequestPermission
}

/**
 * Android 10(API 29) 이상에서 foreground 권한은 있고 background 권한이 없을 때만 요청한다.
 * foreground 권한이 없으면 background 요청은 의미가 없으므로 요청하지 않는다.
 */
fun backgroundLocationAction(
    sdkInt: Int,
    hasForeground: Boolean,
    hasBackground: Boolean
): BackgroundLocationAction {
    if (sdkInt < BACKGROUND_LOCATION_MIN_SDK) return BackgroundLocationAction.NotNeeded
    if (!hasForeground) return BackgroundLocationAction.NotNeeded
    if (hasBackground) return BackgroundLocationAction.NotNeeded
    return BackgroundLocationAction.RequestPermission
}

const val BACKGROUND_LOCATION_MIN_SDK = 29
