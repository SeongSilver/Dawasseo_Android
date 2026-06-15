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

## 최근 관련 커밋

- `0f7ac3e` - `Style : 디자인 시안 기반 Compose UI 정리`
- `54de71b` - `Docs : 화면설계 및 로고, 아이콘 추가`
- `4cd0c58` - `Chore : Android Studio 프로젝트 설정 정리`
- `27d2044` - `Build : Android 핵심 의존성 및 앱 인프라 구성`
- `4773210` - `Docs : 프로젝트 문서 위치 정리`

## 검증 상태

- `assembleDebug` 성공
- `test` 성공
- 현재 작업트리는 기록 작성 전 기준으로 깨끗했다.

## 남은 이슈

- 현재 UI는 실제 데이터/권한/네트워크가 연결되지 않은 목업 상태다.
- Google Maps 실제 지도 SDK 화면은 아직 적용되지 않았다.
- Supabase Auth, DB, Realtime, Storage 실제 연동이 아직 없다.
- Kakao Login/Share SDK는 아직 연결되지 않았다.
- Firebase `google-services.json`과 Google Services Gradle 플러그인 적용은 아직 보류 중이다.
- 위치 권한 요청, 백그라운드 위치 추적, 알람 트리거 로직은 아직 구현 전이다.
- 알람음 녹음/파일 선택/Media3 반복 재생은 아직 UI 목업 단계다.
- 친구 권한 요청, 대리 알람 설정, FCM 발송은 아직 구현 전이다.
- `docs/IMPLEMENTATION_NOTES.md`는 인코딩 깨짐이 남아 있어 정리 대상이다.

## 다음 작업 추천 순서

1. 인증 흐름 구현
   - `AuthViewModel`
   - `AuthRepository`
   - Supabase Auth 클라이언트 구성
   - 이메일 로그인/회원가입
   - 로그인 상태에 따른 Navigation 분기

2. Supabase DB 연동
   - `AlarmDto`, `FriendDto`, `UserProfileDto`, `AlarmPermissionDto`
   - DTO와 Domain mapper
   - 알람 insert/select/update
   - Room cache 동기화

3. 지도 홈 실제 기능 연결
   - Google Maps SDK 적용
   - 현재 위치 foreground 권한 요청
   - Kakao Local API 장소 검색
   - 목적지 선택 및 반경 설정
   - 알람 생성 UseCase/ViewModel 연결

4. 위치 추적과 알람 트리거 구현
   - Android 10+ background location 단계적 권한 요청
   - Android 13+ notification 권한 요청
   - `LocationTrackingService`에서 Balanced Priority 위치 업데이트
   - `calculateDistance()` 기반 반경 진입 판단
   - 트리거 후 알람 비활성화 및 활성 알람 0개 시 추적 중단

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

