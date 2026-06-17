# 2026-06-17 Radius Selector

## 변경

- 알람 반경 UI를 여러 개의 버튼 묶음에서 selectbox로 변경했다.
- 반경 선택 컴포넌트를 `RadiusSelector.kt`로 분리해 `Component.kt` 전체 읽기 필요성을 줄였다.
- 반경 옵션을 `10m`, `50m`, `100m`, `300m`, `500m`, `1km`, `3km`, `10km`로 정리했다.
- `50km` UI 옵션은 제거했다.
- 앱 최소 반경 보정값을 `0.01km`로 낮췄다.
- Supabase `alarms.radius_km` check constraint를 `0.01 ~ 50km`로 보수하는 migration을 추가했다.

## 검증

- `assembleDebug`: 성공
- `test`: 성공

## 라이브 DB 적용

Supabase SQL Editor 또는 CLI로 아래 migration을 적용해야 10m/50m 저장이 가능하다.

- `supabase/migrations/20260617001000_allow_small_alarm_radius.sql`
