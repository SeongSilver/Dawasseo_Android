package com.wakepoint.app.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.wakepoint.app.core.location.calculateDistance
import com.wakepoint.app.core.location.calculateDistanceToSegment
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
    private var isLocationUpdatesStarted = false
    private var previousLocation: Location? = null
    private val triggeringAlarmIds = mutableSetOf<String>()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach(::handleLocation)
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

        val activeAlarmCount = intent?.getIntExtra(EXTRA_ACTIVE_ALARM_COUNT, 0) ?: 0
        updateTrackingNotification(activeAlarmCount)
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
                triggeringAlarmIds.retainAll(alarms.map { it.id }.toSet())
                if (alarms.isEmpty()) {
                    stopSelf()
                } else {
                    updateTrackingNotification(alarms.size)
                }
            }
        }
    }

    private fun updateTrackingNotification(activeAlarmCount: Int) {
        startForeground(
            AlarmNotificationManager.TRACKING_NOTIFICATION_ID,
            alarmNotificationManager.buildTrackingNotification(activeAlarmCount)
        )
    }

    private fun startLocationUpdates() {
        if (!hasForegroundLocationPermission() || isLocationUpdatesStarted) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MILLIS
        )
            .setMinUpdateIntervalMillis(LOCATION_MIN_INTERVAL_MILLIS)
            .setMaxUpdateDelayMillis(LOCATION_MAX_DELAY_MILLIS)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setWaitForAccurateLocation(false)
            .build()

        runCatching {
            locationClient.requestLocationUpdates(
                request,
                locationCallback,
                mainLooper
            )
        }.onSuccess {
            isLocationUpdatesStarted = true
        }
    }

    private fun handleLocation(location: Location) {
        val previous = previousLocation
        previousLocation = location

        val triggeredAlarms = activeAlarms.filter { alarm ->
            isAlarmReached(
                alarm = alarm,
                currentLocation = location,
                previousLocation = previous
            )
        }

        triggeredAlarms.forEach { alarm ->
            if (!triggeringAlarmIds.add(alarm.id)) return@forEach

            serviceScope.launch {
                runCatching {
                    alarmNotificationManager.showArrivalAlarm(alarm)
                    alarmRepository.markTriggered(
                        alarmId = alarm.id,
                        triggeredAt = Instant.now().toString()
                    )
                }.onFailure {
                    triggeringAlarmIds.remove(alarm.id)
                }
            }
        }
    }

    private fun isAlarmReached(
        alarm: Alarm,
        currentLocation: Location,
        previousLocation: Location?
    ): Boolean {
        val effectiveRadiusKm = alarm.radiusKm + currentLocation.accuracyBufferKm()
        val currentDistanceKm = calculateDistance(
            startLat = currentLocation.latitude,
            startLng = currentLocation.longitude,
            endLat = alarm.targetLat,
            endLng = alarm.targetLng
        )
        if (currentDistanceKm <= effectiveRadiusKm) return true

        if (previousLocation == null || !canUseSegment(previousLocation, currentLocation)) {
            return false
        }

        return calculateDistanceToSegment(
            segmentStartLat = previousLocation.latitude,
            segmentStartLng = previousLocation.longitude,
            segmentEndLat = currentLocation.latitude,
            segmentEndLng = currentLocation.longitude,
            targetLat = alarm.targetLat,
            targetLng = alarm.targetLng
        ) <= effectiveRadiusKm
    }

    private fun canUseSegment(previousLocation: Location, currentLocation: Location): Boolean {
        val elapsedSeconds = when {
            previousLocation.elapsedRealtimeNanos > 0L && currentLocation.elapsedRealtimeNanos > 0L ->
                kotlin.math.abs(currentLocation.elapsedRealtimeNanos - previousLocation.elapsedRealtimeNanos) / 1_000_000_000L
            previousLocation.time > 0L && currentLocation.time > 0L ->
                kotlin.math.abs(currentLocation.time - previousLocation.time) / 1_000L
            else -> 0L
        }
        if (elapsedSeconds > MAX_SEGMENT_GAP_SECONDS) return false

        val segmentLengthKm = calculateDistance(
            startLat = previousLocation.latitude,
            startLng = previousLocation.longitude,
            endLat = currentLocation.latitude,
            endLng = currentLocation.longitude
        )
        return segmentLengthKm <= MAX_SEGMENT_DISTANCE_KM
    }

    private fun Location.accuracyBufferKm(): Double {
        if (!hasAccuracy()) return 0.0
        return (accuracy / 1000.0).coerceAtMost(MAX_ACCURACY_BUFFER_KM)
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
        private const val LOCATION_INTERVAL_MILLIS = 5_000L
        private const val LOCATION_MIN_INTERVAL_MILLIS = 2_000L
        private const val LOCATION_MAX_DELAY_MILLIS = 5_000L
        private const val MAX_SEGMENT_GAP_SECONDS = 300L
        private const val MAX_SEGMENT_DISTANCE_KM = 10.0
        private const val MAX_ACCURACY_BUFFER_KM = 0.05

        private const val EXTRA_ACTIVE_ALARM_COUNT = "extra_active_alarm_count"

        fun startIntent(
            context: android.content.Context,
            activeAlarmCount: Int = 0
        ): Intent {
            return Intent(context, LocationTrackingService::class.java).apply {
                putExtra(EXTRA_ACTIVE_ALARM_COUNT, activeAlarmCount)
            }
        }
    }
}
