package com.wakepoint.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.wakepoint.app.core.notification.AlarmNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {
    @Inject
    lateinit var alarmNotificationManager: AlarmNotificationManager

    override fun onCreate() {
        super.onCreate()
        alarmNotificationManager.createChannels()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
