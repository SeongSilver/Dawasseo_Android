package com.wakepoint.app.data.alarm

import com.wakepoint.app.data.alarm.local.AlarmDao
import com.wakepoint.app.data.alarm.local.toDomain
import com.wakepoint.app.data.alarm.local.toEntity
import com.wakepoint.app.domain.model.Alarm
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultAlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {
    override fun observeAlarms(): Flow<List<Alarm>> {
        return alarmDao.observeAlarms().map { alarms ->
            alarms.map { it.toDomain() }
        }
    }

    override fun observeActiveAlarms(): Flow<List<Alarm>> {
        return alarmDao.observeActiveAlarms().map { alarms ->
            alarms.map { it.toDomain() }
        }
    }

    override suspend fun saveAlarm(alarm: Alarm) {
        alarmDao.upsertAlarm(alarm.toEntity())
    }

    override suspend fun markTriggered(alarmId: String, triggeredAt: String) {
        alarmDao.markTriggered(alarmId, triggeredAt)
    }
}
