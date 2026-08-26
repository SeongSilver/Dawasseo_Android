package com.wakepoint.app.data.timealarm

import com.wakepoint.app.domain.model.TimeAlarm
import kotlinx.coroutines.flow.Flow

/**
 * 시간 알람 로컬 저장소. 위치 알람([com.wakepoint.app.data.alarm.AlarmRepository])과
 * 완전히 독립적이며 원격 동기화가 없다(기기 로컬 전용).
 */
interface TimeAlarmRepository {
    fun observeTimeAlarms(): Flow<List<TimeAlarm>>
    suspend fun getTimeAlarm(id: String): TimeAlarm?
    /** 활성 상태인 시간 알람 목록(부팅 후 재예약용). */
    suspend fun getEnabledTimeAlarms(): List<TimeAlarm>
    suspend fun saveTimeAlarm(alarm: TimeAlarm)
    suspend fun setEnabled(id: String, enabled: Boolean)
    suspend fun deleteTimeAlarm(id: String)
}
