package com.wakepoint.app.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.wakepoint.app.service.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTrackingController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun syncTracking(activeAlarmCount: Int) {
        if (activeAlarmCount > 0 && hasForegroundLocationPermission()) {
            startTracking(activeAlarmCount)
        } else {
            stopTracking()
        }
    }

    private fun startTracking(activeAlarmCount: Int) {
        val intent = LocationTrackingService.startIntent(context, activeAlarmCount)
        // 백그라운드/부팅 시점에는 OS 정책상 포그라운드 서비스 시작이 거부될 수 있으므로 방어적으로 처리.
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private fun stopTracking() {
        context.stopService(LocationTrackingService.startIntent(context, activeAlarmCount = 0))
    }

    private fun hasForegroundLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }
}
