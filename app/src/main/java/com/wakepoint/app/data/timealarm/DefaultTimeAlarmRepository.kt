package com.wakepoint.app.data.timealarm

import com.wakepoint.app.core.alarm.TimeAlarmScheduler
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
    private val scheduler: TimeAlarmScheduler,
) : TimeAlarmRepository {

    override fun observeTimeAlarms(): Flow<List<TimeAlarm>> =
        timeAlarmDao.observeTimeAlarms().map { list -> list.map { it.toDomain() } }

    override suspend fun getTimeAlarm(id: String): TimeAlarm? =
        timeAlarmDao.get(id)?.toDomain()

    override suspend fun getEnabledTimeAlarms(): List<TimeAlarm> =
        timeAlarmDao.getEnabled().map { it.toDomain() }

    override suspend fun saveTimeAlarm(alarm: TimeAlarm) {
        timeAlarmDao.upsert(alarm.toEntity())
        // 저장과 동시에 OS 예약을 갱신(비활성이면 스케줄러가 해제).
        scheduler.schedule(alarm)
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) {
        timeAlarmDao.setEnabled(id, enabled)
        val alarm = timeAlarmDao.get(id)?.toDomain() ?: return
        if (enabled) scheduler.schedule(alarm) else scheduler.cancel(id)
    }

    override suspend fun deleteTimeAlarm(id: String) {
        scheduler.cancel(id)
        timeAlarmDao.delete(id)
    }
}
