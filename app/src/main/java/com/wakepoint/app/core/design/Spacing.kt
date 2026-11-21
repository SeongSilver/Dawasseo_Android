package com.wakepoint.app.core.design

import androidx.compose.ui.unit.dp

/**
 * 다왔어 간격 토큰 — 단일 소스(SSOT). 출처: `design-handoff/DESIGN.md` 4절.
 *
 * 4dp 그리드: 4 · 8 · 12 · 16 · 24 · 32 · 48 만 쓴다.
 * 화면 코드에서는 dp 리터럴 대신 이 토큰을 참조한다.
 */
object WakepointSpacing {
    /** 4dp */
    val xs = 4.dp
    /** 8dp */
    val sm = 8.dp
    /** 12dp */
    val md = 12.dp
    /** 16dp */
    val lg = 16.dp
    /** 24dp */
    val xl = 24.dp
    /** 32dp */
    val xxl = 32.dp
    /** 48dp */
    val xxxl = 48.dp

    // ── 의미 기반 토큰 ──────────────────────────────────────────────
    /** 목록 화면 좌우 여백 = 16 */
    val screenHorizontal = lg
    /** 폼·시트 좌우 여백 = 24 */
    val formHorizontal = xl
    /** 카드 안 여백 = 16 */
    val cardPadding = lg
    /** 카드 ↔ 카드 = 12 */
    val cardGap = md
    /** 필드 ↔ 필드 = 16 */
    val fieldGap = lg
    /** 섹션 타이틀 ↔ 콘텐츠 = 8 */
    val sectionTitleGap = sm
    /** 섹션 ↔ 섹션 = 24 */
    val sectionGap = xl
    /** 라벨 ↔ 입력 필드 = 8 */
    val labelFieldGap = sm
    /** 아이콘 ↔ 텍스트 = 8 */
    val iconTextGap = sm
}

/** 컴포넌트 표준 높이·크기. 출처: DESIGN.md 4·5절. */
object WakepointSize {
    /** 채움·테두리 버튼 높이 = 56 */
    val buttonHeight = 56.dp
    /** 카드·행 안의 작은 버튼 높이 = 40 */
    val buttonHeightSmall = 40.dp
    /** 알람 중지 버튼 높이 = 96 */
    val stopButtonHeight = 96.dp
    /** 알람 중지 버튼 폭 = 312 */
    val stopButtonWidth = 312.dp
    /** 입력·읽기전용 필드 높이 = 56 */
    val fieldHeight = 56.dp
    /** 상태 pill 높이 = 32 */
    val pillHeight = 32.dp
    /** 묶음 행·설정 행 높이 = 56 */
    val rowHeight = 56.dp
    /** 사운드 목록 행 높이 = 64 */
    val soundRowHeight = 64.dp
    /** 상단 바 높이 = 64 */
    val headerHeight = 64.dp
    /** 최소 터치 영역 = 48 */
    val minTouchTarget = 48.dp
    /** 원형 아이콘 버튼 = 48 */
    val circleButton = 48.dp
    /** 칩 = 40 */
    val chipHeight = 40.dp
    /** 지도 위 FAB(현재 위치) = 56, 모서리 16 */
    val fab = 56.dp
    /** 검색바 = 56, 완전 원형 */
    val searchBarHeight = 56.dp
    /** 아바타 = 40 */
    val avatar = 40.dp
    /** 로고 심볼 = 32 */
    val logoMark = 32.dp
    /** 기본 아이콘 = 24 */
    val icon = 24.dp
    /** 작은 아이콘(40 높이 버튼·pill) = 20 */
    val iconSmall = 20.dp
    /** pill 안 아이콘 = 18 */
    val iconPill = 18.dp
    /** 시트 드래그 핸들 = 32 × 4 */
    val sheetHandleWidth = 32.dp
    val sheetHandleHeight = 4.dp
    /** 포커스/오류 테두리 두께 = 3 */
    val focusBorder = 3.dp
    /** 기본 테두리 두께 = 1 */
    val border = 1.dp
}
