# SPEC — 다왔어 (Wakepoint) iOS 이식 명세

목적지 반경 진입 시 울리는 위치 기반 도착 알람 앱. 로컬 우선(Room) + Supabase 동기화, 친구에게 알람 대리 설정 기능 포함.

- Android 패키지: `com.wakepoint.app`, minSdk 26 / targetSdk 34, versionName 0.1.0 (표시 버전 문자열 "다왔어 v2.0.0")
- 언어: 한국어 단일
- 딥링크 스킴: `com.wakepoint.app://auth-callback` (Kakao OAuth 콜백)

## 내비게이션 구조 (WakepointNavGraph.kt)

라우트: `splash` → `auth` → (메인 탭) / `sign-up` / `sound-list?alarmId={alarmId}`

메인은 플로팅 탭 바 4개 탭:
| 탭 | 라벨(strings) | 화면 |
|---|---|---|
| Home | 지도 (`tab_home`) | HomeScreen |
| Alarms | 알람 (`tab_alarms`) | AlarmsScreen |
| Friends | 친구 (`tab_friends`) | FriendsScreen |
| Profile | 마이 (`tab_profile`) | ProfileScreen |

전역: `StartupPermissionRequester`(시작 시 위치/알림 권한 요청), `AlarmDisplayPermissionGate`(알람 표시 권한 안내 다이얼로그), `AppSyncViewModel`(포그라운드 복귀 시 동기화), `WakepointErrorDialog`(공용 에러 다이얼로그).

## 화면별 명세

### 1. SplashScreen (`splash`)
- UI: 로고(logo_square/logo_point), 태그라인 `splash_tagline`("목적지 도착 알림의 시작").
- 상태: `AuthUiState.isCheckingSession` — 세션 확인 중 표시.
- 전환: 저장된 세션 유효 → 메인 탭, 없음 → `auth`.

### 2. AuthScreen (`auth`) — AuthViewModel
- UI 요소: 아이디(이메일)/비밀번호 입력, 로그인 버튼, "카카오로 로그인" 버튼(kakao_login_medium_narrow.png), "또는" 구분선, "비밀번호를 잊으셨나요?"(비밀번호 재설정 메일), "회원가입" 링크, 약관 동의 문구, 저작권.
- 상태(AuthUiState): isCheckingSession, session, loginEmail, loginPassword, signUp*, isSubmitting, errorMessage, infoMessage. `isAuthenticated = session != null`.
- 액션: signIn(), signUp(), kakaoOAuthUrl()(외부 브라우저 오픈), sendPasswordReset(), signOut(), consumeMessage().
- 규칙: 이메일/비번 로그인은 Supabase `password` grant. Kakao는 `/auth/v1/authorize?provider=kakao` → 콜백 `com.wakepoint.app://auth-callback` 처리(completeOAuthSignIn). 로그인 성공 시 푸시 토큰 업로드(PushTokenSyncManager.sync).
- 전환: 인증 성공 → 메인 탭. "회원가입" → `sign-up`.

### 3. SignUpScreen (`sign-up`)
- UI: 아이디(이메일), 비밀번호, 비밀번호 확인, 이름(닉네임) 입력 + 확인 버튼. (strings에 생년월일/전화/인증번호 힌트가 있으나 ViewModel 상태는 email/password/passwordConfirm/name 4개만 사용.)
- 규칙: 비밀번호 = 확인 일치 필수. 성공 시 세션 시작 또는 이메일 확인 안내(infoMessage).

### 4. HomeScreen (지도 탭) — HomeViewModel
- UI: Google Map(iOS는 MapKit 대체), 검색 바(`home_search_placeholder` "어디로 가시나요?"), 검색 결과 리스트, 지도 탭으로 위치 선택, 현재 위치 버튼, "알람 만들기" 버튼 → AlarmSetupSheet(바텀시트).
- AlarmSetupSheet UI: 위치 별칭 입력, 선택한 목적지 표시(주소/좌표 "위도 %1$.5f, 경도 %2$.5f"), 반경 선택 RadiusSelector(옵션: 100m/300m/500m/1km/3km/10km, 기본 500m), 알람 사운드 표시(변경은 마이>알람 사운드 관리), 소리 토글, 진동 토글, 저장 버튼.
- 상태(HomeUiState): alarmLabel, radiusOption, isSavingAlarm, saveErrorMessage, saveSucceeded, searchQuery, searchResults(List<PlaceSearchResult>), isSearching, searchErrorMessage, selectedSoundTitle(기본 "기본 벨소리"), selectedSoundUri, selectedSoundIsCustom, soundEnabled(true), vibrate(true).
- 액션: searchPlaces()(Kakao 키워드+주소 검색 병합, id로 dedupe), resolveTargetAddress(역지오코딩), saveAlarm()(로컬 저장 → 서버 업서트, 실패 시 pendingSync), syncLocationTracking()(활성 알람 수에 따라 추적 서비스 시작/중지).
- 규칙: 라벨 미입력 시 기본 라벨 "목적지 알람"(`alarm_default_label`). 저장 성공 → saveSucceeded, 시트 닫기.

