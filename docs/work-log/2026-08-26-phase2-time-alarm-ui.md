# 2026-08-26 — Phase 2 잔여: 시간 알람 UI(1-1~1-6) + 알람 탭 배선

> 브랜치: `feature/phase2-time-alarm-ui` → main 머지.
> 근거 계획: `docs/merge-report/04-v2-재구축-계획.md` Phase 2. 백엔드(2-1~2-3)는 기존 완료,
> 이번 세션은 UI + 네비 배선 + 테스트.

## 작업 로그

| 커밋 | 내용 |
|---|---|
| `d56538c` | 시간 알람 한국어 표시 포맷터(`feature/timealarm/TimeAlarmFormat`) + 유닛테스트 13개. 오전/오후, 반복 프리셋/요일직접(프리셋 동치 축약), 일회성 오늘/내일 — `TimeAlarmSchedule` 과 동일 계산 사용. |
| `5204a3e` | `AlarmsViewModel` 확장: `observeTimeAlarms` 결합(활성/비활성 파생, `isEmpty` 합산), 저장/토글/삭제 액션, `exactAlarmAllowed` 상태 + onResume 갱신용 `refreshExactAlarmState()`. |
| `369986b` | (1-1) `AlarmTypeSheetContent` 위치/시간 2택 시트 — 알람 탭 +버튼·빈 상태 버튼에서 열림, 위치 선택 시 기존 지도 플로우 직행. (1-2) `TimeAlarmCreateSheetContent` — M3 TimePicker(기본 다음 정시), 반복 5모드+요일 원형 칩, 반복 시 날짜 잠금, 요일 없는 CUSTOM 추가 비활성, 소리/진동 토글. 추가 → Room 저장+AlarmManager 예약. |
| `76a7201` | (1-3) `AlarmTypeLabel` 타입 배지 — 위치/시간 카드 메타줄 공통. (1-4) `TimeAlarmCard` 접힘/펼침 편집(이름/시각 피커/반복/소리·진동/[삭제][저장]). 알람 탭 활성/비활성 섹션에 시간 알람 통합, 요약 카운트 합산, 삭제 확인 다이얼로그. |
| `47eeaee` | (1-5) 정확 알람 권한 안내 다이얼로그 — 시간 알람 생성 직후 미허용이면 설치당 1회(v1 `wakepoint_prompts` prefs 패턴). (1-6) [정확도 제한] 배지 탭 → 시스템 "알람 및 리마인더" 설정 이동, onResume 재확인. 순수 로직 테스트 3개. |
| `(E2E)` | 계측 E2E 5케이스(`e2e/v2/TimeAlarmE2ETest`): 타입 시트 노출/콜백, 생성 시트 요일직접 잠금·비활성, 기본값 생성, 카드 배지·메타, 편집 저장 콜백. |

## 설계 결정
- **날짜 필드는 표시 전용**: 도메인(`TimeAlarm`)에 날짜가 없고 일회성은 "다음 hh:mm" 자동 계산이라
  과거 시각 오류 상태(명세 1c)가 실제로 발생하지 않음 → 오늘/내일 자동 표기 + 반복 시 "반복 사용 중" 잠금.
- **벨소리는 기본 벨소리 고정**: 커스텀 사운드 연동(사운드 목록 화면 재사용)은 후속 작업.
- **선택 모드(다중 삭제)는 위치 알람 전용 유지**: 시간 알람은 편집 카드의 [삭제]로 삭제.
- 문자열은 v2 데모 관례대로 한국어 리터럴(기존 `RepeatMode.label` 패턴) — strings.xml 추출은 후속.

## 테스트 결과
- 유닛테스트: **106개 전부 그린** (기존 90 + 신규 16).
- 계측 E2E: 에뮬레이터(Medium_Phone, API 36) 실행 — **14개 중 13개 통과**.
  - 신규 시간 알람 E2E 5/5 통과, 기존 E2E 8/8 통과.

## 에러 로그
1. **`ScreenshotTourTest.tour2_mainScreens` 실패 (기존 실패, 이번 변경과 무관)**
   - 증상: `IllegalStateException: No compose hierarchies found in the app` — 첫 `waitForText` 시점.
   - 검증: **main 브랜치(변경 전)에서도 동일 실패 재현** → Phase 2 변경과 무관 확인.
   - 원인 추정: 이 테스트는 "캡처 도구"(가짜 세션 → 서버 401 → 지도 타일 로딩 의존)라
     헤드리스 스냅샷 에뮬레이터 환경에서 MainActivity 초기화가 waitUntil 첫 질의보다 늦음.
   - 처리: 미수정(캡처 도구 성격 + 기존 실패). 후속: `waitForText` 전에 hierarchy 존재 대기
     또는 `@Ignore("capture tool")` 검토.
2. **빌드 오류 1건(즉시 수정)**: `ModalBottomSheet` 사용에 `@OptIn(ExperimentalMaterial3Api)` 누락
   → `AlarmsScreen` 에 OptIn 추가로 해결.

## 남은 것 (후속)
- 시간 알람 커스텀 벨소리 연동(사운드 목록 재사용).
- 시간 알람 발화·재부팅 복원 **실기기 런타임 검증**(에뮬레이터 UI 테스트로는 AlarmManager 발화 미검증).
- 로그인 이후 알람 탭 풀 플로우 E2E(Hilt 테스트 더블 필요 — TASKS_NOW #15).
