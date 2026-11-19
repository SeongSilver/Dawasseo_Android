# notify-alarm-invitation

`alarm_invitations`에 행이 INSERT될 때 대상 사용자의 FCM 토큰으로 초대 알림을 보내는 Edge Function이다.

보내는 데이터는 `type=alarm_invitation`, `invitation_id`, `requester_nickname`, `label`뿐이다.
위치, 도착, 알람 발생 정보는 다루지 않는다 (`docs/DESTINATION_ALARM_PRIVACY.md`).

## 사전 준비

1. Firebase 콘솔 → 프로젝트 설정 → 서비스 계정 → "새 비공개 키 생성"으로 서비스 계정 JSON을 받는다.
2. Supabase CLI 로그인과 프로젝트 링크가 되어 있어야 한다.

```powershell
supabase login
supabase link --project-ref <project-ref>
```

## 배포

```powershell
# 서비스 계정 JSON 전체를 시크릿으로 저장한다 (파일 내용을 한 줄 문자열로)
supabase secrets set FCM_SERVICE_ACCOUNT="$(Get-Content -Raw .\firebase-service-account.json)"

# 웹훅은 Supabase 내부 호출이므로 JWT 검증을 끈다
supabase functions deploy notify-alarm-invitation --no-verify-jwt
```

`SUPABASE_URL`과 `SUPABASE_SERVICE_ROLE_KEY`는 Edge Function 런타임이 자동으로 주입한다. 앱에는 절대 넣지 않는다.

## Database Webhook 연결

대시보드 Database → Webhooks → Create a new hook:

- Table: `public.alarm_invitations`
- Events: `Insert`
- Type: Supabase Edge Function → `notify-alarm-invitation`
- Method: `POST`

SQL로 만들 때 (대시보드가 생성하는 형태와 동일):

```sql
create trigger notify_alarm_invitation_insert
after insert on public.alarm_invitations
for each row
execute function supabase_functions.http_request(
  'https://<project-ref>.supabase.co/functions/v1/notify-alarm-invitation',
  'POST',
  '{"Content-Type":"application/json"}',
  '{}',
  '5000'
);
```

## 로컬 테스트

```powershell
supabase functions serve notify-alarm-invitation --no-verify-jwt --env-file .\supabase\.env.local
```

```powershell
curl -X POST http://127.0.0.1:54321/functions/v1/notify-alarm-invitation `
  -H "Content-Type: application/json" `
  -d '{"type":"INSERT","table":"alarm_invitations","schema":"public","record":{"id":"<uuid>","requester_id":"<uuid>","target_id":"<uuid>","label":"회사","status":"pending"},"old_record":null}'
```

응답:

- `{"sent":true}` 전송 성공
- `{"skipped":"target has no push_token"}` 대상이 앱에서 토큰을 올리지 않음
- `{"error":"FCM_SERVICE_ACCOUNT missing"}` 시크릿 미설정
