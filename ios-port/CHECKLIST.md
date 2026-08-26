# CHECKLIST — iOS 이식 순서

## 0. 프로젝트 셋업
- [ ] Xcode 프로젝트 생성 (SwiftUI App, iOS 17+, Bundle ID: com.wakepoint.app)
- [ ] xcconfig로 비밀키 주입 (SUPABASE_URL/ANON_KEY, KAKAO_REST_API_KEY) — 저장소 커밋 금지
- [ ] Spoqa Han Sans Neo otf 5종 등록(UIAppFonts) — `assets/font/` 참조
- [ ] 색상/타이포/radius 토큰 구현 — `THEME.md`
- [ ] 커스텀 URL Scheme `com.wakepoint.app` 등록 — `MANIFEST.md`
- [ ] 앱 아이콘 제작(adaptive icon XML → 1024 PNG 재작업), 로고 벡터 변환(logo_square/logo_point)

## 1. 데이터 모델 + 로컬 저장
- [ ] Swift struct 도메인 모델 (Alarm, Friend, UserProfile, AlarmSound, AlarmPermission…) — `DATA_MODEL.md`
- [ ] SwiftData(또는 GRDB) 스키마: alarms, friends (Room v8 대응)
- [ ] UserDefaults/Keychain: 세션 토큰, defaultSoundUri, pendingFriendAlarms, pendingDeletions
- [ ] 순수 로직 이식 + 단위 테스트: calculateDistance(하버사인), radiusOptionToKm, accuracyBufferKm, isWithinRadius, decideAlarmTriggerAction (기존 AlarmRetriggerTest 케이스 재사용)

## 2. 네트워크 레이어
- [ ] URLSession + Codable Supabase 클라이언트 (auth/v1, rest/v1) — `API.md`
- [ ] 토큰 갱신(refreshSessionIfNeeded) + AuthHttpException 상응 에러 타입 + retryingNetwork 재시도
- [ ] Kakao Local API (keyword/address/coord2address)
- [ ] Repository protocol 구성: Alarm/Friend/Auth/Sound

## 3. 인증 플로우
- [ ] Splash → 세션 확인 → Auth/메인 분기
- [ ] 이메일 로그인/회원가입/비밀번호 재설정
- [ ] Kakao OAuth (ASWebAuthenticationSession + 콜백 파싱)
- [ ] 로그아웃, 닉네임 변경

## 4. 화면 (SPEC.md 순서)
- [ ] 탭 셸 + 플로팅 탭 바 (지도/알람/친구/마이)
- [ ] HomeScreen: MapKit 지도, 장소 검색, AlarmSetupSheet(별칭/반경/사운드/소리/진동)
- [ ] AlarmsScreen: 즐겨찾기/활성/지난 섹션, 토글, 다중 선택 삭제, 편집 시트, 빈 상태
- [ ] SoundListScreen: 번들/커스텀 목록, 미리듣기, 녹음(AVAudioRecorder .m4a), 파일 가져오기(DocumentPicker), 삭제
- [ ] FriendsScreen: 검색/요청/수락/거절/차단/삭제, 알람 권한 요청/수락, 친구에게 알람 보내기 시트, 카카오 초대 공유
- [ ] ProfileScreen: 통계 3종, 설정 목록, 닉네임 변경, 로그아웃
- [ ] 공용: 에러 다이얼로그, 로딩 상태, 확인 다이얼로그

## 5. 백그라운드 위치 + 알람 발동 (핵심)
- [ ] 권한 플로우: WhenInUse → Always 승격 + 알림 권한 (StartupPermissions 대응)
- [ ] CLLocationManager 백그라운드 업데이트 + CLCircularRegion 모니터링(활성 알람 동기화, 20개 제한 처리)
- [ ] 위치 업데이트 → decideAlarmTriggerAction → FIRE/REARM/NONE 처리
- [ ] 발동: 포그라운드 풀스크린 알람 뷰 + AVAudioPlayer 루프 + 햅틱; 백그라운드 time-sensitive 알림(커스텀 사운드) + "알람 끄기" 액션
- [ ] 비반복 알람 발동 후 isActive=false / repeating 알람 재무장 로직
- [ ] 활성 알람 0개 시 추적 중지

## 6. 동기화 + 푸시
- [ ] 로컬 우선 쓰기 + pendingSync/pendingDeletions 큐 재전송
- [ ] scenePhase(.active) 복귀 동기화 + BGAppRefreshTask 주기 동기화
- [ ] 서버 fetch 시 deleteSyncedAlarmsNotIn 규칙(꺼진/삭제 알람 부활 방지) 이식
- [ ] APNs 등록 + push_token 업로드; 친구 알람 수신(Notification Service Extension) → DB 반영 → 추적 갱신
- [ ] 서버 푸시 발송부(supabase/ 함수) APNs 지원 여부 확인/수정

## 7. 테스트/마무리
- [ ] 단위 테스트: 트리거 결정, 거리/반경, 동기화 병합 규칙, DTO 인코딩
- [ ] 시나리오 테스트: 백그라운드/앱 종료/재부팅 후 알람 발동, 반복 알람 재발동, 오프라인 저장→온라인 동기화
- [ ] 실기기 배터리/백그라운드 위치 정확도 검증 (시뮬레이터 GPX 경로 재생)
- [ ] 개인정보: 위치·친구 알람 관련 docs/DESTINATION_ALARM_PRIVACY.md 반영, App Privacy 라벨 작성
