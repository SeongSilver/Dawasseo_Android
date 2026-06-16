---
name: wakepoint-supabase-db
description: Work on Wakepoint Supabase database integration. Use for schema, RLS, migrations, DTOs, REST queries, repositories, Realtime, Storage, owner_id/created_by policies, and live DB verification.
---

# Wakepoint Supabase DB

## Read First

- `../../../docs/SCHEMA.md`
- `../../../docs/GOTCHAS.md`
- Supabase sections in `../../../docs/harness/TASK_ROUTER.md`

## Primary Files

- `../../../app/src/main/java/com/wakepoint/app/core/supabase/SupabaseConfig.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/alarm/DefaultAlarmRepository.kt`
- `../../../app/src/main/java/com/wakepoint/app/data/alarm/remote/AlarmDto.kt`
- `../../../supabase/migrations/`
- Relevant `data/*` repository and `remote/*Dto.kt` files.

## Rules

- Never put service role keys in the Android app.
- Keep client keys in `local.properties` and BuildConfig.
- Keep DTO and domain model separated.
- Check RLS whenever a repository call fails against live Supabase.
- Prefer repository-level Supabase access.

## Verify

- Run `.\gradlew assembleDebug`.
- Run `.\gradlew test` after mapper or repository logic changes.
- Manually verify live insert/update/delete in Supabase when changing DB behavior.
