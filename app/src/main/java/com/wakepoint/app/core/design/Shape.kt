package com.wakepoint.app.core.design

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 다왔어 모서리 토큰 — 단일 소스(SSOT). 출처: `design-handoff/DESIGN.md` 4절.
 *
 * 텍스트 필드 4 · 상태 pill 8 · 메시지 영역 12 · 카드와 묶음 행 16 ·
 * 시트(위쪽)·다이얼로그·빈 상태 카드 28 · 버튼·검색바·스위치·아바타는 완전 원형.
 */
object WakepointRadius {
    /** 4 — 텍스트 필드 */
    val field = 4.dp
    /** 8 — 상태 pill */
    val pill = 8.dp
    /** 12 — 메시지 영역, 버튼 pressed */
    val message = 12.dp
    /** 16 — 카드, 묶음 행 */
    val card = 16.dp
    /** 28 — 시트, 다이얼로그, 빈 상태 카드 */
    val sheet = 28.dp
}

object WakepointShape {
    val field = RoundedCornerShape(WakepointRadius.field)
    val pill = RoundedCornerShape(WakepointRadius.pill)
    val message = RoundedCornerShape(WakepointRadius.message)
    val card = RoundedCornerShape(WakepointRadius.card)
    val dialog = RoundedCornerShape(WakepointRadius.sheet)
    /** 시트는 위쪽 모서리만 28 */
    val sheet = RoundedCornerShape(
        topStart = WakepointRadius.sheet,
        topEnd = WakepointRadius.sheet,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    /** 버튼·검색바·스위치·아바타 */
    val full = CircleShape
    /** 버튼을 누르는 동안 한 단계 작아진 모서리 (m: 원형 → 12) */
    val buttonPressed = RoundedCornerShape(WakepointRadius.message)
}

/** MaterialTheme.shapes 매핑. */
val WakepointShapes = Shapes(
    extraSmall = WakepointShape.field,
    small = WakepointShape.pill,
    medium = WakepointShape.message,
    large = WakepointShape.card,
    extraLarge = WakepointShape.dialog
)
