# v2 통합 명세 구현 — 최종 보고서

무인 자율 실행 결과. 사용자 검토용. (브랜치: `feature/v2-integrated-spec`)

## 1. 요약
- 명세(`design/v2-spec.dc.html`, STEP1~4)의 **UI 화면 17개 항목(P1~P17)을 전부 구현**하고
  각 항목마다 `./gradlew :app:assembleDebug` 통과 후 커밋했다.
- 최종 클린 빌드: **BUILD SUCCESSFUL, exit code 0**.
- 하드코딩 검사: v2 UI 코드에 **스페이싱/타이포(.sp) 리터럴 0건**. `.dp` 34건은 전부
  허용 예외(테두리 두께·아이콘 크기·다이얼로그 구조 원). `.sp`는 토큰 파일(TypeV2.kt)에만 존재.
- **기존 추적 파일 0개 수정** — 전부 신규 파일. v1.5 세션과 머지 충돌 위험 사실상 없음.

## 2. 완료 항목 (17/17 + 토큰)
| STEP | 항목 | 파일 |
|---|---|---|
| 토큰 | P1 색상 확장 | `core/design/ColorV2.kt` |
| 토큰 | (P8) 구조 치수 | `core/design/SizeV2.kt` |
| 토큰 | (P14) hero 타이포 | `core/design/TypeV2.kt` |
| 1 | P2 알람 타입 선택 시트(1a) | `feature/v2/timealarm/AlarmTypeSheet.kt` |
| 1 | P3 시간알람 생성+요일/오류(1b,1c) | `.../TimeAlarmCreateSheet.kt`, `TimeAlarmLogic.kt` |
| 1 | P4 알람 목록 타입 배지(1d) | `.../AlarmTypeBadgeCard.kt`, `model/V2Models.kt` |
| 1 | P5 시간알람 편집 카드(1e) | `.../TimeAlarmEditCard.kt` |
| 1 | P6 정확알람 권한+정확도 배지(1f) | `.../ExactAlarmPermission.kt` |
| 2 | P7 내 장소 목록+진입점(2a) | `feature/v2/place/MyPlacesScreen.kt` |
| 2 | P8 장소 등록/편집 공개전개(2b,2c) | `.../PlaceEditScreen.kt` |
| 2 | P9 장소 삭제 다이얼로그(2d) | `.../PlaceDeleteDialog.kt` |
| 3 | P10 친구 목록+권한 3상태(3a) | `feature/v2/friendalarm/FriendListScreen.kt` |
| 3 | P11 알람 보내기 3탭+가드(3b~3e) | `.../SendAlarmSheet.kt` |
| 3 | P12 보낸 알람 목록(3f) | `.../SentAlarmsScreen.kt` |
| 3 | P13 알람 수신 다이얼로그(3g) | `.../ReceiveAlarmDialog.kt` |
| 4 | P14 막차 메인 3단계(4a) | `feature/v2/lasttrain/LastTrainScreen.kt` |
| 4 | P15 막차 끊김/미지원/집미등록(4b~4d) | `.../LastTrainStates.kt` |
| 4 | P16 위치 전송 동의(4e) | `.../LocationConsentSheet.kt` |
| 4 | P17 친구 막차 보내기/수신+톤(4f) | `.../FriendLastTrain.kt` |

각 화면은 `@Preview` 를 포함해 컴파일·시각 확인 가능. 상태는 UI-로컬(remember), 순수 로직은
`TimeAlarmLogic.kt` 로 분리(테스트 대비).

## 3. BLOCKED 항목
- **없음.** 모든 항목 빌드 통과. (빌드 실패는 P10 `padding` import 누락, P12 확장함수 참조
  오류 2건 발생했으나 즉시 수정, 3턴 연속 실패 룰 미해당.)

## 4. 미구현/최소구현 범위 (명세 추측 금지 원칙에 따른 처리)
UI 시안 기반 명세라 백엔드/외부연동은 명세에 없어 **최소 스텁**으로 처리(자세히는 아래).
- 날짜/시각/요일 **피커 컴포넌트** 미명세 → 필드는 표시/선택 상태만, 실제 피커 미연결.
- 과거시각 판정(`timeIsPast`)은 파라미터 주입(실판정은 뷰모델/피커 몫).
- **카카오맵 연동**(경로/택시비/막차시각 계산), FCM 친구알람 송수신, Supabase 동기화,
  Room 영속성 → 미연결. UI 모델(`feature/v2/model`)만 정의.
