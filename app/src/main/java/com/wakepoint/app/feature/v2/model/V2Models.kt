package com.wakepoint.app.feature.v2.model

/**
 * v2 화면용 UI 모델(표현 전용). 영속성/도메인 레이어와 분리 — 기존 data/domain 미수정.
 * 실제 연동 시 도메인 모델 → 이 UI 모델로 매핑하는 계층을 추가한다(후속).
 */

/** 알람 종류 — 목록에서 아이콘+라벨로 구분(명세 1d). */
enum class AlarmKind(val label: String) {
    LOCATION("위치"),
    TIME("시간"),
}

/** 알람 목록 아이템(즐겨찾기/활성/지난 섹션 공통). */
data class V2AlarmItem(
    val name: String,
    val kind: AlarmKind,
    val meta: String,
    val enabled: Boolean = true,
    val favorite: Boolean = false,
    /** 친구가 설정한 알람이면 설정자 이름(예: "지수" → "지수님이 설정"). */
    val setByFriendName: String? = null,
    val past: Boolean = false,
)
