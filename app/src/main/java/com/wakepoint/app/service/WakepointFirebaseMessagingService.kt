package com.wakepoint.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WakepointFirebaseMessagingService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
