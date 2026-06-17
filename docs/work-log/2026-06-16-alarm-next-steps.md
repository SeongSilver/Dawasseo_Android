# 2026-06-16 Alarm Follow-up

## 완료

- 알람 만들기 개선 변경을 커밋했다.
  - 홈 저장 성공 후 알람 목록으로 이동
  - 알람 탭 `+`에서 홈 알람 만들기로 이동
  - 당시 반경 옵션을 `100m`부터 `50km`까지 확장했다.
- Android 13+ `POST_NOTIFICATIONS` 권한 UX를 추가하고 커밋했다.
  - 알람 만들기 버튼에서 알림 권한 요청
  - 권한 거부 시 바텀시트 안내 문구 표시
- Supabase core schema migration을 추가했다.
  - `user_profiles`
  - `alarms`
  - `friends`
  - `alarm_permissions`
  - 기본 RLS 정책

## 검증

- `assembleDebug`: 성공
- `test`: 성공
- `adb devices`: 미실행, 현재 shell에서 `adb` 명령을 찾지 못함
- Supabase live DB push: 미실행, Supabase CLI/로그인/네트워크 확인 필요

## 남은 수동 검증

- Android 13+ emulator 또는 실기기에서 알림 권한 허용/거부 UX 확인
- 로그인 후 알람 생성 시 Supabase `alarms` row 생성 확인
- 알람 목록 Room cache 반영 확인
- mock location 또는 실기기 이동으로 반경 진입 트리거 확인
- 트리거 후 `is_active=false`, `triggered_at` 갱신 확인
