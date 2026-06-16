# Skill Commands Harness

반복 작업을 모듈별 Skill로 분리한 명령 인덱스다.
새 작업에서는 먼저 아래 이름 중 하나로 범위를 좁힌다.

## Entry

- `wakepoint-start`
  - 현재 상태 파악, 다음 작업 선정, 긴 로그 대신 압축 컨텍스트 사용.
  - 예: "wakepoint-start로 다음 개발 우선순위 정리해줘"

## Module Skills

- `wakepoint-auth`
  - 회원가입, 로그인, 로그아웃, Supabase Auth, DataStore session.
  - 예: "wakepoint-auth로 Google Sign-In 붙여줘"

- `wakepoint-maps-location`
  - Google Maps, foreground 위치 권한, 현재 위치, 목적지 선택, Kakao Local 검색.
  - 예: "wakepoint-maps-location으로 검색 결과 선택 UX 다듬어줘"

- `wakepoint-alarms`
  - 알람 생성, Supabase `alarms`, Room cache, 목록, 활성/지난 알람.
  - 예: "wakepoint-alarms로 알람 수정 기능 추가해줘"

- `wakepoint-tracking-notifications`
  - 위치 추적 서비스, 반경 진입 판단, 알림 채널, Android 13 알림 권한.
  - 예: "wakepoint-tracking-notifications로 POST_NOTIFICATIONS 권한 UX 구현해줘"

- `wakepoint-supabase-db`
  - Schema, RLS, migration, DTO, repository, Realtime, Storage.
  - 예: "wakepoint-supabase-db로 friends RLS 설계해줘"

- `wakepoint-design-system`
  - Compose theme, Pretendard, 색상, 공통 컴포넌트, 문자열.
  - 예: "wakepoint-design-system으로 알람 카드 UI 정리해줘"

- `wakepoint-verify`
  - 작업별 최소 검증 선택, Gradle test/build, 수동 검증 기록.
  - 예: "wakepoint-verify로 이번 변경 검증해줘"

## 사용 규칙

- 작업 요청이 모듈 이름을 포함하면 해당 Skill만 먼저 읽는다.
- 모듈 이름이 없으면 `wakepoint-start`로 라우팅한다.
- 여러 모듈이 걸리면 가장 위험한 모듈 1개부터 시작한다.
  - 위치/알림 > Supabase DB > 인증 > 알람 > 지도 > 디자인 순으로 우선한다.
- 검증만 요청하면 `wakepoint-verify`를 사용한다.

