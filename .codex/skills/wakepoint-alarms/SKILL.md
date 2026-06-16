---
name: wakepoint-alarms
description: Work on Wakepoint alarm creation, Supabase alarms insert/update/delete, Room alarm cache, domain-to-DTO mapping, alarm list screens, active/past alarm separation, disable, and delete actions.
---

# Wakepoint Alarms

## Read First

- `../../../docs/harness/alarm-trigger-flow.md`
- Alarm sections in `../../../docs/harness/TASK_ROUTER.md`

## Primary Files

- `../../../app/src/main/java/com/wakepoint/app/data/alarm/AlarmRepository.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/alarm/DefaultAlarmRepository.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/alarm/local/AlarmDao.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/alarm/local/AlarmEntity.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/alarm/remote/AlarmDto.kt`
- `../../../app/src/main/java/com/wakepoint/app/feature/alarms/AlarmsViewModel.kt`
- `../../../app/src/main/java/com/wakepoint/app/feature/alarms/AlarmsScreen.kt`
- `../../../app/src/main/java/com/wakepoint/app/feature/home/HomeScreen.kt`

## Rules

- Insert/update/delete remote Supabase data through Repository first.
- Sync Room cache only after remote success unless explicitly building offline mode.
- Use logged-in user id for `owner_id` and `created_by`.
- Keep active/past separation derived from alarm state, not duplicated UI-only lists.

## Verify

- Run `.\gradlew assembleDebug`.
- Run `.\gradlew test` after mapper, repository, DAO, or state changes.
- Manually check create, list update, disable, delete, and Supabase row state.
