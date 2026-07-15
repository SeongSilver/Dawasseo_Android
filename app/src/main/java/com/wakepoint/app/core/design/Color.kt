package com.wakepoint.app.core.design

import androidx.compose.ui.graphics.Color

// Neutral tint bases. The prototype expresses most neutrals as rgba over these
// two hues: 112,115,124 for lines/fills and 55,56,60 for text.
private val NeutralLineBase = Color(0xFF70737C)
private val NeutralTextBase = Color(0xFF37383C)

// Brand / primary
val WakepointPrimary = Color(0xFF0066CC)
val WakepointPrimaryHover = Color(0xFF005CB8)
val WakepointPrimaryActive = Color(0xFF0052A3)

// Primary tints (used for selected states, avatar backgrounds, ring fills)
val WakepointPrimaryTint04 = WakepointPrimary.copy(alpha = 0.04f)
val WakepointPrimaryTint05 = WakepointPrimary.copy(alpha = 0.05f)
val WakepointPrimaryTint06 = WakepointPrimary.copy(alpha = 0.06f)
val WakepointPrimaryTint08 = WakepointPrimary.copy(alpha = 0.08f)
val WakepointPrimaryTint10 = WakepointPrimary.copy(alpha = 0.10f)
val WakepointPrimaryTint13 = WakepointPrimary.copy(alpha = 0.13f)
val WakepointPrimaryTint28 = WakepointPrimary.copy(alpha = 0.28f)
val WakepointPrimaryTint35 = WakepointPrimary.copy(alpha = 0.35f)

// Semantic
val WakepointSuccess = Color(0xFF10B981)
val WakepointDanger = Color(0xFFEF4444)
val WakepointDangerBg = Color(0xFFFEF2F2)

// Surfaces / backgrounds
val WakepointCanvas = Color(0xFFFFFFFF)        // white cards / sheets
val WakepointBackground = Color(0xFFF5F5F7)    // page background
val WakepointParchment = Color(0xFFF5F5F7)     // field fill / inset blocks (alias)
val WakepointMapBase = Color(0xFFE3E7EA)
val WakepointDarkTile = Color(0xFF272729)
val WakepointTabBar = Color(0xFFFFFFFF)

// Neutral lines & fills
val WakepointBorder = NeutralLineBase.copy(alpha = 0.16f)
val WakepointBorderStrong = NeutralLineBase.copy(alpha = 0.22f)
val WakepointDivider = NeutralLineBase.copy(alpha = 0.12f)
val WakepointFill = NeutralLineBase.copy(alpha = 0.08f)
val WakepointFillHover = NeutralLineBase.copy(alpha = 0.12f)

// Text
val WakepointInk = Color(0xFF171719)                        // primary text
val WakepointTextSecondary = NeutralTextBase.copy(alpha = 0.61f)
val WakepointTextTertiary = NeutralTextBase.copy(alpha = 0.40f)
val WakepointTextQuaternary = NeutralTextBase.copy(alpha = 0.34f)
val WakepointTextDisabled = NeutralTextBase.copy(alpha = 0.28f)
val WakepointMuted = WakepointTextSecondary                 // alias used across screens

// Icon strokes
val WakepointIconMuted = NeutralLineBase                    // #70737C
val WakepointIconLight = Color(0xFFC2C4C8)                  // chevrons / empty state
val WakepointIconInactive = Color(0xFF989BA2)               // inactive tab / search
