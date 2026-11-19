# IMPLEMENTATION_NOTES.md

구현 중 문서 간 충돌이나 외부 설정이 필요한 미결 사항만 남긴다. 해결된 항목은 삭제한다.
남은 기능 체크리스트는 `docs/REMAINING.md`를 따른다.

갱신일: 2026-11-19

## 우선순위 기준

문서 간 내용이 충돌하면 아래 우선순위를 따른다.

1. `AGENTS.md`
2. `README.md`
3. `SETUP.md`
4. `SCHEMA.md`
5. `API.md`
6. `DESIGN.md`

## 외부 설정이 필요한 항목

### Supabase Auth Provider

- Kakao: 브라우저 OAuth 흐름으로 연결돼 있다. Supabase 대시보드의 Kakao provider 설정과 redirect `com.wakepoint.app://auth-callback` 등록이 필요하다.
- Google: 같은 흐름으로 `provider=google` 버튼이 있다. 대시보드에서 Google provider를 켜고 client id/secret을 넣어야 동작한다.
- 이메일 확인 정책이 켜져 있으면 회원가입 직후 세션이 없어 홈 대신 안내 문구가 표시된다. 실계정으로 확인되지 않았다.

### Supabase Storage

커스텀 알람음 업로드는 `alarm-sounds` 버킷을 전제로 한다. 버킷과 `{user_id}/` 경로 기준 RLS를 대시보드에서 만들어야 한다. 업로드 실패 시 앱은 로컬 URI만 유지한다.

### 초대 푸시 발송

`supabase/functions/notify-alarm-invitation`은 프로덕션에 배포됐고 `FCM_SERVICE_ACCOUNT` 시크릿도 설정됐다 (2026-11-19). 남은 것은 대시보드 Database → Webhooks에서 `alarm_invitations` INSERT 웹훅을 이 함수로 연결하는 일이다. 서비스 계정 JSON은 앱이나 저장소에 포함하지 않는다.

### 전화번호 인증

MVP에서 제외했다. SMS provider를 정한 뒤 Edge Function으로 붙인다.

## 설계상 유지하는 결정

### 알람 반경

DB 제약은 `radius_km >= 0.01 and radius_km <= 50`이다. UI 옵션은 10m에서 10km까지이며 50km 옵션은 UI에서만 제거했다. 기존 데이터 호환을 위해 DB 최대값은 유지한다.

### 대리 알람 사운드

친구가 보낸 초대를 수락해 만들어지는 알람은 기본 알람음만 사용한다. 요청자의 커스텀 사운드를 대상자 기기에서 접근하려면 별도 Storage 정책이 필요하므로 보류한다.

### 개인정보 경계

초대 상태(대기, 수락, 거절, 만료, 취소) 외의 어떤 위치, 도착, 알람 발생 정보도 서버, 푸시, 로그에 남기지 않는다. 상세는 `docs/DESTINATION_ALARM_PRIVACY.md`.
