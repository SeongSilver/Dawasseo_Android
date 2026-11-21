// 초안 — core/design/Theme.kt 에 합쳐 쓰기 위한 참고 코드. 컴파일 확인은 하지 않았다.
// 색 값은 tokens/colors.json 과 같다. #RRGGBBAA 는 Color(0xAARRGGBB) 로 옮겼다.
package com.wakepoint.core.design // 실제 패키지에 맞춰 수정

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class WakepointColors(
    val background: Color,
    val backgroundSecondary: Color,
    val surfaceElevated: Color,
    val surfaceElevatedSecondary: Color,
    val fill: Color,
    val fillWeak: Color,
    val fillStrong: Color,
    val card: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val text: Color,
    val textStrong: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val inactive: Color,
    val line: Color,
    val dim: Color,
    val brand: Color,
    val brandPressed: Color,
    val onBrand: Color,
    val brandSubtle: Color,
    val brandLineStrong: Color,
    val brandFocus: Color,
    val negative: Color,
    val negativeText: Color,
    val negativeSurface: Color,
    val negativeLine: Color,
    val negativeLineStrong: Color,
    val positive: Color,
    val positiveSurface: Color,
    val cautionary: Color,
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

/** M3 컴포넌트 기본값이 어긋나지 않게 하는 최소 매핑. 화면 코드는 LocalWakepointColors 를 직접 쓴다. */
fun WakepointColors.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = brand, onPrimary = onBrand,
        primaryContainer = brandSubtle, onPrimaryContainer = brand,
        secondary = brand, onSecondary = onBrand,
        secondaryContainer = fill, onSecondaryContainer = text,
        background = background, onBackground = text,
        surface = background, onSurface = text, onSurfaceVariant = textSecondary,
        surfaceContainerLowest = background, surfaceContainerLow = background,
        surfaceContainer = backgroundSecondary,
        surfaceContainerHigh = surfaceElevated, surfaceContainerHighest = surfaceElevated,
        outline = line, outlineVariant = line,
        error = negative, onError = onBrand, errorContainer = negativeSurface, onErrorContainer = negativeText,
        scrim = dim,
    )
}

val WakepointShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // 텍스트 필드
    small = RoundedCornerShape(8.dp),        // 상태 pill
    medium = RoundedCornerShape(12.dp),      // 메시지 영역, 버튼 pressed
    large = RoundedCornerShape(16.dp),       // 카드, 묶음 행
    extraLarge = RoundedCornerShape(28.dp),  // 시트, 다이얼로그, 빈 상태
)

@Composable
fun WakepointTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkWakepointColors else LightWakepointColors
    CompositionLocalProvider(LocalWakepointColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(darkTheme),
            shapes = WakepointShapes,
            // typography = WakepointTypography  // DESIGN.md 의 타입 표대로 Pretendard Regular/Medium 로 정의
            content = content,
        )
    }
}