- 기존 화면(AlarmsScreen/ProfileScreen/FriendsScreen/Nav)으로의 **최종 통합·네비 배선**은
  v1.5 세션 충돌 방지를 위해 신규 자립 화면으로 대체하고 통합은 후속으로 남김(§6).

## 5. 주요 결정 요약 (상세: docs/v2-decisions.md)
- **D-001** 누락된 로컬 명세를 공유링크 프로젝트에서 확보해 `design/v2-spec.dc.html` 생성.
- **D-003/D-004** 공유 작업트리에서 v1.5 세션이 브랜치를 전환하는 레이스 발생 → v2로 복귀 후
  **모든 커밋 직전 `git branch==v2` 가드** 적용(17커밋 전부 통과).
- **D-005** v2 tip이 v1.5 커밋으로 오염된 상태를 비파괴 수용(위에 쌓기). CLAUDE.md 미수정.
- **D-002/아키텍처** v2는 신규 `feature/v2/**` + 신규 토큰 파일에만 작성 → 충돌 0.
- **D-006** 사용자 지시: 완료 후 `/compact`→`/clear`→TDD (Stage5, §7).
- **D-007/D-008** 지도 높이·막차 hero 타이포는 v1.5 파일 미편집 위해 `SizeV2.kt`/`TypeV2.kt`
  신규 토큰 파일에 명명 정의(화면 코드 리터럴 금지 준수).

## 6. v1.5 브랜치 머지 시 충돌 예상 파일
- **소스 충돌 예상: 없음.** v2가 추가한 파일은 전부 신규이며 v1.5가 쓰는 이름과 겹치지 않음:
  - `core/design/ColorV2.kt`, `SizeV2.kt`, `TypeV2.kt` (신규, v1.5의 Color/Spacing/Type.kt 미편집)
  - `feature/v2/**` 전체 (신규 패키지)
  - `docs/v2-*.md` (신규)
- **주의(충돌 아님, 정합성 확인용)**:
  - 현재 v2 tip(`92ada49`)은 이미 v1.5 리디자인 커밋 + v1.5의 `CLAUDE.md` 변경을 포함(D-005).
    두 브랜치 재머지 시 대부분 이미-병합 상태. 사용자가 히스토리 정리를 원하면
    `git reset --hard 8824325` 후 v2 커밋 cherry-pick(D-005 참조).
  - 후속으로 v2 화면을 실제 화면/네비에 통합할 때 `AlarmsScreen.kt`/`ProfileScreen.kt`/
    `FriendsScreen.kt`/`WakepointNavGraph.kt` 를 **편집하게 되며 그 시점엔 v1.5와 충돌 가능** →
    v1.5 머지 완료 후 진행 권장.

## 7. 다음 세션 TDD 인수인계 (사용자 지시 = /compact → /clear → TDD)
clear 후 새 세션은 이 문서들만 읽어 이어받을 수 있다: `docs/v2-plan.md`, `docs/v2-decisions.md`,
`docs/v2-design-guide.md`, 본 보고서.

**우선 테스트 대상(순수 로직, JVM 단위테스트 — 의존성 없음):**
1. `feature/v2/timealarm/TimeAlarmLogic.kt`
   - `isDateLocked(mode)`: NONE→false, 그 외→true.
   - `isAddEnabled(timeIsPast, mode, customDays)`:
     - 과거시각이면 항상 false.
     - CUSTOM인데 customDays 비면 false, 하나 이상이면 true.
     - 비-CUSTOM이고 과거 아니면 true.
**테스트 위치/실행:** `app/src/test/java/com/wakepoint/app/feature/v2/...` 에 JUnit 작성 →
`./gradlew :app:testDebugUnitTest`. (이미 `app/src/test/` 존재)
**확장 후보:** 막차 3단계 판정 로직을 순수함수로 추출(LastTrainScreen의 level 분기)해 테스트,
친구 권한 상태 전이, 보낸 알람 상태 2단계.
**TDD 절차 권장:** ① 위 명세대로 실패 테스트 작성 → ② 로직 함수 보완 → ③ green → 리팩터.

## 8. 검증 로그
```
$ ./gradlew clean :app:assembleDebug   → BUILD SUCCESSFUL (exit 0)
v2 UI .sp 리터럴: 0건
v2 UI .dp 리터럴: 34건 (전부 예외: 테두리 1.5/2dp · 아이콘 14~28dp · 다이얼로그 원 64dp)
기존 추적파일 수정: 0개 (신규 파일만)
커밋: P1~P17 (17커밋), 전부 feature/v2-integrated-spec, 커밋 전 브랜치 가드 통과
```
