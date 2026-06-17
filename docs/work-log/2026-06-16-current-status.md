# 2026-06-16 Current Status

## 현재까지 완료한 작업

- Android Native 단일 타겟 프로젝트로 기본 구조를 정리했다.
- Gradle/Kotlin/Compose 기반 앱 모듈이 빌드 가능한 상태다.
- Hilt, KSP, Room, DataStore, WorkManager, FCM, Fused Location 의존성을 추가했다.
- `WakepointApplication`, `MainActivity`, 서비스, Worker에 Hilt 기반 초기 골격을 연결했다.
- Room 기반 `WakepointDatabase`, `AlarmDao`, `AlarmEntity`, `AlarmRepository` 기본 구조를 만들었다.
- FCM 토큰을 DataStore에 저장하는 기본 서비스 구조를 만들었다.
- 알림 채널 생성 로직을 추가했다.
- 문서 파일은 `docs/` 아래로 정리했다.
- 화면설계 이미지는 `docs/design/screens/`에 보관 중이다.
- 디자인 시안을 참고해 Compose 목업 UI를 정리했다.
  - 스플래쉬
  - 로그인
  - 회원가입
  - 지도 홈
  - 알람 설정 바텀시트
  - 알람 목록
  - 알람 소리 목록
  - 음성 녹음 모달
  - 친구 목록
  - 친구에게 알람 보내기 바텀시트
  - 프로필
- `core/design` 공통 컴포넌트를 확장했다.
  - 로고
  - 헤더
  - 기본/보조 버튼
  - 입력창
  - 반경 선택
  - 사운드 선택 행
  - 바텀시트 핸들
  - 지도 마커 프리뷰
- `Navigation Compose` route를 확장했다.
  - `splash`
  - `auth`
  - `sign-up`
  - `sound-list`
  - `home`
  - `alarms`
  - `friends`
  - `profile`
- 앱 표시 문구는 `app/src/main/res/values/strings.xml`로 정리했다.
- 이메일 기반 인증 흐름의 1차 구현을 추가했다.
  - `AuthRepository`, `DefaultAuthRepository`
  - `AuthViewModel`, `AuthUiState`
  - Supabase Auth REST 기반 이메일 로그인/회원가입
  - 회원가입 성공 시 `user_profiles` upsert로 DB 프로필 생성 보강
  - DataStore 기반 auth session 저장/삭제
  - 로그인 상태에 따른 `splash` -> `auth`/`home` Navigation 분기
  - 프로필 화면 로그아웃 연결
  - 로그인/회원가입 입력 필드 상태 연결 및 기본 검증
- MVP 범위에서는 전화번호 인증 UI를 회원가입 화면에서 숨기고 이메일 회원가입 안정화에 집중하도록 정리했다.
- 홈 화면 지도 목업을 Google Maps Compose 기반 실제 지도 화면으로 교체했다.
  - `GOOGLE_MAPS_API_KEY`가 `local.properties`에 입력된 것을 값 노출 없이 확인했다.
  - `play-services-maps`, `maps-compose` 의존성을 추가했다.
  - 기본 카메라는 서울 시청 좌표로 설정했다.
  - 기존 검색창, 중앙 마커 프리뷰, 현재 위치 FAB, 알람 생성 버튼 오버레이는 유지했다.
- 홈 화면 foreground 위치 기능을 1차 연결했다.
  - `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` 런타임 권한을 요청한다.
  - 권한 허용 시 `FusedLocationProviderClient`의 Balanced Priority 현재 위치를 가져온다.
  - 현재 위치를 기본 카메라와 기본 목적지로 사용한다.
  - 위치를 가져오지 못하면 서울 시청 좌표로 fallback한다.
  - 권한이 있을 때만 Google Map `isMyLocationEnabled`를 켠다.
  - 지도 탭 좌표를 목적지로 저장하고 선택 마커와 알람 설정 바텀시트에 전달한다.
- 알람 생성/목록의 실제 데이터 흐름을 1차 연결했다.
  - 홈 알람 바텀시트의 별칭, 반경, 선택 좌표를 state로 관리한다.
  - 알람 저장 시 로그인 유저 id로 `owner_id`, `created_by`를 설정한다.
  - `AlarmDto`와 Domain -> DTO mapper를 추가했다.
  - Supabase REST로 `alarms` insert 성공 후 Room `alarms`에 upsert한다.
  - 알람 활성 상태 변경과 삭제를 Supabase REST + Room에 연결했다.
  - 알람 목록 화면에서 목업 데이터를 제거하고 `AlarmRepository.observeAlarms()`를 구독한다.
  - 알람 목록은 활성/지난 알람으로 구분해 표시한다.