### 5. AlarmsScreen (알람 탭) — AlarmsViewModel
- UI: 요약 헤더("지금 %1$d개의 알람이 지켜보고 있어요" / "모든 알람이 꺼져 있어요"), 섹션 3개: 즐겨찾기 / 활성 알람 / 지난 알람. 알람 카드: 라벨, "반경 %1$@ · %2$@"(반경·주소), 켜기/끄기 토글, 즐겨찾기 토글, 직접 설정/친구가 설정 뱃지. 롱프레스 → 다중 선택 모드("%1$d개 선택", 전체 선택, 선택 삭제, 종료). 빈 상태: "아직 알람이 없어요" + "첫 알람 만들기" 버튼. 삭제 확인 다이얼로그(단건/다건).
- 알람 편집: 카드 탭 → 설정 시트(라벨, 반경, 소리/진동/계속 유지(repeating) 토글, 사운드 선택 → SoundListScreen), updateAlarmSettings().
- 상태(AlarmsUiState): alarms, isUpdating, updatingAlarmIds, soundTitles(uri→제목 맵), errorMessage. 파생: favoriteAlarms / activeAlarms(활성·비즐겨찾기) / inactiveAlarms.
- 액션: refreshAlarms(), setAlarmActive(), setFavorite(), updateAlarmSettings(), deleteAlarm(), deleteAlarms(Set).
- 규칙: 토글/삭제는 로컬 즉시 반영(옵티미스틱) 후 서버 반영, 실패 시 pendingSync/pendingDeletion 큐.

### 6. SoundListScreen (`sound-list?alarmId=`) — SoundListViewModel
- UI: 섹션 "기본 제공"(번들 사운드) / "내 알림음"(녹음/가져오기/기기 알람음). 상단 액션: 가져오기(파일 피커, 오디오), 녹음하기(녹음 시트), 기기 알람음(시스템 링톤 피커). 행 탭 = 선택 + 미리듣기(SoundPreviewPlayer). 커스텀 사운드 삭제(확인 다이얼로그).
- 녹음 시트: Idle("아래 버튼을 눌러 녹음을 시작하세요") → Recording("녹음 중…", 경과 초) → Recorded("녹음 완료", 다시 녹음/저장). 마이크 권한 필요.
- 상태(SoundListUiState): sounds, selectedUri, isPerAlarm(alarmId 유무), recordSheetVisible, recordingPhase(Idle/Recording/Recorded), elapsedSeconds, errorMessage. 파생: bundledSounds/customSounds.
- 규칙: `alarmId` 있으면 해당 알람의 사운드 변경, 없으면 기본(디폴트) 사운드 설정(UserPreferences 저장). 녹음 파일 m4a, 파일명 접두사 rec_/imp_/system_/bundled_.

### 7. FriendsScreen (친구 탭) — FriendsViewModel
- UI: 헤더("친구", "서로의 도착을 지켜봐 주는 사이예요"), 새로고침, 친구 검색(이메일/닉네임 → searchUsers), 검색 결과에서 친구 요청 보내기, 친구 목록(아바타/닉네임/이메일/상태), 받은 친구 요청 수락/거절, 친구 삭제/차단, 알람 권한 요청·수락·거절(뱃지: "대리 설정 가능"/"권한 대기"/"거절됨"), "알람 보내기" 버튼 → 친구에게 알람 전송 시트(주소 검색, 라벨, 반경, 소리/진동), 친구 초대 섹션(카카오톡 공유/링크 복사).
- 상태(FriendsUiState): friends, alarmPermissionRequests, currentUserId/Email, searchQuery, searchResults, selectedSearchUserId, isRefreshing, isSearching, updatingFriendIds, updatingPermissionIds, requestingUserIds, errorMessage, searchMessage, sendAlarmFriend, sendAlarmLabel, sendAlarmRadius(기본 500m), sendAlarmSoundEnabled, sendAlarmVibrate, addressQuery, addressResults….
- 액션: refreshFriends, searchUsers, sendFriendRequest, accept/rejectFriendRequest, deleteFriend, blockFriend, requestAlarmPermission, accept/rejectAlarmPermission, openSendAlarm/sendAlarm(fallbackLabel)/dismissSendAlarm, searchAddress/selectAddress.
- 규칙: 알람 보내기는 상대의 permissionStatus == Accepted 필요. 전송된 알람은 상대 기기에서 FCM push 수신(`pendingFriendAlarms` 큐) 후 로컬 DB 반영. 성공 스낵바 "알람을 보냈어요."

