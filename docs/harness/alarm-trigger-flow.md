# Alarm Trigger Flow Harness

알람 저장, 활성 알람 기반 위치 추적, 반경 진입 트리거를 수정할 때 사용하는 하네스다.
이 플로우는 배터리와 권한 정책 영향을 크게 받으므로 작은 변경도 끝까지 검증한다.

## 먼저 읽을 파일

- `app/src/main/java/com/wakepoint/app/data/alarm/AlarmRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/DefaultAlarmRepository.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/local/AlarmDao.kt`
- `app/src/main/java/com/wakepoint/app/data/alarm/remote/AlarmDto.kt`
- `app/src/main/java/com/wakepoint/app/core/location/DistanceCalculator.kt`
- `app/src/main/java/com/wakepoint/app/core/location/LocationTrackingController.kt`
- `app/src/main/java/com/wakepoint/app/core/notification/AlarmNotificationManager.kt`
- `app/src/main/java/com/wakepoint/app/service/LocationTrackingService.kt`
- `app/src/main/AndroidManifest.xml`
- `docs/SCHEMA.md`
- `docs/CHECKLISTS.md`

## 기대 흐름

1. 홈에서 알람을 생성한다.
2. `AlarmRepository.saveAlarm()`이 Supabase `alarms` insert를 수행한다.
3. 성공한 remote alarm을 Room cache에 upsert한다.
4. 활성 알람이 1개 이상이면 `syncLocationTracking()`이 tracking service를 시작한다.
5. `LocationTrackingService`가 Balanced Priority 위치 업데이트를 받는다.
6. 현재 위치와 각 활성 알람 목적지의 거리를 `calculateDistance()`로 계산한다.
7. 거리가 radius 이하면 arrival notification을 표시한다.
8. 해당 알람을 `is_active=false`, `triggered_at=now`로 갱신한다.
9. 남은 활성 알람이 0개면 tracking service를 중단한다.

## 변경 규칙

- Supabase와 Room 동기화는 Repository에서만 처리한다.
- 거리 계산 공식을 새로 만들지 말고 `DistanceCalculator.calculateDistance()`를 사용한다.
- 위치 업데이트는 Balanced Priority를 기본으로 유지한다.
- 활성 알람 0개 중단 조건을 깨지 않는다.
- Foreground Service 알림은 장시간 위치 추적의 필수 요소로 유지한다.
- Android 13+ arrival notification은 `POST_NOTIFICATIONS` 권한 상태를 고려한다.
- background location은 이 하네스의 현재 범위가 아니다. 별도 단계에서 추가한다.

## 자동 검증

```powershell
.\gradlew assembleDebug
.\gradlew test
```

Gradle dependency 다운로드나 wrapper 실행이 sandbox 네트워크 제한으로 실패하면 같은 명령을 escalated 권한으로 다시 실행한다.

## 수동 검증

1. emulator 또는 실기기에서 로그인한다.
2. 현재 위치 근처 좌표로 알람을 생성한다.
3. 알람 목록에서 새 알람이 활성 상태로 보이는지 확인한다.
4. Foreground Service tracking notification이 표시되는지 확인한다.
5. emulator mock location 또는 실기기 이동으로 반경 안에 진입한다.
6. arrival notification이 표시되는지 확인한다.
7. 알람 목록에서 해당 알람이 지난 알람으로 이동했는지 확인한다.
8. Supabase `alarms` row의 `is_active=false`, `triggered_at` 갱신을 확인한다.
9. 활성 알람이 더 없으면 tracking notification이 사라지는지 확인한다.

## 문제 해결 순서

- 알람 저장 실패: Supabase URL/key, `alarms` table, RLS, owner_id/created_by 값을 확인한다.
- 목록 미반영: Room upsert와 `observeAlarms()` flow를 확인한다.
- 서비스 미시작: active alarm count와 `LocationTrackingController` start intent를 확인한다.
- 위치 미수신: foreground permission, Google Play services, emulator location provider를 확인한다.
- 알림 미표시: notification channel, foreground notification, Android 13+ permission을 확인한다.
- 트리거 안 됨: radius 단위, lat/lng 순서, `calculateDistance()` 입력값을 확인한다.

## 다음 확장 지점

- Android 13+ `POST_NOTIFICATIONS` 권한 UX
- Android 10+ background location 단계적 요청
- Foreground Service 재시작 안정성
- 알람 트리거 후 ringing screen과 반복 사운드 재생
- 위치 업데이트 주기와 배터리 사용량 실기기 측정

