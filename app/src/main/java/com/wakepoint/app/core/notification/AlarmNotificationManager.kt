package com.wakepoint.app.core.notification

import android.content.Context

class AlarmNotificationManager(
    private val context: Context
) {
    fun createChannels() {
        // Notification channel creation will be wired when FCM/alarm playback is implemented.
        context.applicationContext
    }
}
