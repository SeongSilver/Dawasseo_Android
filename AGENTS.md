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
- Pretendard가 앱 기본 폰트로 적용되어 있다.

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
- 트리거 후 해당 알람은 `is_active=false`, `triggered_at` 갱신이 필요하다.
- Android 13+ 알림은 `POST_NOTIFICATIONS` 권한 UX를 별도로 고려한다.

## 디자인 규칙

- Pretendard를 기본 폰트로 사용한다.
- Primary accent는 `#0066cc` 하나만 사용한다.
- font weight는 300/400/600 중심으로 사용하고 500은 피한다.
- 그라데이션 배경과 불필요한 장식은 피한다.
- 카드 안에 카드를 중첩하지 않는다.
- 하단 탭바는 검정 배경 기준을 유지한다.

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
- 디자인: `docs/DESIGN.md`
- 작업 로그: `docs/work-log/`

작업이 끝나면 필요한 경우 작업 로그만 짧게 갱신한다.
