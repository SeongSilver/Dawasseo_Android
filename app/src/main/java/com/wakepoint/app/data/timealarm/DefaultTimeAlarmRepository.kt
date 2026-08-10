package com.wakepoint.app.data.timealarm

import com.wakepoint.app.data.timealarm.local.TimeAlarmDao
import com.wakepoint.app.data.timealarm.local.toDomain
import com.wakepoint.app.data.timealarm.local.toEntity
import com.wakepoint.app.domain.model.TimeAlarm
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultTimeAlarmRepository @Inject constructor(
    private val timeAlarmDao: TimeAlarmDao,
) : TimeAlarmRepository {

    override fun observeTimeAlarms(): Flow<List<TimeAlarm>> =
        timeAlarmDao.observeTimeAlarms().map { list -> list.map { it.toDomain() } }

    override suspend fun getTimeAlarm(id: String): TimeAlarm? =
        timeAlarmDao.get(id)?.toDomain()

    override suspend fun saveTimeAlarm(alarm: TimeAlarm) {
        timeAlarmDao.upsert(alarm.toEntity())
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) {
        timeAlarmDao.setEnabled(id, enabled)
    }

    override suspend fun deleteTimeAlarm(id: String) {
        timeAlarmDao.delete(id)
    }
}
