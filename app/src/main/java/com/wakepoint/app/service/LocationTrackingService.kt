package com.wakepoint.app.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.wakepoint.app.core.location.calculateDistance
import com.wakepoint.app.core.notification.AlarmNotificationManager
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.domain.model.Alarm
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant

@AndroidEntryPoint
class LocationTrackingService : Service() {
    @Inject
    lateinit var alarmNotificationManager: AlarmNotificationManager

    @Inject
    lateinit var alarmRepository: AlarmRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var locationClient: FusedLocationProviderClient
    private var activeAlarms: List<Alarm> = emptyList()
    private var observeJob: Job? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val triggeredAlarms = activeAlarms.filter { alarm ->
                calculateDistance(
                    startLat = location.latitude,
                    startLng = location.longitude,
                    endLat = alarm.targetLat,
                    endLng = alarm.targetLng
                ) <= alarm.radiusKm
            }

            triggeredAlarms.forEach { alarm ->
                serviceScope.launch {
                    alarmNotificationManager.showArrivalAlarm(alarm)
                    alarmRepository.markTriggered(
                        alarmId = alarm.id,
                        triggeredAt = Instant.now().toString()
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        alarmNotificationManager.createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasForegroundLocationPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(
            AlarmNotificationManager.TRACKING_NOTIFICATION_ID,
            alarmNotificationManager.buildTrackingNotification(activeAlarmCount = 0)
        )
        observeActiveAlarms()
        startLocationUpdates()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        observeJob?.cancel()
        locationClient.removeLocationUpdates(locationCallback)
        serviceScope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    private fun observeActiveAlarms() {
        if (observeJob != null) return

        observeJob = serviceScope.launch {
            alarmRepository.observeActiveAlarms().collectLatest { alarms ->
                activeAlarms = alarms
                if (alarms.isEmpty()) {
                    stopSelf()
                } else {
                    startForeground(
                        AlarmNotificationManager.TRACKING_NOTIFICATION_ID,
                        alarmNotificationManager.buildTrackingNotification(alarms.size)
                    )
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (!hasForegroundLocationPermission()) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            LOCATION_INTERVAL_MILLIS
        )
            .setMinUpdateIntervalMillis(LOCATION_MIN_INTERVAL_MILLIS)
            .build()

        runCatching {
            locationClient.requestLocationUpdates(
                request,
                locationCallback,
                mainLooper
            )
        }
    }

    private fun hasForegroundLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val LOCATION_INTERVAL_MILLIS = 60_000L
        private const val LOCATION_MIN_INTERVAL_MILLIS = 30_000L

        fun startIntent(context: android.content.Context): Intent {
            return Intent(context, LocationTrackingService::class.java)
        }
    }
}
