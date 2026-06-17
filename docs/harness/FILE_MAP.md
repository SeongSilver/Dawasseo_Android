# File Map Harness

파일 위치를 찾기 위한 압축 지도다.
전체 `rg --files`를 하기 전에 이 문서를 먼저 확인한다.

## App Entry

- `app/src/main/java/com/wakepoint/app/MainActivity.kt`
- `app/src/main/java/com/wakepoint/app/WakepointApplication.kt`
- `app/src/main/java/com/wakepoint/app/navigation/WakepointNavGraph.kt`

## Core

- `app/src/main/java/com/wakepoint/app/core/di/AppModule.kt`: Hilt binding
- `app/src/main/java/com/wakepoint/app/core/data/preferences/UserPreferencesDataStore.kt`: DataStore session/settings
- `app/src/main/java/com/wakepoint/app/core/supabase/SupabaseConfig.kt`: Supabase endpoint/key config
- `app/src/main/java/com/wakepoint/app/core/location/DistanceCalculator.kt`: distance calculation
- `app/src/main/java/com/wakepoint/app/core/location/LocationTrackingController.kt`: tracking service start/stop
- `app/src/main/java/com/wakepoint/app/core/notification/AlarmNotificationManager.kt`: notification channels and alarm notifications

## Design

- `app/src/main/java/com/wakepoint/app/core/design/Color.kt`
- `app/src/main/java/com/wakepoint/app/core/design/Type.kt`
- `app/src/main/java/com/wakepoint/app/core/design/Theme.kt`
- `app/src/main/java/com/wakepoint/app/core/design/Component.kt`
- `app/src/main/res/font/`
- `app/src/main/res/values/styles.xml`

## Auth

- `app/src/main/java/com/wakepoint/app/data/auth/AuthRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/auth/DefaultAuthRepository.kt`
- `app/src/main/java/com/wakepoint/app/feature/auth/AuthViewModel.kt`
- `app/src/main/java/com/wakepoint/app/feature/auth/AuthScreen.kt`

## Home, Map, Search

- `app/src/main/java/com/wakepoint/app/feature/home/HomeScreen.kt`
- `app/src/main/java/com/wakepoint/app/feature/home/HomeViewModel.kt`
- `app/src/main/java/com/wakepoint/app/data/location/KakaoLocalRepository.kt`

## Alarm

- `app/src/main/java/com/wakepoint/app/data/alarm/AlarmRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/DefaultAlarmRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/local/AlarmDao.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/local/AlarmEntity.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/remote/AlarmDto.kt`
- `app/src/main/java/com/wakepoint/app/feature/alarms/AlarmsViewModel.kt`
- `app/src/main/java/com/wakepoint/app/feature/alarms/AlarmsScreen.kt`

## Service

- `app/src/main/java/com/wakepoint/app/service/LocationTrackingService.kt`
- `app/src/main/java/com/wakepoint/app/service/WakepointFirebaseMessagingService.kt`
- `app/src/main/java/com/wakepoint/app/service/AlarmWorker.kt`

## Resources

- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/styles.xml`

## Docs

- `docs/harness/START_HERE.md`: 새 세션 진입점
- `docs/harness/CURRENT_STATE.md`: 압축 현재 상태
- `docs/harness/TASK_ROUTER.md`: 작업 유형별 읽을 파일
- `docs/harness/SKILL_COMMANDS.md`: 모듈별 Skill 명령 인덱스
- `docs/harness/STRUCTURE_POLICY.md`: 전역/프로젝트 분리와 파일명 규칙
- `docs/harness/MEMORY_POLICY.md`: 메모리와 work-log 분할 규칙
- `docs/harness/HOOK_POLICY.md`: hook 종료 조건
- `docs/harness/TOKEN_POLICY.md`: 토큰 절약용 읽기/출력 규칙
- `docs/harness/VERIFICATION_MATRIX.md`: 검증 범위 선택
- `docs/GOTCHAS.md`: 반복 함정
- `docs/CHECKLISTS.md`: 반복 체크리스트
- `docs/work-log/2026-06-16-current-status.md`: 긴 상세 작업 로그

## Project Skills

- `.codex/skills/wakepoint-start/SKILL.md`: 새 작업 라우팅
- `.codex/skills/wakepoint-auth/SKILL.md`: 인증
- `.codex/skills/wakepoint-maps-location/SKILL.md`: 지도, 위치, 검색
- `.codex/skills/wakepoint-alarms/SKILL.md`: 알람 저장, 목록
- `.codex/skills/wakepoint-tracking-notifications/SKILL.md`: 위치 추적, 트리거, 알림
- `.codex/skills/wakepoint-supabase-db/SKILL.md`: Supabase schema, RLS, repository
- `.codex/skills/wakepoint-design-system/SKILL.md`: Compose design system
- `.codex/skills/wakepoint-verify/SKILL.md`: 검증 선택과 실행
- `.codex/memory/INDEX.md`: 프로젝트 메모리 인덱스

## Design Screens

- `docs/design/screens/login.png`
- `docs/design/screens/signup.png`
- `docs/design/screens/splash.png`
- `docs/design/screens/map.png`
- `docs/design/screens/map-alarm-settings.png`
- `docs/design/screens/alarms-list.png`
- `docs/design/screens/alarms-sound-list.png`
- `docs/design/screens/alarms-sound-recording.png`
- `docs/design/screens/friends-list.png`
- `docs/design/screens/friends-detail.png`
- `docs/design/screens/friends-send-alarm.png`
- `docs/design/screens/profile.png`
