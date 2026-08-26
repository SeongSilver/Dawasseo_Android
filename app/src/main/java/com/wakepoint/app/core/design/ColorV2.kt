package com.wakepoint.app.core.design

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────
// v2 통합 명세 — 추가 역할 컬러 토큰 (SSOT 보강)
// v2 스펙 대부분은 v1.5 토큰(Color.kt)과 동일하여 그대로 재사용한다.
// 아래는 기존 토큰으로 대체 불가한 신규 3색만 정의한다.
// (근거: docs/v2-design-guide.md §3 / docs/v2-decisions.md)
// ─────────────────────────────────────────────────────────────────────

/** 선택된 칩·요일 원형의 진한 테두리(2px). 기존 선택표시는 배경 tint뿐이라 별도 정의. */
val WakepointChipActiveBorder = Color(0xFF1668B8)

/** 막차 "임박" 단계 카드의 inset ring(1.5px). Warning 텍스트색과 구분되는 링 전용색. */
val WakepointImminentRing = Color(0xFFF0B429)

/** 비활성(disabled) Primary 버튼 배경. 기존 disabled 토큰은 텍스트색만 있었다. */
val WakepointButtonDisabledBg = Color(0xFFF4F4F5)
