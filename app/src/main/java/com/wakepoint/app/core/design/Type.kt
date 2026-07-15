package com.wakepoint.app.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wakepoint.app.R

val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_light, FontWeight.Light),
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_regular, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

val WakepointTypography = Typography(
    // Splash / onboarding display
    displayMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 34.sp,
        lineHeight = 45.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.027).em
    ),
    // Page & login titles ("알람", "어서 오세요…")
    headlineLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.024).em
    ),
    headlineMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.02).em
    ),
    // Arrival card place name
    titleLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.02).em
    ),
    // List item title / profile name
    titleMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.015).em
    ),
    // Section header inside forms
    titleSmall = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.01).em
    ),
    // Body / input text / place names
    bodyLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.01).em
    ),
    // Body secondary / meta summary
    bodyMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),
    // Captions / addresses / meta lines
    bodySmall = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    ),
    // Primary button / list labels
    labelLarge = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    // Small buttons / secondary labels
    labelMedium = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    // Tab labels / alias tags
    labelSmall = TextStyle(
        fontFamily = PretendardFontFamily,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.02.em
    )
)
