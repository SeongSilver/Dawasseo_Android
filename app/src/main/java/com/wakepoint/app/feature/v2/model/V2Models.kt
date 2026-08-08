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

/**
 * 내 장소(STEP2). 이름 이원화: 내가 보는 이름(비공개) ↔ 친구가 보는 이름(공개 시 필수).
 * 주소는 어떤 경우에도 친구에게 비공개.
 */
data class V2Place(
    val myName: String,
    val isPublic: Boolean = false,
    /** 친구가 보는 이름(예: "장소 2"). 공개 시 필수. */
    val sharedName: String? = null,
    /** "집"은 고정 슬롯. */
    val isFixed: Boolean = false,
)

/** 친구 대리설정 권한 상태(STEP3). */
enum class FriendPermission(val label: String) {
    ALLOWED("대리 설정 가능"),
    PENDING("권한 대기"),
    DENIED("거절됨"),
}

/** 보낸 알람 상태(STEP3) — 발신자에게는 2단계까지만 보인다. */
enum class SentAlarmStatus(val label: String) {
    SENT("전송됨"),
    REGISTERED("기기 등록됨"),
}

/** 막차 3단계(STEP4). */
enum class LastTrainLevel {
    RELAXED, // 여유 (파랑)
    IMMINENT, // 임박 (Warning)
    CRITICAL, // 아슬 (Danger 반전)
}