- foreground 위치 추적과 알람 트리거 판단을 1차 구현했다.
  - 활성 알람 1개 이상이면 `LocationTrackingService`를 foreground service로 시작한다.
  - 활성 알람 0개면 위치 추적 서비스를 중단한다.
  - `LocationTrackingService`는 Balanced Priority 위치 업데이트를 구독한다.
  - `calculateDistance()`로 현재 위치와 알람 목적지 사이 거리를 계산한다.
  - 반경 진입 시 도착 알림을 표시하고 해당 알람을 `is_active=false`, `triggered_at=now`로 업데이트한다.
  - foreground 추적 알림과 도착 알림 생성 로직을 `AlarmNotificationManager`에 추가했다.
- Kakao Local 장소 검색을 1차 구현했다.
  - `KAKAO_REST_API_KEY`로 Kakao Local keyword search REST API를 호출한다.
  - 검색창 클릭 시 장소 검색 바텀시트를 표시한다.
  - 검색 결과 선택 시 지도 카메라 이동, 목적지 좌표, 목적지 주소를 갱신한다.
- 앱 전체 폰트를 Pretendard로 적용했다.
  - Pretendard Light/Regular/SemiBold/Bold font resource를 추가했다.
  - Compose Typography 주요 스타일에 Pretendard FontFamily를 적용했다.
  - XML app theme 기본 `android:fontFamily`도 `@font/pretendard`로 설정했다.

## 최근 관련 커밋

- `0f7ac3e` - `Style : 디자인 시안 기반 Compose UI 정리`
- `54de71b` - `Docs : 화면설계 및 로고, 아이콘 추가`
- `4cd0c58` - `Chore : Android Studio 프로젝트 설정 정리`
- `27d2044` - `Build : Android 핵심 의존성 및 앱 인프라 구성`
- `4773210` - `Docs : 프로젝트 문서 위치 정리`

## 검증 상태

- `assembleDebug` 성공
- `test` 성공
- 2026-06-16 인증 흐름 추가 후 `test` 성공
- 2026-06-16 인증 흐름 추가 후 `assembleDebug` 성공
- 2026-06-16 회원가입 DB 프로필 upsert 보강 후 `test` 성공
- 2026-06-16 회원가입 DB 프로필 upsert 보강 후 `assembleDebug` 성공
- 2026-06-16 전화번호 인증 UI MVP 제외 후 `test` 성공
- 2026-06-16 전화번호 인증 UI MVP 제외 후 `assembleDebug` 성공
- 2026-06-16 Google Maps Compose 홈 화면 교체 후 `assembleDebug` 성공
- 2026-06-16 Google Maps Compose 홈 화면 교체 후 `test` 성공
- 2026-06-16 foreground 위치 권한/현재 위치/지도 탭 목적지 선택 구현 후 `assembleDebug` 성공
- 2026-06-16 foreground 위치 권한/현재 위치/지도 탭 목적지 선택 구현 후 `test` 성공
- 2026-06-16 알람 생성 저장/Supabase insert/실제 목록 연결 후 `assembleDebug` 성공
- 2026-06-16 알람 생성 저장/Supabase insert/실제 목록 연결 후 `test` 성공
- 2026-06-16 foreground 위치 추적/알람 트리거/Kakao Local 검색 구현 후 `assembleDebug` 성공
- 2026-06-16 foreground 위치 추적/알람 트리거/Kakao Local 검색 구현 후 `test` 성공
- 2026-06-16 Pretendard 폰트 적용 후 `assembleDebug` 성공
- 2026-06-16 Pretendard 폰트 적용 후 `test` 성공
- 현재 작업트리는 인증 흐름 변경사항과 기존 `.idea/` 변경사항이 커밋 전 상태다.

## 남은 이슈

