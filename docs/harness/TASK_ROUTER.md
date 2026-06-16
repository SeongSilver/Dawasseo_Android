# Task Router Harness

사용자 요청을 받았을 때 어느 하네스와 파일부터 열지 정하는 라우터다.
작업 시작 시 이 문서에서 관련 섹션 하나만 고른다.

## 인증

읽을 문서:

- `docs/CHECKLISTS.md`
- `docs/GOTCHAS.md`

먼저 볼 파일:

- `app/src/main/java/com/wakepoint/app/data/auth/AuthRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/auth/DefaultAuthRepository.kt`
- `app/src/main/java/com/wakepoint/app/feature/auth/AuthViewModel.kt`
- `app/src/main/java/com/wakepoint/app/feature/auth/AuthScreen.kt`
- `app/src/main/java/com/wakepoint/app/navigation/WakepointNavGraph.kt`

주요 검증:

- 회원가입
- 이메일 확인 정책별 UX
- 로그인 후 홈 진입
- 로그아웃 후 인증 화면 복귀

## 홈 지도와 위치

읽을 문서:

- `docs/harness/maps-location-flow.md`
- `docs/GOTCHAS.md`

먼저 볼 파일:

- `app/src/main/java/com/wakepoint/app/feature/home/HomeScreen.kt`
- `app/src/main/java/com/wakepoint/app/feature/home/HomeViewModel.kt`
- `app/src/main/java/com/wakepoint/app/data/location/KakaoLocalRepository.kt`
- `app/src/main/AndroidManifest.xml`

주요 검증:

- 지도 렌더링
- foreground 위치 권한
- 현재 위치 fallback
- 지도 탭 목적지 선택
- Kakao 검색 결과 선택

## 알람 저장과 목록

읽을 문서:

- `docs/harness/alarm-trigger-flow.md`
- `docs/CHECKLISTS.md`

먼저 볼 파일:

- `app/src/main/java/com/wakepoint/app/data/alarm/AlarmRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/DefaultAlarmRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/local/AlarmDao.kt`
- `app/src/main/java/com/wakepoint/app/feature/alarms/AlarmsViewModel.kt`
- `app/src/main/java/com/wakepoint/app/feature/alarms/AlarmsScreen.kt`

주요 검증:

- Supabase insert/update/delete
- Room cache 반영
- 활성/지난 알람 분리
- 삭제와 비활성화 action

## 위치 추적과 트리거

읽을 문서:

- `docs/harness/alarm-trigger-flow.md`
- `docs/harness/VERIFICATION_MATRIX.md`

먼저 볼 파일:

- `app/src/main/java/com/wakepoint/app/service/LocationTrackingService.kt`
- `app/src/main/java/com/wakepoint/app/core/location/LocationTrackingController.kt`
- `app/src/main/java/com/wakepoint/app/core/location/DistanceCalculator.kt`
- `app/src/main/java/com/wakepoint/app/core/notification/AlarmNotificationManager.kt`
- `app/src/main/AndroidManifest.xml`

주요 검증:

- 활성 알람 1개 이상이면 추적 시작
- 활성 알람 0개면 추적 중단
- Balanced Priority
- 반경 진입 시 알림
- 트리거 후 알람 비활성화

## 알림 권한

읽을 문서:

- `docs/harness/alarm-trigger-flow.md`
- `docs/CHECKLISTS.md`

먼저 볼 파일:

- `app/src/main/java/com/wakepoint/app/core/notification/AlarmNotificationManager.kt`
- `app/src/main/java/com/wakepoint/app/service/LocationTrackingService.kt`
- `app/src/main/java/com/wakepoint/app/feature/home/HomeScreen.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`

주요 검증:

- Android 13+ `POST_NOTIFICATIONS` 요청
- 거부 시 앱 crash 없음
- 허용 후 foreground/arrival notification 표시

## 친구/권한/대리 알람

읽을 문서:

- `docs/SCHEMA.md`
- `docs/CHECKLISTS.md`

먼저 만들 파일 후보:

- `app/src/main/java/com/wakepoint/app/data/friend/FriendRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/friend/DefaultFriendRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/friend/remote/FriendDto.kt`
- `app/src/main/java/com/wakepoint/app/data/friend/remote/AlarmPermissionDto.kt`
- `app/src/main/java/com/wakepoint/app/feature/friends/FriendsViewModel.kt`
- `app/src/main/java/com/wakepoint/app/feature/friends/FriendsScreen.kt`

주요 검증:

- 친구 추가
- 권한 요청/수락/거절
- 권한 있는 대상에게 알람 생성
- Supabase RLS

## 커스텀 알람음

읽을 문서:

- `docs/SCHEMA.md`
- `docs/CHECKLISTS.md`

먼저 만들 파일 후보:

- `app/src/main/java/com/wakepoint/app/data/recording/RecordingRepository.kt`
- `app/src/main/java/com/wakepoint/app/feature/alarms/AlarmSoundPicker.kt`
- `app/src/main/java/com/wakepoint/app/service/AlarmPlayerService.kt`

주요 검증:

- 녹음 권한
- SAF 파일 선택
- Supabase Storage upload
- Media3 반복 재생
- 알람 중지 UX

## 디자인/폰트

읽을 문서:

- `docs/DESIGN.md`
- `docs/GOTCHAS.md`

먼저 볼 파일:

- `app/src/main/java/com/wakepoint/app/core/design/Color.kt`
- `app/src/main/java/com/wakepoint/app/core/design/Type.kt`
- `app/src/main/java/com/wakepoint/app/core/design/Component.kt`
- `app/src/main/res/font/`
- `app/src/main/res/values/styles.xml`

주요 검증:

- Pretendard 적용
- accent color 단일 사용
- font weight 300/400/600 중심
- 문자열 overflow 없음

