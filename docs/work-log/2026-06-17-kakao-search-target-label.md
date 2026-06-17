# 2026-06-17 Kakao Search Target Label

## Completed

- Connected the home top search entry to Kakao-based destination search flow.
- Kakao search now combines keyword search and address search.
- Map tap now resolves selected coordinates to a readable Kakao address when possible.
- Alarm setup selected target no longer falls back to latitude/longitude text.
- Search result selection stores a readable place/building/road label for alarm creation.

## Verified

- `.\gradlew assembleDebug`: success
- `.\gradlew test`: success
