package com.wakepoint.app.core.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wakepoint.app.core.alarm.TimeAlarmScheduler
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.timealarm.TimeAlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 재부팅(BOOT_COMPLETED)이나 앱 업데이트(MY_PACKAGE_REPLACED) 후, 활성 알람이 있으면
 * 위치 추적을 다시 시작한다.
 *
 * 기존에는 홈 화면이 위치 권한을 감지할 때만 추적이 시작돼서, 재부팅하거나 OS가
 * 프로세스를 종료하면 사용자가 앱을 다시 열기 전까지 도착 알람이 울리지 않았다.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var timeAlarmRepository: TimeAlarmRepository

    @Inject
    lateinit var timeAlarmScheduler: TimeAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        // onReceive 는 짧게 끝나야 하므로 goAsync 로 시간을 확보하고 백그라운드에서 처리.
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope.launch {
            runCatching { alarmRepository.syncLocationTracking() }
                .onFailure { Log.w(TAG, "부팅 후 위치 추적 재개 실패", it) }
            // OS 재부팅 시 AlarmManager 예약이 모두 사라지므로 활성 시간 알람을 다시 예약한다.
            runCatching {
                timeAlarmRepository.getEnabledTimeAlarms().forEach { timeAlarmScheduler.schedule(it) }
            }.onFailure { Log.w(TAG, "부팅 후 시간 알람 재예약 실패", it) }
            pendingResult.finish()
        }
    }

    private companion object {
        const val TAG = "BootCompletedReceiver"
    }
}
