# 2026-11-19 Remaining Features

브랜치: `design-reset`

## 완료한 작업

- `docs/REMAINING.md` 체크리스트 22개 중 19개 구현 (커밋 0f3b44a ~ 344579c).
  - 알람음 재생, 선택, 가져오기, 녹음, Storage 업로드
  - 백그라운드 위치 권한 단계 요청, AlarmWorker 제거, 상태바 단색 아이콘
  - 알람 초대 스키마와 보내기/수락/거절/취소, 30초 폴링 갱신
  - 비밀번호 찾기, Google 로그인, 프로필 닉네임 편집
  - FCM 토큰 업로드와 수신 알림, 초대 푸시 Edge Function
  - 단위 테스트 22개
- 외부 설정
  - Supabase: migration 8개 `db push --remote` 적용, `FCM_SERVICE_ACCOUNT` 시크릿, `notify-alarm-invitation` 배포, `alarm_invitations` INSERT 웹훅 연결
  - Firebase: `app/google-services.json` 배치(gitignore), Google Services 플러그인 4.4.2 적용
- Impeccable 디자인 스킬을 `.claude/skills`, `.claude/agents`에 설치. 개인 설정 `settings.local.json`은 gitignore.

## 검증

- `assembleDebug`: 성공
- `test`: 성공, 22개 통과
- Edge Function 더미 호출: HTTP 200

## 남은 이슈

- blocked: 18 전화번호 인증(SMS provider 미정), 21 이메일 확인 정책 실계정 검증, 22 실기기 검증
- 대시보드 미완: Storage 버킷 `alarm-sounds`와 정책, Auth redirect/Kakao/Confirm email 확인, Google provider
