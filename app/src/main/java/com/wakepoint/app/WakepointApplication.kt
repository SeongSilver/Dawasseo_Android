package com.wakepoint.app

import android.app.Application
import com.wakepoint.app.core.notification.AlarmNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WakepointApplication : Application() {
    @Inject
    lateinit var alarmNotificationManager: AlarmNotificationManager

    override fun onCreate() {
        super.onCreate()
        alarmNotificationManager.createChannels()
    }
}
