package com.wakepoint.app.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WakepointLightScheme = lightColorScheme(
    primary = WakepointPrimary,
    onPrimary = Color.White,
    secondary = WakepointSuccess,
    error = WakepointDanger,
    background = WakepointCanvas,
    onBackground = WakepointInk,
    surface = WakepointCanvas,
    onSurface = WakepointInk,
    surfaceVariant = WakepointParchment,
    outline = WakepointBorder
)

@Composable
fun WakepointTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WakepointLightScheme,
        typography = WakepointTypography,
        content = content
    )
}
