# 남은 기능 체크리스트

작성일: 2026-11-19. 디자인 작업은 제외한다. 상태 값: `todo`, `now`, `done`, `blocked`.
완료 확인 방법을 통과하면 체크하고 커밋한다. 같은 항목에서 3번 실패하면 `blocked`로 표시하고 이유를 적는다.

| # | 항목 | 상태 | 비고 |
|---|---|---|---|
| 1 | 알람 소리 재생 | done | 2026-11-19 완료. MediaPlayer 알람 스트림, 테스트 5개 |
| 2 | 백그라운드 위치 권한 흐름 | done | 2026-11-19 완료. API 29+ 단계 요청, 테스트 4개 |
| 3 | AlarmWorker 정리 | done | 2026-11-19 완료. 참조 0건 |
| 4 | 알림 상태바 단색 아이콘 | done | 2026-11-19 완료. ic_stat_alarm |
| 5 | 홈/친구 하드코딩 문자열 strings 이관 | done | 2026-11-19 완료. grep 0건 |
| 6 | 알람음 선택 저장 + 파일 가져오기 | done | 2026-11-19 완료. SoundListViewModel |
| 7 | 알람음 녹음 | done | 2026-11-19 완료. MediaRecorder m4a |
| 8 | 알람 초대 스키마 | done | 2026-11-19 완료. migration + SCHEMA.md |
| 9 | 친구에게 알람 보내기 + 초대 수락/거절 + 로컬 알람 등록 | done | 2026-11-19 완료. 초대 send/accept/reject/cancel |
| 10 | 친구/초대 상태 자동 갱신 | done | 2026-11-19 완료. 30초 폴링 |
| 11 | 비밀번호 찾기 | done | 2026-11-19 완료. /auth/v1/recover |
| 12 | Google 로그인 | done | 2026-11-19 완료. provider=google, 대시보드 설정 별도 |
| 13 | 프로필 ViewModel + 닉네임 편집 | done | 2026-11-19 완료. ProfileViewModel |
| 14 | FCM 토큰 서버 업로드 + 수신 알림 표시 | now | 2026-11-19 시작 |
| 15 | 초대 푸시 발송 Edge Function | now | 2026-11-19 시작 |
| 16 | Firebase google-services 적용 | blocked | google-services.json 없음. Firebase 콘솔에서 받아 app/ 에 두면 플러그인 적용 가능 |
| 17 | 알람음 Supabase Storage 업로드 | done | 2026-11-19 완료. bucket alarm-sounds 필요, 테스트 3개 |
| 18 | 전화번호 인증 | blocked | MVP 제외, SMS provider 미정 |
| 19 | 단위 테스트 보강 | done | 2026-11-19 완료. 단위 테스트 22개 통과 |
| 20 | IMPLEMENTATION_NOTES 미결 정리 | done | 2026-11-19 완료. 해결 항목 제거, 외부 설정 항목만 유지 |
| 21 | 이메일 확인 정책 실계정 검증 | blocked | 실계정과 Supabase 대시보드 접근 필요 |
| 22 | 실기기 검증 | blocked | 실기기 없음 |

## 항목 상세와 완료 확인 방법

- [x] **1. 알람 소리 재생**: 도착 알람이 울릴 때 `AlarmAlertService`가 알람 스트림으로 소리를 반복 재생한다. 알람에 `soundUri`가 있으면 그 파일을, 없으면 시스템 기본 알람음을 쓴다. 중지 시 소리도 멈춘다.
  확인: `assembleDebug` 성공, `AlarmSoundPlayer` 단위 테스트(URI 선택 로직) 통과.
- [x] **2. 백그라운드 위치 권한 흐름**: foreground 권한 허용 후 알람 생성 시 Android 10+에서 `ACCESS_BACKGROUND_LOCATION`을 단계적으로 요청하고, 거부 시 설정 이동 안내를 보여준다. 권한 유무는 추적 서비스 시작 조건에 영향을 주지 않는다.
  확인: `assembleDebug` 성공, 권한 분기 헬퍼 단위 테스트 통과.
- [x] **3. AlarmWorker 정리**: 빈 `AlarmWorker`와 관련 Hilt Worker 설정을 제거한다.
  확인: 참조 0건, `assembleDebug` 성공.
