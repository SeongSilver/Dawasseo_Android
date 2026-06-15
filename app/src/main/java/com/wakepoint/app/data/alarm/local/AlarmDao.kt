package com.wakepoint.app.data.alarm.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY is_active DESC, label ASC")
    fun observeAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE is_active = 1")
    fun observeActiveAlarms(): Flow<List<AlarmEntity>>

    @Upsert
    suspend fun upsertAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET is_active = 0, triggered_at = :triggeredAt WHERE id = :alarmId")
    suspend fun markTriggered(alarmId: String, triggeredAt: String)
}
