package com.wakepoint.app.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wakepoint.app.R

// v1.5 디자인 스펙은 본문 폰트로 Pretendard 를 지정하지만, 프로젝트에 Pretendard
// 폰트 리소스가 없어 임의 추가하지 않고(스펙에 없는 값 임의 생성 금지) 기존에 번들된
// Spoqa Han Sans Neo 를 유지한다. 타이포 "6단계 스케일(크기/굵기)"만 통일 적용한다.
val SpoqaFontFamily = FontFamily(
    Font(R.font.spoqa_han_sans_neo_thin, FontWeight.Thin),
    Font(R.font.spoqa_han_sans_neo_light, FontWeight.Light),
    Font(R.font.spoqa_han_sans_neo_regular, FontWeight.Normal),
    Font(R.font.spoqa_han_sans_neo_medium, FontWeight.Medium),
    // Spoqa Han Sans Neo에는 SemiBold(600)가 없어 Medium(500)으로 매핑한다.
    Font(R.font.spoqa_han_sans_neo_medium, FontWeight.SemiBold),
    Font(R.font.spoqa_han_sans_neo_bold, FontWeight.Bold)
)

/**
 * v1.5 통일 리디자인 — 타이포 6단계 단일 소스(SSOT).
 *
 * Material3 Typography 역할(role) 이름에 6단계를 매핑한다. 화면 코드는
 * `MaterialTheme.typography.<role>` 로만 참조하고 인라인 fontSize/fontWeight 를 쓰지 않는다.
 *
 *   T1 화면 타이틀   28 / Bold      → headlineLarge, headlineMedium
 *   T2 섹션 타이틀   18 / SemiBold  → titleSmall
 *   T3 카드 제목     16 / SemiBold  → titleMedium
 *   T4 본문·기본     15 / Regular   → bodyLarge, bodyMedium
 *   T5 보조          13 / Regular   → bodySmall
 *   T6 캡션·배지     11 / Medium    → labelSmall
 *   버튼 텍스트      15 / SemiBold  → labelLarge, labelMedium
 *
 * (line-height/letter-spacing 는 v1.5 스펙에 명시가 없어 기존 값에서 크기 변화에 맞춰
 *  최소 조정만 하고 임의 신설하지 않았다.)
 * 예외: displayMedium(스플래시), titleLarge(전체화면 알람 알림 = OS 화면 #07, 리디자인 제외)
 *       은 6단계 밖으로 그대로 둔다.
 */
val WakepointTypography = Typography(
    // 스플래시 디스플레이 (리디자인 범위 밖)
    displayMedium = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 34.sp,
        lineHeight = 45.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.027).em
    ),
    // T1 화면 타이틀 — 28 / Bold
    headlineLarge = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.024).em
    ),
    // T1 화면 타이틀 — 로그인 타이틀도 동일 스타일로 통일
    headlineMedium = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.024).em
    ),
    // 전체화면 알람 알림 hero (OS 화면 #07, 리디자인 제외 — 기존 유지)
    titleLarge = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.02).em
    ),
    // T3 카드 제목 — 16 / SemiBold (구 19~20px 강등)
    titleMedium = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.015).em
    ),
    // T2 섹션 타이틀 — 18 / SemiBold
    titleSmall = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.01).em
    ),
    // T4 본문·기본 — 15 / Regular
    bodyLarge = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.01).em
    ),
    // T4 본문·기본 — 15 / Regular
    bodyMedium = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.01).em
    ),
    // T5 보조 — 13 / Regular
    bodySmall = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    ),
    // 버튼 텍스트 — 15 / SemiBold
    labelLarge = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    // 버튼 텍스트(소형·보조 라벨) — 15 / SemiBold 로 통일
    labelMedium = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    // T6 캡션·배지 — 11 / Medium
    labelSmall = TextStyle(
        fontFamily = SpoqaFontFamily,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    )
)
