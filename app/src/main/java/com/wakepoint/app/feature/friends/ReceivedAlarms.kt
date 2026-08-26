package com.wakepoint.app.feature.friends

import android.content.Context
import com.wakepoint.app.domain.model.Alarm

/**
 * v2 STEP3 (3-7) — 친구가 보낸 알람 수신 감지.
 *
 * 서버 동기화(refreshAlarms)로 내려온 알람 중 "다른 사용자가 만들었고(created_by≠나)
 * 아직 확인 안 한(id∉acknowledged)" 것을 새 수신으로 본다. 푸시 유실이 있어도
 * 동기화만 되면 다이얼로그가 뜨는 풀 기반 감지라 FCM 페이로드 확장이 필요 없다.
 */
object ReceivedAlarmLogic {

    /**
     * 새로 수신된(미확인) 친구 알람. [currentUserId] 미로딩(빈 값)이면 오탐을 막기 위해 빈 목록.
     * created_by 가 빈 레거시 행도 제외한다.
     */
    fun newReceived(
        alarms: List<Alarm>,
        currentUserId: String,
        acknowledgedIds: Set<String>,
    ): List<Alarm> {
        if (currentUserId.isBlank()) return emptyList()
        return alarms.filter {
            it.createdBy.isNotBlank() && it.createdBy != currentUserId && it.id !in acknowledgedIds
        }
    }

    /**
     * 최초 실행(시드 전)에는 기존에 이미 받아 둔 친구 알람 전부를 확인 처리 대상으로 돌려
     * 업데이트 직후 과거 알람 다이얼로그가 쏟아지는 것을 막는다.
     */
    fun idsToSeed(alarms: List<Alarm>, currentUserId: String): Set<String> =
        newReceived(alarms, currentUserId, emptySet()).mapTo(mutableSetOf()) { it.id }
}

private const val RECEIVED_PREFS = "wakepoint_received_alarms"
private const val KEY_ACKNOWLEDGED = "acknowledged_ids"
private const val KEY_SEEDED = "seeded"

fun Context.acknowledgedReceivedAlarmIds(): Set<String> =
    getSharedPreferences(RECEIVED_PREFS, Context.MODE_PRIVATE)
        .getStringSet(KEY_ACKNOWLEDGED, emptySet()) ?: emptySet()

fun Context.acknowledgeReceivedAlarms(ids: Set<String>) {
    if (ids.isEmpty()) return
    val prefs = getSharedPreferences(RECEIVED_PREFS, Context.MODE_PRIVATE)
    prefs.edit()
        .putStringSet(KEY_ACKNOWLEDGED, acknowledgedReceivedAlarmIds() + ids)
        .apply()
}

fun Context.isReceivedAlarmSeeded(): Boolean =
    getSharedPreferences(RECEIVED_PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SEEDED, false)

fun Context.markReceivedAlarmSeeded() {
    getSharedPreferences(RECEIVED_PREFS, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_SEEDED, true).apply()
}
