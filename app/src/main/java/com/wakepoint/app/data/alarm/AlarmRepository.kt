package com.wakepoint.app.data.alarm

import com.wakepoint.app.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun observeAlarms(): Flow<List<Alarm>>
    fun observeActiveAlarms(): Flow<List<Alarm>>

    /** 서버(owner_id = 나)의 알람을 받아 로컬 Room에 반영한다. 친구가 보낸 알람 수신·멀티기기 복원의 핵심. */
    suspend fun refreshAlarms()
    suspend fun saveAlarm(alarm: Alarm)
    suspend fun sendAlarmToFriend(alarm: Alarm)

    /** v2 STEP3 (3-6): 내가 친구에게 보낸 알람(원격 소유자=친구) 목록. */
    suspend fun fetchSentAlarms(): List<Alarm>

    /** v2 STEP3 (3-6): 아직 서버에 못 올라간 친구 알람 대기분(오프라인 등 — 재전송 예정). */
    suspend fun pendingSentAlarms(): List<Alarm>

    /** v2 STEP3 (3-6): 보낸 알람 취소 — 원격에서 삭제한다(멱등). */
    suspend fun cancelSentAlarm(alarmId: String)
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun setAlarmActive(alarmId: String, isActive: Boolean)
    suspend fun setFavorite(alarmId: String, isFavorite: Boolean)
    suspend fun deleteAlarm(alarmId: String)
    suspend fun syncLocationTracking()
    suspend fun handleTriggeredAlarm(alarmId: String)
}
