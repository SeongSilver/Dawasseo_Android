# 다왔어 - Codex 프로젝트 설정

위치 기반 알람 Android 앱. 목적지 도착 시 알람을 울리고, 이후 친구/가족 대리 설정까지 확장한다.
현재 구현은 Android Native + Kotlin + Jetpack Compose 단일 타겟이다.

## 기술 기준

- 플랫폼: Android Native, Android Studio
- 언어/UI: Kotlin, Jetpack Compose, Material 3
- 아키텍처: MVVM + Repository, ViewModel + StateFlow
- 비동기: Kotlin Coroutines + Flow
- DI: Hilt
- 로컬 저장소: DataStore + Room
- 백엔드: Supabase Auth/DB/Realtime/Storage
- 위치/지도: Fused Location Provider + Google Maps SDK + Kakao Local API
- 알림/푸시: Android Notification + Firebase Cloud Messaging
- 빌드: Gradle Kotlin DSL

상세 DB/RLS는 `docs/SCHEMA.md`, 설정은 `docs/SETUP.md`, 디자인은 `docs/DESIGN.md`를 따른다.

## 현재 상태 요약

- 이메일 회원가입/로그인 REST 흐름과 DataStore session 저장이 연결되어 있다.
- 전화번호 인증은 MVP에서 숨기거나 선택 입력으로 낮췄다.
- Google Maps Compose 홈 화면, foreground 위치 권한, 현재 위치 fallback이 구현되어 있다.
- 지도 탭과 Kakao Local 검색 결과 선택으로 목적지 좌표를 저장한다.
- 홈에서 알람 생성 후 Supabase insert와 Room cache 동기화를 수행한다.
- 알람 목록은 실제 Room flow 데이터를 표시한다.
- 알람 별칭/반경 수정은 Supabase PATCH 후 Room cache에 반영한다.
- 활성 알람 기반 foreground 위치 추적과 반경 진입 트리거가 1차 구현되어 있다.
- UI 는 2026-11-21 Montage 디자인(라이트 · 다크)으로 재적용 중이다. Pretendard 폰트 파일이 없어 시스템 폰트를 쓴다.

## 개발 원칙

- 기존 코드 패턴, 모듈 경계, Repository 흐름을 우선한다.
- Supabase 쿼리는 Repository에서만 수행한다.
- DTO와 Domain Model을 분리한다.
- UI 상태는 StateFlow 또는 Compose state로 단방향 관리한다.
- Kotlin null-safety를 지키고 불필요한 `!!`를 쓰지 않는다.
- 사용자 문자열은 `strings.xml`로 분리한다.
- 민감 키는 코드에 하드코딩하지 않는다.
- 사용자 또는 이전 작업자가 만든 unrelated 변경은 되돌리지 않는다.

## 위치/알람 규칙

- 첫 단계는 foreground 위치만 다룬다.
- background location은 별도 단계에서 권한과 UX를 분리한다.
- 위치 업데이트는 Balanced Priority 기준이다.
- 거리 계산은 `core/location/DistanceCalculator.kt`의 `calculateDistance()`만 사용한다.
- 활성 알람이 1개 이상이면 위치 추적을 시작한다.
- 활성 알람이 0개면 위치 추적을 중단한다.
- 트리거 후 서버 갱신 없이 로컬 알람만 울리고 해당 로컬 알람을 삭제한다.
- Android 13+ 알림은 `POST_NOTIFICATIONS` 권한 UX를 별도로 고려한다.
- 친구/대리 알람에서 A는 B의 위치, 도착, 근접, 알람 발생, 확인 상태를 알 수 없어야 한다. 상세 원칙은 `docs/DESTINATION_ALARM_PRIVACY.md`를 따른다.

## 디자인 규칙

전체 규칙은 `docs/DESIGN.md`(라이트 · 다크 2테마, Montage 역할 토큰)를 따른다. 요약:

- 단일 소스(SSOT)는 `core/design/` 의 `Theme.kt`(색) · `Type.kt`(타입) · `Shape.kt`(모서리) ·
  `Spacing.kt`(간격 · 치수) · `Component.kt`(공통 컴포넌트) 다.
- **화면 코드에 hex 를 쓰지 않는다.** `LocalWakepointColors.current.<역할>` 만 참조하고,
  8자리 색의 알파를 바꾸거나 색을 섞지 않는다. `Color(0x…)` 리터럴은 `Theme.kt` 에만 둔다.
- 간격 · 폰트 리터럴도 쓰지 않는다. `WakepointSpacing` · `WakepointSize` ·
  `MaterialTheme.typography.<role>` 만 쓴다 (테두리 두께, 디자인 고유의 구조 치수는 예외).
- 라이트 · 다크 두 테마. `WakepointTheme()` 이 `isSystemInDarkTheme()` 을 따른다.
- 빨강(`negative*`)은 **입력 오류와 실패 메시지에만** 쓴다. 삭제 · 차단 · 로그아웃에 쓰지 않는다.
- 파랑(`brand`)은 핵심 액션과 선택 상태에만 쓴다.
- 굵기는 Regular 400 과 Medium 500 두 가지. Bold 를 쓰지 않고 인라인 `fontWeight` 도 쓰지 않는다.
- 그림자(elevation)를 쓰지 않는다. 면은 채움과 테두리로 구분한다.
- 로딩은 스피너 없이 버튼 문구 변경("처리 중" · "저장 중" · "검색 중") + 비활성으로 표현한다.
- 모서리: 필드 4 · pill 8 · 메시지 12 · 카드 16 · 시트 · 다이얼로그 28 · 버튼 · 검색바는 원형.
- Edge-to-edge 인셋을 처리한다. 스크롤은 목록과 시트 본문 안에서만.
- 폰트는 Pretendard 가 스펙이지만 `res/font` 에 파일이 없어 시스템 폰트로 구현되어 있다.
- 화면을 손대면 `@WakepointScreenPreviews`(라이트 · 다크)로 상태별 미리보기를 둔다.
- 구현 현황과 디자인과 다른 점은 `docs/DESIGN_PROGRESS.md` 에 기록한다.

## 환경 키

`local.properties`에 아래 키를 둔다.

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `GOOGLE_MAPS_API_KEY`
- `KAKAO_NATIVE_APP_KEY`
- `KAKAO_REST_API_KEY`
- `FIREBASE_PROJECT_ID`

Supabase service role key는 절대 앱에 포함하지 않는다.

## 검증

기본 명령:

```powershell
.\gradlew assembleDebug
.\gradlew test
git status --short
```

Gradle이 sandbox 네트워크 제한으로 실패하면 같은 명령을 escalated 권한으로 재시도한다.
문서만 변경한 경우 빌드는 생략 가능하지만 최종 응답에 이유를 남긴다.
작업이 끝나면 검증 후 관련 변경만 묶어 커밋한다. 커밋은 작업 단위로 나누고, unrelated 변경은 포함하지 않는다.

## 문서 운영

- 반복 함정: `docs/GOTCHAS.md`
- 반복 체크리스트: `docs/CHECKLISTS.md`
- API 문서: `docs/API.md`
- DB schema: `docs/SCHEMA.md`
- 설정: `docs/SETUP.md`
- 디자인: `docs/DESIGN.md` (원본 산출물 `design-handoff/`, 구현 현황 `docs/DESIGN_PROGRESS.md`)
- 작업 로그: `docs/work-log/`

작업이 끝나면 필요한 경우 작업 로그만 짧게 갱신한다.
