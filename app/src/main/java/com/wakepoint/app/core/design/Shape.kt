package com.wakepoint.app.core.design

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * v1.5 통일 리디자인 — 코너 라운드 4단계 단일 소스(SSOT).
 *
 * 8 배지·작은 요소 / 12 입력·칩·버튼 / 20 카드·시트 / full 검색·탭바·FAB.
 * (디자인: "라운드 — 4단계")
 */
object WakepointRadius {
    /** 8 — 배지·작은 요소 */
    val badge = 8.dp
    /** 12 — 입력·칩·버튼 */
    val control = 12.dp
    /** 20 — 카드·시트 */
    val card = 20.dp
}

/** 라운드 단계에 대응하는 Shape 상수. */
object WakepointShape {
    val badge = RoundedCornerShape(WakepointRadius.badge)
    val control = RoundedCornerShape(WakepointRadius.control)
    val card = RoundedCornerShape(WakepointRadius.card)
    /** full — 검색·탭바·FAB */
    val full = CircleShape
}

/** MaterialTheme.shapes 로 노출되는 매핑(small=배지, medium=컨트롤, large=카드). */
val WakepointShapes = Shapes(
    small = WakepointShape.badge,
    medium = WakepointShape.control,
    large = WakepointShape.card
)
