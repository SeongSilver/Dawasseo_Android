# Gotchas

이 문서는 다왔어 Android 개발 중 반복해서 밟기 쉬운 함정과 이미 확인된 제약을 모아둔 하네스다.
새 기능을 만들기 전, 관련 항목을 먼저 훑고 같은 문제를 재현하지 않도록 한다.

## 빌드와 의존성

- Gradle wrapper 또는 dependency 다운로드는 Codex sandbox 네트워크 제한 때문에 실패할 수 있다.
  - `SocketException`, `Permission denied: getsockopt`, DNS 오류가 보이면 같은 명령을 escalated 권한으로 재시도한다.
- 현재 프로젝트 Kotlin은 `2.0.21` 기준이다.
  - `com.google.maps.android:maps-compose:6.7.0`은 Kotlin metadata 버전 불일치가 발생했다.
  - Kotlin을 올리기 전까지 Maps Compose는 `6.4.1`을 유지한다.
- Android resource 파일명은 소문자와 underscore만 안전하다.
  - 폰트 파일도 `pretendard_regular.otf`처럼 소문자 이름을 사용한다.

## 환경 변수와 키

- `local.properties` 키 이름은 정확히 맞춘다.
  - `SUPABASE_URL`
  - `SUPABASE_ANON_KEY`
  - `GOOGLE_MAPS_API_KEY`
  - `KAKAO_NATIVE_APP_KEY`
  - `KAKAO_REST_API_KEY`
  - `FIREBASE_PROJECT_ID`
- Supabase anon key는 클라이언트에 포함 가능하지만 service role key는 절대 앱에 포함하지 않는다.
- Google Maps API key는 출시 전 Android package name + SHA-1 제한을 걸어야 한다.
- Kakao Local 검색은 Native App Key가 아니라 REST API Key를 사용한다.

## Supabase

- 회원가입 직후 홈 진입 가능 여부는 Supabase Auth의 이메일 확인 정책에 영향을 받는다.
  - 이메일 확인이 켜져 있으면 세션이 바로 없을 수 있고, 이때는 확인 메일 안내가 정상 흐름이다.
- `alarms` insert/update/delete는 RLS와 schema가 라이브 DB에 반영되어 있어야 동작한다.
  - 앱에서 저장 실패가 나면 먼저 Supabase SQL Editor에서 `alarms` 테이블과 RLS 정책을 확인한다.
- Repository 밖에서 Supabase 쿼리를 직접 호출하지 않는다.
  - UI와 ViewModel은 Repository action만 호출한다.

## 위치와 알림

- 첫 단계는 foreground 위치 권한만 사용한다.
  - Android 10+ background location은 별도 단계에서 추가한다.
- 위치 업데이트는 Balanced Priority 기준이다.
  - High Accuracy 상시 사용은 배터리와 발열 위험 때문에 금지한다.
- 거리 계산은 `core/location/DistanceCalculator.kt`의 `calculateDistance()`만 사용한다.
- 활성 알람이 0개가 되면 위치 추적은 즉시 중단되어야 한다.
- Android 13+ 알림 표시는 `POST_NOTIFICATIONS` 런타임 권한이 필요하다.
  - 현재 트리거 알림은 권한이 없으면 표시되지 않을 수 있으므로 권한 UX를 별도로 구현해야 한다.

## 지도와 검색

- 지도 blank 화면은 보통 API key, API enablement, SHA-1/package 제한, emulator Play Services 문제 중 하나다.
- 현재 기본 위치 fallback은 서울 좌표다.
  - 위치 권한을 허용하고 현재 위치를 가져오면 내 위치가 기본 목적지 후보가 된다.
- Kakao Local 검색 실패 시 `KAKAO_REST_API_KEY`, Kakao 앱 설정, 네트워크, quota를 순서대로 확인한다.

## 디자인과 리소스

- 앱 폰트는 Pretendard를 기본으로 사용한다.
- Accent color는 `#0066cc` 하나만 사용한다.
- font weight `500`은 쓰지 않고 `300`, `400`, `600` 중심으로 맞춘다.
- UI 문자열은 가능한 `strings.xml`로 분리한다.

