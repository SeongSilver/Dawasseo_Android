package com.wakepoint.app.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WakepointLightScheme = lightColorScheme(
    primary = WakepointPrimary,
    onPrimary = Color.White,
    primaryContainer = WakepointPrimaryTint08,
    onPrimaryContainer = WakepointPrimary,
    secondary = WakepointSuccess,
    onSecondary = Color.White,
    error = WakepointDanger,
    onError = Color.White,
    background = WakepointBackground,
    onBackground = WakepointInk,
    surface = WakepointCanvas,
    onSurface = WakepointInk,
    surfaceVariant = WakepointParchment,
    onSurfaceVariant = WakepointTextSecondary,
    outline = WakepointBorder,
    outlineVariant = WakepointDivider
)

@Composable
fun WakepointTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WakepointLightScheme,
        typography = WakepointTypography,
        shapes = WakepointShapes,
        content = content
    )
}
