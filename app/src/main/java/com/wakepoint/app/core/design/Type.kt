package com.wakepoint.app.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 다왔어 타입 스케일 — 단일 소스(SSOT). 출처: `design-handoff/DESIGN.md` 3절.
 *
 * 디자인 스펙의 본문 폰트는 Pretendard 지만 `res/font` 에 Pretendard 리소스가 없다.
 * 스펙에 없는 에셋을 임의로 추가하지 않고 시스템 폰트(`FontFamily.Default`)로 진행한다.
 * (`docs/DESIGN_PROGRESS.md` 의 "디자인과 다르게 구현한 곳" 참고)
 *
 * 굵기는 Regular(400) 와 Medium(500) 두 가지만 쓴다. Bold 를 쓰지 않는다.
 * 화면 코드는 `MaterialTheme.typography.<role>` 로만 참조하고 인라인 fontSize/fontWeight 를 쓰지 않는다.
 */
val WakepointFontFamily = FontFamily.Default

private fun wakepointStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double
) = TextStyle(
    fontFamily = WakepointFontFamily,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing.sp
)

val WakepointTypography = Typography(
    /** 57/64 · 400 — 녹음 경과 시간 */
    displayLarge = wakepointStyle(57, 64, FontWeight.Normal, -0.25),
    /** 45/52 · 400 — 알람 울림 "도착했습니다" */
    displayMedium = wakepointStyle(45, 52, FontWeight.Normal, 0.0),
    displaySmall = wakepointStyle(36, 44, FontWeight.Normal, 0.0),
    headlineLarge = wakepointStyle(32, 40, FontWeight.Normal, 0.0),
    headlineMedium = wakepointStyle(28, 36, FontWeight.Normal, 0.0),
    /** 24/32 · 400 — 시트·다이얼로그 제목, 알람 중지 버튼 라벨 */
    headlineSmall = wakepointStyle(24, 32, FontWeight.Normal, 0.0),
    /** 22/28 · 400 — 상단 바 제목, 빈 상태 제목 */
    titleLarge = wakepointStyle(22, 28, FontWeight.Normal, 0.0),
    /** 16/24 · 500 — 섹션 제목, 행의 이름, 버튼 m 라벨 */
    titleMedium = wakepointStyle(16, 24, FontWeight.Medium, 0.15),
    /** 14/20 · 500 — 요청/친구 탭 */
    titleSmall = wakepointStyle(14, 20, FontWeight.Medium, 0.1),
    /** 16/24 · 400 — 본문, 필드 입력값, 목록 행 제목 */
    bodyLarge = wakepointStyle(16, 24, FontWeight.Normal, 0.5),
    /** 14/20 · 400 — 보조 설명, 메시지 */
    bodyMedium = wakepointStyle(14, 20, FontWeight.Normal, 0.25),
    /** 12/16 · 400 — 떠오른 필드 라벨, 필드 보조 문구, 행 부제 */
    bodySmall = wakepointStyle(12, 16, FontWeight.Normal, 0.4),
    /** 14/20 · 500 — 버튼 s 라벨, 상태 pill, 섹션 라벨 */
    labelLarge = wakepointStyle(14, 20, FontWeight.Medium, 0.1),
    /** 12/16 · 500 — 하단 탭 라벨 */
    labelMedium = wakepointStyle(12, 16, FontWeight.Medium, 0.5),
    /** 11/16 · 500 — 탭 배지 숫자 */
    labelSmall = wakepointStyle(11, 16, FontWeight.Medium, 0.5)
)

/** 브랜드 워드마크 "다왔어" — 22/28 · 500 (DESIGN.md 3절 titleLarge 단서). */
val WakepointWordmarkStyle = wakepointStyle(22, 28, FontWeight.Medium, 0.0)
