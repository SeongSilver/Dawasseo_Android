package com.wakepoint.app.core.design

import androidx.compose.ui.unit.dp

/**
 * v2 전용 구조적 치수 토큰(SSOT 보강).
 * v1.5 Spacing.kt(WakepointSize)를 건드리지 않고 v2에서 필요한 구조 높이를 명명한다.
 * (지도/히어로 등 구조적 높이 — 스페이싱/폰트 토큰과 별개, docs/v2-design-guide.md 참조)
 */
object WakepointSizeV2 {
    /** 장소 등록·편집 상단 지도 프리뷰 높이. */
    val mapPreviewHeight = 160.dp

    /** 막차 메인 카운트다운 hero 영역 최소 높이. */
    val lastTrainHeroMinHeight = 120.dp
}
