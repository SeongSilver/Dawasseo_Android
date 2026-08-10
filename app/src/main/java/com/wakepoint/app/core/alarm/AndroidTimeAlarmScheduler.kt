package com.wakepoint.app.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.wakepoint.app.domain.model.TimeAlarm
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AlarmManager 기반 시간 알람 스케줄러.
 *
 * 발화 시각은 [TimeAlarmSchedule.nextTriggerMillis] 로 계산하고, `setExactAndAllowWhileIdle` 로
 * 절전(Doze) 중에도 정확히 울리도록 예약한다. 정확 알람이 허용되지 않은 기기(API 31/32 미허용)에서는
 * 근사 예약으로 폴백해 SecurityException 을 피한다.
 */
@Singleton
class AndroidTimeAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : TimeAlarmScheduler {

    private val alarmManager: AlarmManager? get() = context.getSystemService()

    override fun schedule(alarm: TimeAlarm) {
        val manager = alarmManager ?: return
        if (!alarm.enabled) {
            cancel(alarm.id)
            return
        }
        val triggerAt = TimeAlarmSchedule.nextTriggerMillis(
            nowMillis = System.currentTimeMillis(),
            zone = ZoneId.systemDefault(),
            hour = alarm.hour,
            minute = alarm.minute,
            repeatMode = alarm.repeatMode,
            customDays = alarm.repeatDays,
        )
        if (triggerAt == null) {
            cancel(alarm.id)
            return
        }
        val pendingIntent = firePendingIntent(alarm.id)
        try {
            if (canScheduleExact()) {
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: SecurityException) {
            // 정확 알람 권한이 런타임에 취소된 경우 근사 예약으로 폴백.
            Log.w(TAG, "정확 알람 예약 실패, 근사 예약으로 폴백", e)
            alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    override fun cancel(alarmId: String) {
        alarmManager?.cancel(firePendingIntent(alarmId))
    }

    override fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            (alarmManager?.canScheduleExactAlarms() == true)

    private fun firePendingIntent(alarmId: String): PendingIntent {
        val intent = Intent(context, TimeAlarmReceiver::class.java).apply {
            action = TimeAlarmReceiver.ACTION_FIRE
            putExtra(TimeAlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val TAG = "TimeAlarmScheduler"
    }
}
