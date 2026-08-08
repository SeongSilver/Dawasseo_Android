# v2 통합 명세 구현 계획 (docs/v2-spec 기반)

> 소스: `design/v2-spec.dc.html` (STEP1 시간알람 · STEP2 내장소 · STEP3 친구알람 · STEP4 막차알람)
> 원칙: **UI 우선 / 신규 파일 위주 / 기존 디자인 토큰 재사용 / 빌드 그린 유지 / 항목 단위 커밋**

## 아키텍처 결정 (요약, 상세는 v2-decisions.md)
- **격리**: v1.5 세션이 활성 편집 중인 파일(AlarmsScreen/ProfileScreen/FriendsScreen/HomeScreen/
  navigation/Color.kt 등)을 **건드리지 않는다**. v2는 신규 패키지
  `com.wakepoint.app.feature.v2/**` 아래 자립형 Composable + `@Preview` 로 구현 → 머지 충돌 0.
- **토큰**: 기존 `core/design/*` 재사용. 신규 3색만 별도 파일 `core/design/ColorV2.kt` 에 추가.
- **상태/데이터**: UI-로컬 상태(remember) + 신규 UI 모델(`feature/v2/model`). 기존 ViewModel/Room/
  Supabase/도메인 **미수정**. 외부연동(카카오맵/막차계산/FCM)은 스펙상 미정의 → 최소 스텁.
- **빌드 단위**: `./gradlew :app:assembleDebug`. assembleDebug 는 nav 미연결 신규 파일도 컴파일.

---

## 신규 화면 (feature/v2)

### STEP 1 — 시간 알람
- [x] **P1** v2 토큰 확장 — `core/design/ColorV2.kt` (칩 활성테두리 #1668B8 / 임박링 #F0B429 / 비활성버튼BG #F4F4F5) `[토큰]` ✅ 빌드통과
- [x] **P2** 알람 타입 선택 바텀시트 (1a) — `feature/v2/timealarm/AlarmTypeSheet.kt` `[신규]` ✅ 빌드통과
- [x] **P3** 시간 알람 생성 시트 + 요일직접/과거시각 오류 상태 (1b,1c) — `feature/v2/timealarm/TimeAlarmCreateSheet.kt` + `TimeAlarmLogic.kt` `[신규]` ✅ 빌드통과
- [x] **P4** 알람 목록 타입 배지 카드 (1d) — `feature/v2/timealarm/AlarmTypeBadgeCard.kt` + `model/V2Models.kt` `[변경→신규데모]` ✅ 빌드통과
- [x] **P5** 시간 알람 편집 확장 카드 (1e) — `feature/v2/timealarm/TimeAlarmEditCard.kt` `[변경→신규데모]` ✅ 빌드통과
- [ ] **P6** 정확 알람 권한 다이얼로그 + 정확도 제한 배지 (1f) — `feature/v2/timealarm/ExactAlarmPermission.kt` `[신규]`

### STEP 2 — 내 장소
- [ ] **P7** 내 장소 목록 + 마이 진입점 행 (2a) — `feature/v2/place/MyPlacesScreen.kt` `[신규]`
- [ ] **P8** 장소 등록/편집 (공개 토글 ON/OFF 전개 + 친구 미리보기) (2b,2c) — `feature/v2/place/PlaceEditScreen.kt` `[신규]`
- [ ] **P9** 장소 삭제 확인 다이얼로그 (2d) — `feature/v2/place/PlaceDeleteDialog.kt` `[신규]`

### STEP 3 — 친구 알람
- [ ] **P10** 친구 목록 + 권한 3상태 확장 카드 (3a) — `feature/v2/friendalarm/FriendListScreen.kt` `[신규]`
- [ ] **P11** 알람 보내기 시트 3탭(주소/장소/시간)+빈상태+버전가드 (3b~3e) — `feature/v2/friendalarm/SendAlarmSheet.kt` `[신규]`
- [ ] **P12** 보낸 알람 목록 (3f) — `feature/v2/friendalarm/SentAlarmsScreen.kt` `[신규]`
- [ ] **P13** 알람 수신 다이얼로그 (3g) — `feature/v2/friendalarm/ReceiveAlarmDialog.kt` `[신규]`

### STEP 4 — 막차 알람
- [ ] **P14** 막차 메인 3단계(여유/임박/아슬) (4a) — `feature/v2/lasttrain/LastTrainScreen.kt` `[신규]`
- [ ] **P15** 막차 끊김/미지원/집 미등록 상태 (4b,4c,4d) — `feature/v2/lasttrain/LastTrainStates.kt` `[신규]`
- [ ] **P16** 위치 전송 동의 시트 (4e) — `feature/v2/lasttrain/LocationConsentSheet.kt` `[신규]`
- [ ] **P17** 친구 막차 알람 보내기/수신 + 톤 정책 (4f) — `feature/v2/lasttrain/FriendLastTrain.kt` `[신규]`

## 데이터 변경 (UI 모델만, 영속성 제외)
- [ ] 각 STEP 구현 시 `feature/v2/model/*.kt` 에 UI 데이터 클래스 정의(Place, V2Alarm, FriendPermission,
      SentAlarm, LastTrainState 등). 기존 domain/data 레이어는 미수정.

## 로직 변경
- [ ] 없음(신규만). 진입 분기·유효성·상태머신은 각 신규 화면의 UI-로컬 상태로 표현. 실제 연동은 후속.

## Stage 5 — 테스트 (사용자 지시: 완료 후 /compact → /clear → TDD)
- [ ] **P18** 순수 로직/상태 단위 테스트 — 반복→요일비트, 과거시각 유효성, 막차 3단계 판정,
      권한 상태머신 등 순수함수를 `feature/v2/**` 에 분리하고 `test/` 에 JVM 단위테스트. (후속 세션)

---
## 진행 로그
- 0단계 통과, 베이스라인 `./gradlew :app:assembleDebug` = BUILD SUCCESSFUL(exit 0).
