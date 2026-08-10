package com.wakepoint.app.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.wakepoint.app.data.timealarm.TimeAlarmRepository
import com.wakepoint.app.service.AlarmAlertService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 시간 알람 발화 수신기. AlarmManager 가 예약 시각에 이 리시버로 브로드캐스트한다.
 *
 * 최신 상태를 저장소에서 다시 읽어(활성 여부 확인) 전체화면 알람을 띄우고,
 * 반복 알람이면 다음 발화를 재예약, 일회성이면 비활성화한다.
 */
@AndroidEntryPoint
class TimeAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TimeAlarmRepository

    @Inject
    lateinit var scheduler: TimeAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope.launch {
            runCatching {
                val alarm = repository.getTimeAlarm(alarmId) ?: return@runCatching
                if (!alarm.enabled) return@runCatching

                val serviceIntent = AlarmAlertService.startIntent(
                    context = context,
                    alarmId = alarm.id,
                    label = alarm.label.ifBlank { DEFAULT_LABEL },
                    targetAddress = "",
                    soundUri = alarm.soundUri,
                    soundEnabled = alarm.soundEnabled,
                    vibrate = alarm.vibrate,
                )
                ContextCompat.startForegroundService(context, serviceIntent)

                if (alarm.repeatMode == RepeatMode.NONE) {
                    // 일회성: 울린 뒤 비활성화해 목록에 남긴다(위치 알람과 동일 정책).
                    repository.setEnabled(alarm.id, false)
                } else {
                    // 반복: 지금(=발화 시각) 기준 다음 발화를 재예약.
                    scheduler.schedule(alarm)
                }
            }.onFailure { Log.w(TAG, "시간 알람 발화 처리 실패: $alarmId", it) }
            pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_FIRE = "com.wakepoint.app.action.TIME_ALARM_FIRE"
        const val EXTRA_ALARM_ID = "extra_time_alarm_id"
        private const val DEFAULT_LABEL = "시간 알람"
        private const val TAG = "TimeAlarmReceiver"
    }
}
