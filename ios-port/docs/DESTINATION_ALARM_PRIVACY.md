# 목적지 알람 개인정보 원칙

이 앱의 위치 기반 알람은 상대방 위치 추적 기능이 아니다.

현재 기능은 사용자가 본인 기기에서 직접 목적지 알람을 설정하고, 목적지 근처에 도착했을 때 본인 기기에서 로컬 알람이 울리는 기능이다.

향후 친구 기능은 A가 B에게 목적지 알람을 제안하고, B가 명시적으로 승인하면 B의 기기에서만 위치 판단과 알람 실행이 일어나는 구조로 제한한다.

## 절대 원칙

- A는 B의 현재 위치를 알 수 없어야 한다.
- A는 B의 도착 여부를 알 수 없어야 한다.
- A는 B가 목적지 근처에 있는지 알 수 없어야 한다.
- A는 B에게 알람이 울렸는지 알 수 없어야 한다.
- A는 B가 알람을 확인하거나 종료했는지 알 수 없어야 한다.
- 서버는 B의 현재 위치, 마지막 위치, 이동 상태, 목적지까지 남은 거리, 도착 여부, 도착 시각, 알람 울림 여부, 알람 울림 시각, 알람 확인 여부, 알람 확인 시각을 저장하면 안 된다.
- 위치 판단은 B 기기 내부에서만 수행한다.
- 알람은 B 기기의 로컬 알림/알람으로만 발생한다.
- 로컬 알람은 1회 울린 뒤 로컬에서 삭제한다.
- Supabase, FCM, 분석 이벤트, 크래시 로그, 디버그 로그에 위치/근접/도착/알람 발생/확인 정보를 남기지 않는다.

## 서버 저장 허용 정보

서버에는 초대 전달과 상태 관리를 위한 최소 정보만 저장한다.

- 알람 초대 ID
- 요청자 사용자 ID
- 대상자 사용자 ID
- 목적지 이름
- 목적지 좌표
- 반경
- 만료시간
- 초대 상태값: `pending`, `accepted`, `rejected`, `expired`, `cancelled`
- 생성 시각
- 수락 시각
- 거절 시각

## 금지 필드와 이벤트

아래 성격의 필드, 함수, API, 이벤트, 분석 이름을 만들지 않는다.

- `current_lat`, `current_lng`, `last_location`
- `remaining_distance`
- `arrived`, `arrival_detected_at`, `arrived_at`
- `near_destination`
- `alarm_triggered`, `alarm_triggered_at`
- `alarm_confirmed`, `alarm_confirmed_at`
- `updateUserLocation`
- `sendCurrentLocationToServer`
- `saveLastKnownLocation`
- `notifyRequesterArrived`
- `saveArrivalDetectedAt`
- `saveAlarmTriggeredAt`
- `sendAlarmTriggeredEventToRequester`
- `trackTargetUserLocation`
- `ARRIVED`
- `NEAR_DESTINATION`
- `LOCATION_UPDATED`
- `ALARM_TRIGGERED`
- `ALARM_CONFIRMED`

## 요청자에게 보여줄 수 있는 상태

A에게는 초대 처리 상태만 보여준다.

- 대기 중
- 수락됨
- 거절됨
- 만료됨
- 취소됨

A에게 B의 이동 중, 근처, 도착, 미도착, 알람 울림, 알람 확인 상태를 보여주지 않는다.

## 로컬 트리거 처리

승인된 알람이 B 기기에서 트리거되면 아래 순서만 수행한다.

1. B 기기에서 로컬 알람 UI 또는 알림을 표시한다.
2. 해당 로컬 알람 행을 삭제한다.
3. Supabase를 업데이트하지 않는다.
4. 요청자에게 FCM을 보내지 않는다.
5. 위치, 근접, 도착, 트리거 상세 정보를 로그로 남기지 않는다.
