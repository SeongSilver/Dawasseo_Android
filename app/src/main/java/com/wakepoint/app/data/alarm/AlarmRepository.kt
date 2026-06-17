package com.wakepoint.app.data.alarm

import com.wakepoint.app.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun observeAlarms(): Flow<List<Alarm>>
    fun observeActiveAlarms(): Flow<List<Alarm>>
    suspend fun saveAlarm(alarm: Alarm)
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun setAlarmActive(alarmId: String, isActive: Boolean)
    suspend fun deleteAlarm(alarmId: String)
    suspend fun syncLocationTracking()
    suspend fun markTriggered(alarmId: String, triggeredAt: String)
}
