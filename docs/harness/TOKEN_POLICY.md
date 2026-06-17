# Token Policy

토큰 누수를 막기 위한 작업 규칙이다.

## Read Rules

- `AGENTS.md`는 새 세션 첫 1회만 읽는다.
- 작업 모듈이 정해져 있으면 해당 `.codex/skills/wakepoint-*` Skill 1개만 읽는다.
- 큰 Kotlin 파일은 전체 `Get-Content`로 읽지 않는다.
- 먼저 `rg -n "symbol|function|error"`로 위치를 찾고, 필요한 라인 범위만 읽는다.
- `HomeScreen.kt`, `AlarmsScreen.kt`, `Component.kt`는 특히 전체 읽기 금지 대상이다.

## Shell Output Rules

- 파일 크기/상태 확인은 `Select-Object`로 필요한 열만 출력한다.
- 빌드 검증은 최종 성공/실패와 핵심 오류 줄만 요약한다.
- `git diff`는 전체 출력보다 `git diff --stat`, `git diff --name-status`, 특정 파일 diff를 우선한다.

## Refactor Rules

- 300줄을 넘는 Compose 파일은 새 기능을 추가할 때 관련 컴포넌트를 별도 파일로 분리한다.
- 공통 컴포넌트 파일은 domain-specific UI가 커지면 별도 컴포넌트 파일로 분리한다.
- 하네스 문서는 인덱스 역할만 유지하고, 긴 설명은 작업 로그로 보낸다.