- 이메일 로그인/회원가입은 Supabase Auth REST 엔드포인트에 연결했지만, 실제 Supabase 프로젝트 설정과 이메일 확인 정책은 실계정으로 검증해야 한다.
- `local.properties`에는 `SUPABASE_URL`, `SUPABASE_ANON_KEY`가 입력되어 빌드 시 BuildConfig로 주입된다.
- Supabase Auth에서 이메일 확인이 켜져 있으면 회원가입 직후 세션이 없어 홈 진입 대신 확인 메일 안내가 표시된다. MVP 즉시 홈 진입을 원하면 Supabase Auth의 이메일 확인 정책을 확인해야 한다.
- 전화번호 인증은 MVP 이후 Supabase Edge Function + SMS provider로 구현할 예정이다.
- Kakao Login/Share SDK는 아직 연결되지 않았고, 카카오 로그인 버튼은 placeholder 상태다.
- Google Sign-In은 아직 구현되지 않았다.
- 홈/알람/친구 UI는 실제 데이터/권한/네트워크가 연결되지 않은 목업 상태다.
- Google Maps 실제 지도 SDK 화면과 foreground 현재 위치/목적지 탭 선택은 1차 적용됐다.
- Supabase `alarms` insert/update/delete는 REST로 1차 연결됐다. 다만 실기기에서 Supabase RLS/스키마 적용 상태를 검증해야 한다.
- foreground 위치 추적 서비스와 트리거 판단은 1차 구현됐다. Android 13+ 도착 알림 표시는 `POST_NOTIFICATIONS` 런타임 권한 UX를 별도 구현해야 안정적이다.
- Kakao Local 검색은 1차 구현됐다. 실제 검색 성공은 `KAKAO_REST_API_KEY`와 Kakao 앱 설정으로 실기기 검증해야 한다.
- Supabase Realtime, Storage 실제 연동이 아직 없다.
- Firebase `google-services.json`과 Google Services Gradle 플러그인 적용은 아직 보류 중이다.
- 위치 권한 요청, 백그라운드 위치 추적, 알람 트리거 로직은 아직 구현 전이다.
- 알람음 녹음/파일 선택/Media3 반복 재생은 아직 UI 목업 단계다.
- 친구 권한 요청, 대리 알람 설정, FCM 발송은 아직 구현 전이다.
- `docs/IMPLEMENTATION_NOTES.md`는 인코딩 깨짐이 남아 있어 정리 대상이다.

## 다음 작업 추천 순서

1. 알람 생성 실기기 검증
   - Supabase SQL Editor에서 `alarms` 테이블/RLS 적용 확인
   - 앱에서 알람 생성 후 Supabase `alarms` 레코드 생성 확인
   - 알람 목록 화면에 Room cache가 즉시 반영되는지 확인
   - 활성 토글/삭제가 Supabase와 Room에 모두 반영되는지 확인

2. Supabase DB 연동 확장
   - `FriendDto`, `UserProfileDto`, `AlarmPermissionDto`
   - 친구/권한 Repository
   - Realtime 구독

3. 인증 보강
   - Supabase 프로젝트 실계정 로그인/회원가입 검증
   - 이메일 확인 enabled/disabled 정책에 따른 UX 정리
   - Google Sign-In
   - Kakao Login SDK 및 Supabase Auth Provider 연동
   - 서버 오류 메시지 현지화

4. 위치 추적/알림 실기기 검증
   - foreground service 알림 표시 확인
   - Android 13+ `POST_NOTIFICATIONS` 권한 요청 UX 구현
   - 실제 위치 또는 에뮬레이터 mock location으로 반경 진입 테스트
   - 트리거 후 Supabase/Room 알람 비활성화 확인

5. 백그라운드 추적 확장
   - Android 10+ background location 단계적 권한 요청
   - 장시간 추적 배터리/발열 확인
   - Foreground Service 시작 조건과 중단 조건 실기기 검증

5. 친구/권한/대리 알람 구현
   - 친구 추가
   - 권한 요청/수락/거절
   - 권한 있는 친구에게 알람 보내기
   - Realtime 구독
   - FCM 알림

6. 커스텀 알람음 구현
   - MediaRecorder 녹음
   - SAF 파일 선택
   - Supabase Storage 업로드
   - Media3/ExoPlayer 반복 재생

## 앞으로의 작업 로그 운영

- 작업이 끝날 때마다 `docs/work-log/`에 새 파일을 추가한다.
- 커밋 메시지와 검증 결과를 함께 남긴다.
- 다음 작업자가 바로 이어갈 수 있도록 "다음 작업"을 항상 마지막에 적는다.

