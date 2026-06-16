---
name: wakepoint-design-system
description: Work on Wakepoint Jetpack Compose design system. Use for Material 3 theme, colors, Pretendard typography, common components, UI strings, cards, buttons, tabs, and visual consistency.
---

# Wakepoint Design System

## Read First

- `../../../docs/DESIGN.md`
- `../../../docs/GOTCHAS.md`

## Primary Files

- `../../../app/src/main/java/com/wakepoint/app/core/design/Color.kt`
- `../../../app/src/main/java/com/wakepoint/app/core/design/Type.kt`
- `../../../app/src/main/java/com/wakepoint/app/core/design/Theme.kt`
- `../../../app/src/main/java/com/wakepoint/app/core/design/Component.kt`
- `../../../app/src/main/res/font/`
- `../../../app/src/main/res/values/styles.xml`
- `../../../app/src/main/res/values/strings.xml`

## Rules

- Use Pretendard as the default app font.
- Keep `#0066cc` as the only accent color.
- Avoid font weight `500`; prefer `300`, `400`, `600`.
- Do not add gradient backgrounds.
- Do not nest cards inside cards.
- Put user-facing strings in `strings.xml`.

## Verify

- Run `.\gradlew assembleDebug`.
- Manually inspect changed screens for overflow, color drift, and inconsistent typography.
