package com.wakepoint.app.feature.v2.timealarm

/** v2 STEP1 — 시간 알람 반복 모드. */
enum class RepeatMode(val label: String) {
    NONE("안 함"),
    DAILY("매일"),
    WEEKDAY("평일"),
    WEEKEND("주말"),
    CUSTOM("요일 직접"),
}

/**
 * 시간 알람 생성 화면의 순수 로직(피커/뷰모델과 분리 — 후속 TDD 대상).
 * UI는 이 함수들의 결과만 반영한다.
 */
object TimeAlarmLogic {

    /** 반복(NONE 외)을 고르면 날짜 선택은 잠기고 시각만 적용된다(명세 1c). */
    fun isDateLocked(mode: RepeatMode): Boolean = mode != RepeatMode.NONE

    /**
     * [알람 추가] 버튼 활성 조건.
     * - 과거 시각이면 비활성(명세 1c: "이미 지난 시각이에요").
     * - "요일 직접"인데 선택 요일이 없으면 비활성(최소 구현 — 명세엔 미명시, decisions 기록).
     */
    fun isAddEnabled(timeIsPast: Boolean, mode: RepeatMode, customDays: Set<Int>): Boolean =
        !timeIsPast && (mode != RepeatMode.CUSTOM || customDays.isNotEmpty())
}
