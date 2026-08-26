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

// Semantic (v1.5: Positive #0B8A4B, Danger #EF4444 + Light #FDECEC)
val WakepointSuccess = Color(0xFF0B8A4B)
val WakepointDanger = Color(0xFFEF4444)
val WakepointDangerBg = Color(0xFFFDECEC)

// Surfaces / backgrounds
val WakepointCanvas = Color(0xFFFFFFFF)        // white cards / sheets (v1.5 Card)
val WakepointBackground = Color(0xFFF5F5F7)    // page background (v1.5 BG)
val WakepointParchment = Color(0xFFEFEFF1)     // field fill / Secondary button (v1.5 Field)
val WakepointMapBase = Color(0xFFE3E7EA)
val WakepointDarkTile = Color(0xFF272729)
val WakepointTabBar = Color(0xFFFFFFFF)

// Neutral lines & fills
val WakepointBorder = NeutralLineBase.copy(alpha = 0.16f)
val WakepointBorderStrong = NeutralLineBase.copy(alpha = 0.22f)
val WakepointDivider = NeutralLineBase.copy(alpha = 0.12f)
val WakepointFill = NeutralLineBase.copy(alpha = 0.08f)
val WakepointFillHover = NeutralLineBase.copy(alpha = 0.12f)

// Text (v1.5: Text1 #171719 / Text2 #5B5E66 / Text3 #9CA0A8 / Disabled #C7CAD1)
val WakepointInk = Color(0xFF171719)                        // Text1 — 타이틀·제목
val WakepointTextSecondary = Color(0xFF5B5E66)              // Text2 — 본문
val WakepointTextTertiary = Color(0xFF9CA0A8)              // Text3 — 보조·placeholder
val WakepointTextQuaternary = Color(0xFF9CA0A8)            // Text3 — placeholder (alias)
val WakepointTextDisabled = Color(0xFFC7CAD1)             // Disabled — 지난 알람 텍스트
val WakepointMuted = WakepointTextSecondary                 // alias used across screens

// Icon strokes
val WakepointIconMuted = NeutralLineBase                    // #70737C
val WakepointIconLight = Color(0xFFC2C4C8)                  // chevrons / empty state
val WakepointIconInactive = Color(0xFF989BA2)               // inactive tab / search

// ─────────────────────────────────────────────────────────────────────
// v1.5 통일 리디자인 — 역할(role) 컬러 토큰 (단일 소스)
// 화면 코드에서는 이 이름들을 참조한다. 위의 기존 이름은 하위호환 별칭이다.
// ─────────────────────────────────────────────────────────────────────
/** Primary #0066CC — 버튼·토글·선택·활성 탭 */
val WakepointPrimaryLight = Color(0xFFE9F1FB)               // 선택 칩 BG·탭 pill·아바타 배경
val WakepointField = Color(0xFFEFEFF1)                      // 입력·Secondary 버튼 배경
val WakepointText2 = WakepointTextSecondary                // #5B5E66 — 본문
val WakepointText3 = WakepointTextTertiary                 // #9CA0A8 — 보조·placeholder
val WakepointDangerLight = WakepointDangerBg               // #FDECEC — Danger Tonal 배경
val WakepointInactiveTrack = Color(0xFFE3E4E8)             // 꺼진 토글 트랙
val WakepointDisabledText = WakepointTextDisabled          // #C7CAD1 — 비활성 텍스트
val WakepointPositive = WakepointSuccess                   // #0B8A4B — 긍정 배지 텍스트
val WakepointPositiveLight = Color(0xFFE6F6EE)            // 긍정 배지 배경
val WakepointWarning = Color(0xFFB25E00)                  // 권한 대기 배지 텍스트
val WakepointWarningLight = Color(0xFFFFF4E5)             // 권한 대기 배지 배경
val WakepointChipBorder = Color(0xFFDEE0E4)               // 칩·읽기전용 테두리(1.5px)
val WakepointStar = Color(0xFFFFB020)                     // 즐겨찾기 별(앰버). 나머지 별은 회색
