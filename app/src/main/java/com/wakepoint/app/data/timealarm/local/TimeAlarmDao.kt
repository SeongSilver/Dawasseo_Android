package com.wakepoint.app.data.timealarm.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeAlarmDao {
    @Query("SELECT * FROM time_alarms ORDER BY hour ASC, minute ASC")
    fun observeTimeAlarms(): Flow<List<TimeAlarmEntity>>

    @Query("SELECT * FROM time_alarms")
    suspend fun getAll(): List<TimeAlarmEntity>

    @Query("SELECT * FROM time_alarms WHERE enabled = 1")
    suspend fun getEnabled(): List<TimeAlarmEntity>

    @Query("SELECT * FROM time_alarms WHERE id = :id LIMIT 1")
    suspend fun get(id: String): TimeAlarmEntity?

    @Upsert
    suspend fun upsert(entity: TimeAlarmEntity)

    @Query("UPDATE time_alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("DELETE FROM time_alarms WHERE id = :id")
    suspend fun delete(id: String)
}
