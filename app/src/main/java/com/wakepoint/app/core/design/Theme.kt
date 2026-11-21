package com.wakepoint.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 다왔어 색 역할(role) 토큰 — 단일 소스(SSOT).
 *
 * 출처: `design-handoff/DESIGN.md` 2절 · `design-handoff/tokens/colors.json`.
 * Montage 시멘틱 팔레트를 역할 32개로 옮긴 것이다.
 *
 * 규칙(DESIGN.md 1절):
 *  - 화면 코드에는 hex 를 쓰지 않는다. `LocalWakepointColors.current` 의 역할만 참조한다.
 *  - 새 색을 만들거나 섞거나 알파를 바꾸지 않는다. 8자리 값의 알파는 그대로 쓴다.
 *  - 빨강(`negative*`)은 입력 오류와 실패 메시지에만 쓴다. 삭제·차단·로그아웃에는 쓰지 않는다.
 *
 * 이 파일은 앱에서 `Color(0x…)` 리터럴이 허용되는 유일한 곳이다.
 */
@Immutable
data class WakepointColors(
    /** 화면 배경, 테두리 카드 배경, 테두리 필드의 라벨 배경 */
    val background: Color,
    /** 하단 탭바 */
    val backgroundSecondary: Color,
    /** 바텀시트, 다이얼로그, 지도 위 검색바와 현재 위치 버튼, 알림 카드 */
    val surfaceElevated: Color,
    /** 시트 안 읽기 전용 행, 장소 검색 결과 목록, 시트 안 안내 메시지 */
    val surfaceElevatedSecondary: Color,
    /** 채움 필드, 읽기 전용 행, 보조(tonal) 버튼, 검색바, 진행 pill, 안내, 꺼진 트랙, 빈 상태 아이콘 원 */
    val fill: Color,
    /** 중립 pill, 알람 울림 바깥 원 */
    val fillWeak: Color,
    /** 아바타 배경, 알람 울림 중앙 원 */
    val fillStrong: Color,
    /** 활성 알람 카드, 프로필 메뉴 카드, 사운드 목록 카드, 빈 상태 카드 */
    val card: Color,
    /** 비활성 버튼·스위치 배경 */
    val disabledContainer: Color,
    /** 비활성 버튼 글자, 비활성 스위치 핸들 */
    val disabledContent: Color,
    /** 본문·제목, 보조 버튼 글자 */
    val text: Color,
    /** 로고 심볼 */
    val textStrong: Color,
    /** 보조 텍스트, 일반 아이콘 */
    val textSecondary: Color,
    /** 필드 라벨, 필드 보조 문구 */
    val textTertiary: Color,
    /** 비선택 탭, 꺼진 스위치 핸들·테두리, 시트 핸들 */
    val inactive: Color,
    /** 필드·테두리 버튼·카드 테두리, 구분선 */
    val line: Color,
    /** 시트·다이얼로그 뒤 scrim (이 값 그대로, 추가 알파 금지) */
    val dim: Color,
    /** 채움 버튼, 켜진 스위치, 탭 인디케이터·배지, 선택 체크, 글자 버튼, 선택 탭, 목적지 마커 */
    val brand: Color,
    /** 채움 버튼 pressed */
    val brandPressed: Color,
    /** brand 채움 위 글자·아이콘, 스위치 핸들 */
    val onBrand: Color,
    /** 선택된 행(친구 검색·알람음), 하단 탭 선택 인디케이터 */
    val brandSubtle: Color,
    /** 필드 포커스 테두리 */
    val brandLineStrong: Color,
    /** 포커스 링 */
    val brandFocus: Color,
    /** 오류 아이콘 */
    val negative: Color,
    /** 오류 메시지 글자, 필드 오류 라벨·보조 문구 */
    val negativeText: Color,
    /** 오류 메시지 배경 */
    val negativeSurface: Color,
    /** 오류 메시지 테두리 */
    val negativeLine: Color,
    /** 필드 오류 테두리 */
    val negativeLineStrong: Color,
    /** 완료 pill 글자와 체크 */
    val positive: Color,
    /** 완료 pill 배경 */
    val positiveSurface: Color,
    /** 알림 권한 안내 글자 */
    val cautionary: Color,
    /** 알림 권한 안내 배경 */
    val cautionarySurface: Color,
)

