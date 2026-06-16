# Current State Harness

2026-06-16 기준 압축 상태다.
새 세션에서 긴 작업 로그 대신 이 문서를 먼저 읽는다.

## 완료된 큰 흐름

- Android Native + Kotlin + Jetpack Compose 단일 타겟으로 전환했다.
- 반복 개발 하네스를 `.codex/skills/wakepoint-*` 모듈 Skill로 분리했다.
- 전역/프로젝트 폴더를 분리하고 Wakepoint 전용 Skill은 프로젝트 `.codex/skills`에만 둔다.
- 프로젝트 파일/폴더명은 영문으로 정리했다.
- Supabase 이메일 회원가입/로그인 REST 흐름을 연결했다.
- Auth session은 DataStore에 저장한다.
- 전화번호 인증은 MVP에서 숨기거나 선택 입력으로 낮췄다.
- Google Maps Compose를 홈 화면에 적용했다.
- foreground 위치 권한 요청, 현재 위치 가져오기, fallback 위치 처리를 구현했다.
- 지도 탭과 Kakao Local 검색 결과 선택으로 목적지 좌표를 저장한다.
- 홈 바텀시트에서 알람을 생성하고 Supabase `alarms` insert 후 Room cache에 반영한다.
- 알람 목록은 Room flow 실제 데이터로 표시한다.
- 활성 알람 기반 foreground 위치 추적과 반경 진입 트리거를 1차 구현했다.
- Pretendard 폰트를 앱 기본 폰트로 적용했다.

## 주요 제약

- Kotlin은 `2.0.21` 기준이다.
- Maps Compose는 Kotlin metadata 호환 문제 때문에 `6.4.1`을 사용한다.
- 위치 업데이트는 Balanced Priority 기준이다.
- background location은 아직 범위 밖이다.
- Android 13+ `POST_NOTIFICATIONS` 권한 UX는 아직 보강 필요하다.
- Supabase Realtime, Storage, FCM, 친구/권한, 커스텀 알람음은 아직 본격 구현 전이다.

## 환경 키

`local.properties`에 아래 키를 둔다.

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `GOOGLE_MAPS_API_KEY`
- `KAKAO_NATIVE_APP_KEY`
- `KAKAO_REST_API_KEY`
- `FIREBASE_PROJECT_ID`

service role key는 앱에 넣지 않는다.

## 검증 이력

아래 주요 단계 후 `assembleDebug`와 `test`가 성공했다.

- Supabase Auth 연결
- Google Maps Compose 적용
- foreground 위치 권한/현재 위치/목적지 선택
- 알람 생성 저장/Supabase insert/Room 목록 연결
- foreground 위치 추적/알람 트리거/Kakao Local 검색
- Pretendard 폰트 적용

## 다음 작업 후보

- 다음 작업 시작 시 `wakepoint-start` 또는 관련 모듈 Skill을 먼저 사용한다.
- `POST_NOTIFICATIONS` 권한 요청 UX
- 알람 트리거 실기기 또는 emulator mock location 검증
- Supabase `alarms` RLS 실DB 검증
- Kakao 검색 debounce, empty/error state
- 친구/권한/대리 알람 Repository와 화면
- 커스텀 알람음 녹음, 업로드, 반복 재생
