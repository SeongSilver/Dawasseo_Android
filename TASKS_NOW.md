# TASKS_NOW (2026-08-26 기준)

근거: v2 재구축 Phase 2~6 완료(`docs/merge-report/04-v2-재구축-계획.md`,
`docs/work-log/2026-08-26-phase*.md`), 유닛테스트 129 그린, 계측 E2E(e2e 패키지) 24/24 그린.

## 완료됨 (이번 사이클)

- v2 STEP1 시간 알람: 백엔드(Room/AlarmManager/부팅 복원) + UI(1-1~1-6) + 알람 탭 통합.
- v2 STEP2 내 장소: 도메인/Room v10/화면 2-1~2-4/마이 진입점 (로컬 전용).
- v2 STEP3 친구 알람: 보낸 알람 목록(3-6)·수신 다이얼로그(3-7) 실기능, 3탭 스펙 UI(3-3~3-5).
- v2 STEP4 막차 알람: UI 4-1~4-6 + 모의 계산기 + 시간 알람 실연동 (마이 진입점).
- 백로그: 친구 검색 placeholder 리소스화, 뒤로가기 접근성 라벨, 미사용 signup 문자열 정리,
  NetworkRetry 백오프 상한 캡, AlarmDto 로컬 전용 필드 정책 문서화, CHECKLISTS macOS 병기.

## 즉시 착수 가능

1. 시간 알람 커스텀 벨소리 연동 — 생성/편집 시트의 "기본 벨소리" 고정을 사운드 목록 화면과 연결 | S
2. testTag 부여 — 핵심 UI(로그인/탭바/알람 카드) E2E 셀렉터 안정화 (기존 항목 유지) | S
3. `ScreenshotTourTest.tour2_mainScreens` 수리 또는 `@Ignore("capture tool")` — 헤드리스
   에뮬레이터에서 compose hierarchy 준비 전 질의로 상시 실패(변경 전부터, Phase 2 로그 참조) | S
4. 받은 알람 자동 활성 설정(스펙 3g 옵션) + 막차 재미 모드 온오프 설정 — 마이 탭 알림 설정 | M

## Blocked (선행조건 있음)

5. 시간 알람 발화·재부팅 복원 런타임 검증 | 선행: 실기기 | M
6. Kakao Mobility 실계산기 — `LastTrainCalculator` 구현 교체 + 임계값 제품 확정 | 선행: API 키/백엔드 | L
7. 친구 공개 장소 원격 동기화(장소 탭 3-3 실기능) | 선행: Supabase 신규 테이블 권한 | M
8. 시간 알람 친구 전송(시간 탭 3-4 실기능) + 막차 트리거 전송(4-6 배선) | 선행: 서버 스키마/FCM 페이로드 | M
9. Supabase live DB migration push | 선행: Supabase CLI 로그인 | M
10. Kakao OAuth 콜백 플로우 검증 | 선행: 카카오 계정/외부 브라우저 | M
11. 로그인 이후 풀 플로우 E2E — Hilt 테스트 더블(FakeAuthRepository) 필요 | 선행: hilt-android-testing | L
12. AlarmsViewModel·Repository 단위테스트 확충 | 선행: coroutines-test/mockk 의존성 | M

## 참고

- v2 신규 화면 문자열은 v2 관례상 한국어 리터럴 — 다국어 필요 시 strings.xml 일괄 추출.
- 확인(acknowledged) 수신 알람 id 는 로컬 prefs(`wakepoint_received_alarms`)에 무한 누적 —
  용량 문제는 없지만 서버 ack 스키마 도입 시 정리.