val DarkWakepointColors = WakepointColors(
    background = Color(0xFF1B1C1E),
    backgroundSecondary = Color(0xFF0F0F10),
    surfaceElevated = Color(0xFF212225),
    surfaceElevatedSecondary = Color(0xFF141415),
    fill = Color(0x3870737C),
    fillWeak = Color(0x1F70737C),
    fillStrong = Color(0x4770737C),
    card = Color(0x1F70737C),
    disabledContainer = Color(0xFF2E2F33),
    disabledContent = Color(0x29989BA2),
    text = Color(0xFFF7F7F8),
    textStrong = Color(0xFFFFFFFF),
    textSecondary = Color(0xE0C2C4C8),
    textTertiary = Color(0x9CAEB0B6),
    inactive = Color(0xFF5A5C63),
    line = Color(0xFF37383C),
    dim = Color(0xBD171719),
    brand = Color(0xFF3385FF),
    brandPressed = Color(0xFF0066FF),
    onBrand = Color(0xFFFFFFFF),
    brandSubtle = Color(0x0D3385FF),
    brandLineStrong = Color(0x6E3385FF),
    brandFocus = Color(0x1F3385FF),
    negative = Color(0xFFFF6363),
    negativeText = Color(0xFFFF6363),
    negativeSurface = Color(0x14FF6363),
    negativeLine = Color(0x6EFF6363),
    negativeLineStrong = Color(0x85FF6363),
    positive = Color(0xFF1ED45A),
    positiveSurface = Color(0x141ED45A),
    cautionary = Color(0xFFFFA938),
    cautionarySurface = Color(0x14FFA938),
)

val LightWakepointColors = WakepointColors(
    background = Color(0xFFFFFFFF),
    backgroundSecondary = Color(0xFFF7F7F8),
    surfaceElevated = Color(0xFFFFFFFF),
    surfaceElevatedSecondary = Color(0xFFF7F7F8),
    fill = Color(0x1470737C),
    fillWeak = Color(0x0D70737C),
    fillStrong = Color(0x2970737C),
    card = Color(0xFFF7F7F8),
    disabledContainer = Color(0xFFF4F4F5),
    disabledContent = Color(0x2937383C),
    text = Color(0xFF171719),
    textStrong = Color(0xFF000000),
    textSecondary = Color(0xE02E2F33),
    textTertiary = Color(0x9C37383C),
    inactive = Color(0xFF989BA2),
    line = Color(0xFFE1E2E4),
    dim = Color(0x85171719),
    brand = Color(0xFF0066FF),
    brandPressed = Color(0xFF0054D1),
    onBrand = Color(0xFFFFFFFF),
    brandSubtle = Color(0x0D0066FF),
    brandLineStrong = Color(0x6E0066FF),
    brandFocus = Color(0x1F0066FF),
    negative = Color(0xFFFF4242),
    negativeText = Color(0xFFE52222),
    negativeSurface = Color(0x14FF4242),
    negativeLine = Color(0x6EFF4242),
    negativeLineStrong = Color(0x85FF4242),
    positive = Color(0xFF00BF40),
    positiveSurface = Color(0x1400BF40),
    cautionary = Color(0xFFFF9200),
    cautionarySurface = Color(0x14FF9200),
)

val LocalWakepointColors = staticCompositionLocalOf { LightWakepointColors }

/** 현재 테마가 다크인지. 지도 스타일 등 테마별 리소스 선택에 쓴다. */
val LocalWakepointDarkTheme = staticCompositionLocalOf { false }

/** 화면 코드에서 역할 토큰을 짧게 읽기 위한 접근자. */
val wakepointColors: WakepointColors
    @Composable @ReadOnlyComposable get() = LocalWakepointColors.current

/**
 * M3 컴포넌트 기본값이 역할 토큰과 어긋나지 않도록 하는 최소 매핑.
 * 화면 코드는 그래도 `LocalWakepointColors` 를 직접 쓰는 것을 기본으로 한다.
 */
fun WakepointColors.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = brandSubtle,
        onPrimaryContainer = brand,
        secondary = brand,
        onSecondary = onBrand,
        secondaryContainer = fill,
        onSecondaryContainer = text,
        tertiary = brand,
        onTertiary = onBrand,
        background = background,
        onBackground = text,
        surface = background,
        onSurface = text,
        surfaceVariant = fill,
        onSurfaceVariant = textSecondary,
        surfaceContainerLowest = background,
        surfaceContainerLow = background,
        surfaceContainer = backgroundSecondary,
        surfaceContainerHigh = surfaceElevated,
        surfaceContainerHighest = surfaceElevated,
        outline = line,
        outlineVariant = line,
        error = negative,
        onError = onBrand,
        errorContainer = negativeSurface,
        onErrorContainer = negativeText,
        scrim = dim,
    )
}

/**
 * 앱 테마. 시스템 다크 설정을 따른다(DESIGN.md 1절).
 * `darkTheme` 를 넘기면 @Preview 에서 테마를 고정할 수 있다.
 */
@Composable
fun WakepointTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkWakepointColors else LightWakepointColors
    CompositionLocalProvider(
        LocalWakepointColors provides colors,
        LocalWakepointDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(darkTheme),
            typography = WakepointTypography,
            shapes = WakepointShapes,
            content = content
        )
    }
}
