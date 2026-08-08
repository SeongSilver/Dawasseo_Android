package com.wakepoint.app.core.design

import androidx.compose.ui.unit.dp

/**
 * v1.5 통일 리디자인 — 간격(spacing) 단일 소스(SSOT).
 *
 * 8pt 그리드: 4·8·12·16·24·32 만 사용한다. 화면 코드에서는 dp 리터럴을 쓰지 말고
 * 반드시 이 토큰을 참조한다. (디자인: "간격 — 8pt 그리드")
 */
object WakepointSpacing {
    // ── 원시 그리드 스텝 (4·8·12·16·24·32) ──────────────────────────
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

    // ── 의미 기반 토큰 (전 화면 통일) ──────────────────────────────
    /** 화면 좌우 여백 = 16 */
    val screenHorizontal = lg
    /** 카드 내부 패딩(상하·좌우 동일) = 16 */
    val cardPadding = lg
    /** 카드 ↔ 카드 = 12 */
    val cardGap = md
    /** 섹션 타이틀 ↔ 콘텐츠 = 8 */
    val sectionTitleGap = sm
    /** 섹션 ↔ 섹션 = 24 */
    val sectionGap = xl
    /** 라벨 ↔ 입력 필드 = 8 */
    val labelFieldGap = sm
    /** 아이콘 ↔ 텍스트 = 8 */
    val iconTextGap = sm
}

/**
 * v1.5 컴포넌트 표준 높이/크기 토큰.
 * (디자인: "컴포넌트 높이" 시트)
 */
object WakepointSize {
    /** 버튼(풀폭·2분할 동일) 높이 = 52 */
    val buttonHeight = 52.dp
    /** 입력·읽기전용 필드 높이 = 52 */
    val fieldHeight = 52.dp
    /** 칩 높이 = 44 */
    val chipHeight = 44.dp
    /** 설정 행 / 토글 행 높이 = 56 */
    val rowHeight = 56.dp
    /** FAB(알약형 확장 포함) 높이 = 48 */
    val fab = 48.dp
    /** 원형 아이콘 버튼 = 48 */
    val circleButton = 48.dp
}
