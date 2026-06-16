---
name: wakepoint-auth
description: Work on Wakepoint Android authentication. Use for email signup, login, logout, Supabase Auth REST, session persistence in DataStore, auth navigation, Google Sign-In, Kakao Login, or phone verification MVP decisions.
---

# Wakepoint Auth

## Read First

- `../../../docs/harness/CURRENT_STATE.md`
- `../../../docs/GOTCHAS.md`
- Auth section in `../../../docs/harness/TASK_ROUTER.md`

## Primary Files

- `../../../app/src/main/java/com/wakepoint/app/data/auth/AuthRepository.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/auth/DefaultAuthRepository.kt`
- `../../../app/src/main/java/com/wakepoint/app/feature/auth/AuthViewModel.kt`
- `../../../app/src/main/java/com/wakepoint/app/feature/auth/AuthScreen.kt`
- `../../../app/src/main/java/com/wakepoint/app/core/data/preferences/UserPreferencesDataStore.kt`
- `../../../app/src/main/java/com/wakepoint/app/navigation/WakepointNavGraph.kt`

## Rules

- Keep Supabase calls inside Repository.
- Keep session persistence inside DataStore helpers.
- Treat phone verification as optional or hidden for MVP unless explicitly reintroduced.
- Do not hardcode secrets.
- Put new user-facing strings in `strings.xml`.

## Verify

- Run `.\gradlew assembleDebug`.
- Run `.\gradlew test` after repository or session logic changes.
- Manually check signup, login, logout, and email-confirmation behavior when possible.
