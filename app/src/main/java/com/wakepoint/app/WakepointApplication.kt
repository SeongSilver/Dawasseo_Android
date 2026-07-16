package com.wakepoint.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wakepoint.app.core.notification.AlarmNotificationManager
import com.wakepoint.app.service.AlarmWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class WakepointApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var alarmNotificationManager: AlarmNotificationManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        alarmNotificationManager.createChannels()
        schedulePeriodicAlarmSync()
    }

    /** 네트워크 연결 시 주기적으로 서버 알람을 로컬에 반영(FCM·resume 새로고침의 백업 경로). */
    private fun schedulePeriodicAlarmSync() {
        val request = PeriodicWorkRequestBuilder<AlarmWorker>(
            ALARM_SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        runCatching {
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                ALARM_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private companion object {
        const val ALARM_SYNC_WORK_NAME = "alarm-periodic-sync"
        const val ALARM_SYNC_INTERVAL_MINUTES = 30L
    }
}
