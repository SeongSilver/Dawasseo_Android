# Start Here Harness

새 세션이나 새 작업을 시작할 때 가장 먼저 읽는 토큰 절약용 진입점이다.
긴 작업 로그를 매번 처음부터 읽지 말고, 이 파일에서 필요한 하네스로 라우팅한다.

## 읽기 순서

1. `AGENTS.md`
2. `docs/harness/START_HERE.md`
3. `docs/harness/CURRENT_STATE.md`
4. `docs/harness/TASK_ROUTER.md`
5. `docs/harness/SKILL_COMMANDS.md`
6. 현재 작업과 직접 관련된 하네스 또는 Skill 1개

긴 문서는 아래 조건에서만 읽는다.

- `docs/work-log/2026-06-16-current-status.md`: 최근 상태가 `CURRENT_STATE.md`와 맞지 않을 때
- `docs/SCHEMA.md`: DB schema, RLS, Supabase table을 바꿀 때
- `docs/API.md`: API contract를 바꿀 때
- `docs/DESIGN.md`: 디자인 토큰이나 공통 UI 규칙을 바꿀 때
- `docs/SETUP.md`: 개발 환경, 키, Firebase, Kakao, Google 설정을 바꿀 때
- `docs/harness/STRUCTURE_POLICY.md`: 폴더 구조, 전역/프로젝트 분리, 파일명 규칙을 바꿀 때
- `docs/harness/MEMORY_POLICY.md`: 메모리나 작업 로그 운영 방식을 바꿀 때
- `docs/harness/HOOK_POLICY.md`: hook을 만들거나 수정할 때

## Skill 우선 사용

반복 작업은 `.codex/skills/wakepoint-*` Skill로 분리되어 있다.
작업 요청에 맞는 Skill이 있으면 긴 하네스 문서보다 Skill을 먼저 읽는다.
명령 목록은 `docs/harness/SKILL_COMMANDS.md`를 따른다.

## 현재 우선순위

1. Android 13+ `POST_NOTIFICATIONS` 권한 UX
2. 알람 트리거 실기기 검증과 foreground service 안정화
3. Supabase `alarms` RLS/schema 실DB 검증
4. Kakao Local 검색 UX 정리
5. 친구/권한/대리 알람
6. 커스텀 알람음

## 기본 명령

```powershell
.\gradlew assembleDebug
.\gradlew test
git status --short
```

Gradle이 네트워크 또는 wrapper 다운로드 문제로 실패하면 같은 명령을 escalated 권한으로 재시도한다.

## 토큰 절약 규칙

- 먼저 `TASK_ROUTER.md`에서 작업 유형을 찾는다.
- 라우터가 지정한 파일만 먼저 연다.
- 전체 repository scan은 파일 위치를 모를 때만 한다.
- project-specific Skill은 `.codex/skills`에만 두고 전역 `~/.codex/skills`에는 두지 않는다.
- 긴 work log 대신 `CURRENT_STATE.md`를 먼저 믿고, 모순이 있을 때만 work log를 확인한다.
- 코드 설명을 요청받지 않은 경우 구현과 검증 결과 중심으로 답한다.
