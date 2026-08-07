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

    @Query("SELECT COUNT(*) FROM alarms WHERE is_active = 1")
    suspend fun countActiveAlarms(): Int

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :alarmId LIMIT 1")
    suspend fun getAlarm(alarmId: String): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE pending_sync = 1")
    suspend fun pendingSyncAlarms(): List<AlarmEntity>

    @Upsert
    suspend fun upsertAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET pending_sync = :pending WHERE id = :alarmId")
    suspend fun setPendingSync(alarmId: String, pending: Boolean)

    // 서버에서 사라진 알람만 제거하되, 아직 동기화되지 않은(pending) 로컬 전용 알람은 보존한다.
    @Query("DELETE FROM alarms WHERE id NOT IN (:ids) AND pending_sync = 0")
    suspend fun deleteSyncedAlarmsNotIn(ids: List<String>)

    @Query("DELETE FROM alarms WHERE pending_sync = 0")
    suspend fun deleteSyncedAlarms()

    @Query(
        """
        UPDATE alarms
        SET label = :label,
            radius_km = :radiusKm,
            sound_type = :soundType,
            sound_uri = :soundUri,
            vibrate = :vibrate,
            repeating = :repeating
        WHERE id = :alarmId
        """
    )
    suspend fun updateAlarmSettings(
        alarmId: String,
        label: String,
        radiusKm: Double,
        soundType: String,
        soundUri: String?,
        vibrate: Boolean,
        repeating: Boolean
    )

    // is_active 를 로컬에서 바꿀 때는 동기화 대기(pending_sync=1)로 함께 표시한다.
    // 이렇게 해야 아직 서버에 반영되기 전, refresh 병합이 서버의 옛 값(예: is_active=1)으로
    // 이 변경을 되돌리지 못한다. "현재 위치에서 껐는데 다시 켜지는" 재발동 루프의 핵심 방어.
    @Query("UPDATE alarms SET is_active = :isActive, pending_sync = 1 WHERE id = :alarmId")
    suspend fun setAlarmActive(alarmId: String, isActive: Boolean)

    @Query("UPDATE alarms SET is_favorite = :isFavorite WHERE id = :alarmId")
    suspend fun setFavorite(alarmId: String, isFavorite: Boolean)

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteAlarm(alarmId: String)
}