### 8. ProfileScreen (마이 탭) — ProfileViewModel
- UI: 프로필(닉네임/이메일, 닉네임 변경 다이얼로그), 통계 3개(깨워드린 횟수 / 활성 알람 / 지켜봐 주는 친구), 설정 목록: 알림 설정(시스템 설정 이동), 위치 권한 관리(시스템 설정), 알람 사운드 관리(→ SoundListScreen), 고객센터(mailto support@wakepoint.app), 로그아웃, 버전 표기("다왔어 v2.0.0").
- 상태(ProfileUiState): nickname, email, wakeupCount, activeAlarmCount, friendCount, isSavingNickname, nicknameErrorMessage.
- 액션: updateNickname()(Supabase user_profiles PATCH), signOut.

### 9. AlarmAlertActivity (전체 화면 알람) — iOS에선 풀스크린 뷰/Critical Alert 대체
- UI: "목적지에 도착했어요" + 알람 라벨 + 목적지 주소 + "알람 끄기" 버튼. 잠금화면 위 표시(showWhenLocked/turnScreenOn).
- 동작: AlarmAlertService가 사운드(AlarmAudioPlayer, 알람 볼륨, 루프) + 진동(AlarmVibrationController) 재생. "알람 끄기" → 사운드/진동 중지, 알림 제거, (비반복 알람이면) 알람 비활성화.
- 전달 데이터(AlarmAlertContract): alarmId, alarmLabel, targetAddress, soundUri, soundEnabled, vibrate.

## 핵심 비즈니스 규칙

1. **반경 트리거 (AlarmRetrigger.kt, 순수 함수 — 그대로 이식)**
   ```
   decideAlarmTriggerAction(reached, repeating, currentlyTriggering):
     reached && !currentlyTriggering            → FIRE   (반경 첫 진입 → 발동)
     !reached && repeating && currentlyTriggering → REARM (반복 알람이 반경 이탈 → 재무장)
     else                                        → NONE
   ```
2. **발동 후처리**: 비반복 알람은 발동 시 isActive=false로 내림(무한 반복 방지, 커밋 88e0ee0). 반복(repeating) 알람은 켜진 채 반경 이탈 시 재무장. `repeating`은 기기 로컬 전용 필드로 서버에 동기화하지 않음.
3. **거리 판정**: 하버사인 거리(calculateDistance) + 위치 정확도 버퍼(accuracyBufferKm, 최대 0.05km). `isWithinRadius(distanceKm, radiusKm, accuracyBuffer)`. 반경 범위 0.1~50km, 기본 0.5km.
4. **위치 추적 수명주기**: 활성 알람 ≥1 → LocationTrackingService(포그라운드, location 타입) 시작 + 상시 알림("활성 알람 %1$d개를 확인하고 있어요."); 0 → 중지. 재부팅/앱 업데이트 후 BootCompletedReceiver가 추적 재개. iOS: 활성 알람 있을 때 allowsBackgroundLocationUpdates + region monitoring 병행 권장.
5. **동기화(로컬 우선)**: Room이 단일 소스. 쓰기 실패 시 pendingSync=true / 삭제 실패 시 pendingDeletions(DataStore). WorkManager 주기 동기화 30분("alarm-periodic-sync") + 포그라운드 복귀 시 동기화(AppSyncViewModel). 서버 fetch 시 pendingSync 아닌 로컬 행만 서버 기준으로 교체(deleteSyncedAlarmsNotIn) — 꺼짐/삭제가 되살아나지 않게(커밋 360ae0d).
6. **세션**: access/refresh 토큰 저장, 만료 임박 시 refreshSessionIfNeeded(). 401 등 실패 구분용 AuthHttpException(statusCode).
7. **에러 표출**: UserFacingError.of() — 기본 문구 "문제가 발생했어요. 잠시 후 다시 시도해 주세요."
