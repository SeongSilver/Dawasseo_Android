---
name: wakepoint-maps-location
description: Work on Wakepoint Android Google Maps, foreground location permission, current location, destination selection, map markers, camera movement, and Kakao Local place search.
---

# Wakepoint Maps Location

## Read First

- `../../../docs/harness/maps-location-flow.md`
- `../../../docs/GOTCHAS.md`

## Primary Files

- `../../../app/src/main/java/com/wakepoint/app/feature/home/HomeScreen.kt`
- `../../../app/src/main/java/com/wakepoint/app/feature/home/HomeViewModel.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/location/KakaoLocalRepository.kt`
- `../../../app/src/main/AndroidManifest.xml`
- `../../../app/build.gradle.kts`
- `../../../app/src/main/res/values/strings.xml`

## Rules

- Request foreground location only in the home map flow.
- Do not mix background location permission into this module.
- Use BuildConfig for Google Maps and Kakao keys.
- Keep Kakao coordinate order straight: `x` is longitude, `y` is latitude.
- Keep map tap and search result selection converging to the same target state.

## Verify

- Run `.\gradlew assembleDebug`.
- Manually check map render, permission allow/deny, current location fallback, map tap target, search result selection.