- [x] **4. 알림 상태바 단색 아이콘**: 추적 알림과 도착 알림에 단색 벡터 small icon을 지정한다.
  확인: `assembleDebug` 성공, `setSmallIcon`이 새 drawable을 참조.
- [x] **5. 문자열 이관**: `FriendsScreen`과 `HomeScreen`의 한글 리터럴을 strings 리소스로 옮긴다.
  확인: 두 파일에서 `"[가-힣]` grep 0건, `assembleDebug` 성공.
- [x] **6. 알람음 선택 저장 + 파일 가져오기**: 알람음 목록에 ViewModel을 붙여 기본음/파일 선택을 알람의 `soundType`, `soundUri`에 저장한다. SAF로 오디오 파일을 가져오고 persistable 권한을 잡는다.
  확인: `assembleDebug` 성공, 선택 결과가 `AlarmRepository.updateAlarm`으로 전달.
- [x] **7. 알람음 녹음**: `RECORD_AUDIO` 권한 요청 후 MediaRecorder로 녹음해 앱 내부 저장소에 파일을 만들고 알람음으로 선택할 수 있다.
  확인: `assembleDebug` 성공, 녹음 파일 경로가 `soundUri`로 저장.
- [x] **8. 알람 초대 스키마**: `alarm_invitations` 테이블과 RLS migration, `docs/SCHEMA.md` 갱신. 개인정보 원칙의 허용 필드만 둔다.
  확인: migration 파일 존재, 금지 필드 grep 0건.
- [x] **9. 알람 보내기/초대 처리**: 권한 있는 친구에게 목적지, 반경, 별칭으로 초대를 보낸다. 받은 초대를 수락하면 내 기기 Room과 Supabase `alarms`에 `created_by`=요청자인 알람을 만든다. 요청자는 초대 상태(대기/수락/거절/만료/취소)만 본다.
  확인: `assembleDebug` 성공, 금지 함수명/이벤트명 grep 0건.
- [x] **10. 상태 자동 갱신**: 친구 화면이 보이는 동안 주기적으로 친구, 권한, 초대를 다시 불러온다.
  확인: `assembleDebug` 성공.
- [x] **11. 비밀번호 찾기**: 이메일 입력 후 Supabase `/auth/v1/recover` 호출, 결과 안내.
  확인: `assembleDebug` 성공.
- [x] **12. Google 로그인**: 카카오와 같은 브라우저 OAuth 흐름으로 `provider=google` 로그인 버튼을 추가한다.
  확인: `assembleDebug` 성공. Supabase 대시보드 Google provider 설정은 별도.
- [x] **13. 프로필 ViewModel**: `user_profiles`에서 닉네임/이메일을 읽고 닉네임을 수정한다.
  확인: `assembleDebug` 성공.
- [ ] **14. FCM 토큰 업로드 + 수신 알림**: 로그인 후와 토큰 갱신 시 `user_profiles.push_token`을 갱신한다. 수신한 초대 메시지를 알림으로 표시한다.
  확인: `assembleDebug` 성공.
- [ ] **15. 초대 푸시 Edge Function**: `supabase/functions/notify-alarm-invitation`에 초대 insert 시 대상 토큰으로 FCM을 보내는 함수를 둔다. 배포는 별도.
  확인: 함수 소스 존재.
- [ ] **16. google-services 적용**: `google-services.json`과 플러그인 적용.
  확인: 파일 존재, 빌드 성공.
- [x] **17. Storage 업로드**: 커스텀 알람음을 Supabase Storage에 올린다.
  확인: 업로드 Repository 존재, `assembleDebug` 성공.
- [ ] **18. 전화번호 인증**: SMS provider 연동.
- [x] **19. 단위 테스트 보강**: 반경 옵션 변환, 초대 상태 매핑, 사운드 URI 선택 테스트.
  확인: `test` 성공.
- [x] **20. IMPLEMENTATION_NOTES 정리**: 해결된 항목 제거, 남은 미결만 유지.
  확인: 문서 갱신.
- [ ] **21. 이메일 확인 정책 실계정 검증**.
- [ ] **22. 실기기 검증**.
