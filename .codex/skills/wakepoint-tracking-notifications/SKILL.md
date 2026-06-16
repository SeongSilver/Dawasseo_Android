---
name: wakepoint-tracking-notifications
description: Work on Wakepoint foreground location tracking, active-alarm tracking start and stop conditions, Balanced Priority location updates, distance-based alarm trigger, Android notifications, notification channels, and Android 13 POST_NOTIFICATIONS permission UX.
---

# Wakepoint Tracking Notifications

## Read First

- `../../../docs/harness/alarm-trigger-flow.md`
- Tracking and notification sections in `../../../docs/harness/TASK_ROUTER.md`
- `../../../docs/harness/VERIFICATION_MATRIX.md`

## Primary Files

- `../../../app/src/main/java/com/wakepoint/app/service/LocationTrackingService.kt`
- `../../../app/src/main/java/com/wakepoint/app/core/location/LocationTrackingController.kt`
- `../../../app/src/main/java/com/wakepoint/app/core/location/DistanceCalculator.kt`
- `../../../app/src/main/java/com/wakepoint/app/core/notification/AlarmNotificationManager.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/alarm/DefaultAlarmRepository.kt`
- `../../../app/src/main/AndroidManifest.xml`
- `../../../app/src/main/res/values/strings.xml`

## Rules

- Start tracking when active alarm count is at least 1.
- Stop tracking immediately when active alarm count becomes 0.
- Use Balanced Priority by default.
- Use only `DistanceCalculator.calculateDistance()` for radius checks.
- Mark triggered alarms inactive and set `triggered_at`.
- Keep Android 13+ notification permission separate from location permission.

## Verify

- Run `.\gradlew assembleDebug`.
- Run `.\gradlew test` after service, distance, repository, or state changes.
- Manually check foreground notification, mock-location trigger, arrival notification, and inactive alarm update.
