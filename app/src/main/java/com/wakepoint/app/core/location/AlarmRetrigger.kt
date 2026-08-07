package com.wakepoint.app.core.location

/** 위치 업데이트 시 알람 하나에 대해 취할 동작. */
enum class AlarmTriggerAction {
    /** 반경에 처음 진입 → 알람 발동. */
    FIRE,

    /** 계속 유지(반복) 알람이 반경을 벗어남 → 재무장(다음 재진입 시 다시 발동). */
    REARM,

    /** 아무 것도 하지 않음(이미 발동 중·반경 밖 대기 등). */
    NONE
}

/**
 * 위치 업데이트마다 알람 하나에 대해 무엇을 할지 결정하는 순수 로직.
 *
 * - 반경 진입 & 아직 발동 전 → FIRE
 * - 반경 이탈 & 반복 알람 & 발동 상태 → REARM (다음 재진입 때 다시 울림)
 * - 그 외(이미 발동 중 반경 안, 비반복 알람의 반경 밖 등) → NONE
 *
 * @param reached 현재 위치가 알람 반경 안인지.
 * @param repeating 계속 유지(반복) 알람인지.
 * @param currentlyTriggering 이미 발동되어 아직 반경을 벗어나지 않은 상태인지.
 */
fun decideAlarmTriggerAction(
    reached: Boolean,
    repeating: Boolean,
    currentlyTriggering: Boolean
): AlarmTriggerAction = when {
    reached && !currentlyTriggering -> AlarmTriggerAction.FIRE
    !reached && repeating && currentlyTriggering -> AlarmTriggerAction.REARM
    else -> AlarmTriggerAction.NONE
}
