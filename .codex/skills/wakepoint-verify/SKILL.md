---
name: wakepoint-verify
description: Choose and run the right verification for Wakepoint Android work. Use after code changes, dependency changes, repository updates, location or notification changes, map changes, or when the user asks whether a feature works.
---

# Wakepoint Verify

## Read First

- `../../../docs/harness/VERIFICATION_MATRIX.md`
- `../../../docs/CHECKLISTS.md`

## Workflow

1. Classify the change type using `VERIFICATION_MATRIX.md`.
2. Run only the required automatic checks.
3. If Gradle fails due sandbox network restrictions, rerun the same command with escalated permissions.
4. For Android runtime behavior, state which manual emulator or real-device check is still needed.
5. Stage only files related to the finished task.
6. Commit the task-sized change after verification.
7. Report skipped checks with a reason.

## Common Commands

```powershell
.\gradlew assembleDebug
.\gradlew test
.\gradlew lint
.\gradlew connectedAndroidTest
```

## Reporting Format

Use a short verification block:

```text
검증:
- assembleDebug: 성공
- test: 성공
- 수동: Android 13 emulator 알림 권한은 미실행, 연결된 기기 없음
커밋:
- abc1234 feat: ...
```
