package com.wakepoint.app.core.design

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * v2 전용 타이포 토큰(SSOT 보강).
 * 6단계 표준 role(Type.kt)로 표현 불가한 막차 화면의 초대형 hero 텍스트만 정의한다.
 * 화면 코드는 이 명명 토큰만 참조한다(sp 리터럴 금지 규칙 준수 — 리터럴은 토큰 파일에만).
 * (근거: docs/v2-design-guide.md §3, docs/v2-decisions.md)
 */
object WakepointTypeV2 {
    /** 막차 남은 시간 카운트다운(hero, 52/Bold). 명세 STEP4 최상위 위계. */
    val lastTrainCountdown = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 52.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.03).em,
    )

    /** 막차 택시비 등 금액 강조(34/Bold). 화면 2번째로 큰 요소. */
    val amountEmphasis = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.02).em,
    )
}
