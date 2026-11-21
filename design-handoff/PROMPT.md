# Claude Code 프롬프트 — 다왔어 디자인 적용 (라이트 · 다크)

아래를 그대로 붙여 넣는다. `design-handoff/` 는 이 폴더를 저장소 루트에 복사한 경로다.

---

Android 앱 「다왔어」(코드명 Wakepoint)에 디자인을 적용해 줘. 지금 코드는 디자인이 제거된 상태(Material 3 기본값)이고, 기능 로직은 이미 들어 있다. **바꾸는 것은 UI 계층뿐이다.**

## 읽을 것 (이 순서로)
1. `design-handoff/DESIGN.md` — 색 역할, 타입, 모양, 컴포넌트 수치, 화면 목록, 제약. 이 문서가 기준이다.
2. `DESIGN_SPEC.md` — 화면별 요소와 상태. 디자인과 명세가 다르면 멈추고 물어본다.
3. `design-handoff/tokens/colors.json`, `design-handoff/tokens/Theme.reference.kt` — 색 값과 테마 초안.
4. 화면을 작업할 때마다 `design-handoff/png/light/<이름>.png`, `png/dark/<이름>.png` 를 보고, 수치는 `design-handoff/screens/light/<이름>.html` 의 인라인 스타일에서 읽는다 (1px = 1dp, `data-layer` = 컴포넌트 이름).

## 작업 순서
1. `core/design/Theme.kt`: `WakepointColors`(라이트 · 다크), `LocalWakepointColors`, 타입(Pretendard Regular · Medium), 모양을 정의하고 `WakepointTheme` 에 주입한다. `isSystemInDarkTheme()` 을 따른다. Pretendard 폰트 파일이 `res/font` 에 없으면 추가가 필요하다고 알리고 시스템 폰트로 진행한다.
2. `core/design/Component.kt`, `RadiusSelector.kt`: DESIGN.md 5절대로 공통 컴포넌트에 스타일을 입힌다. **기존 함수 시그니처는 유지한다.** 파라미터를 추가해야 하면 기본값을 준다.
3. 화면: 알람 울림 → 홈 → 알람 목록 → 친구 → 로그인 · 회원가입 → 프로필 → 사운드 목록 순서. DESIGN.md 6절 표의 구현 위치를 따른다.
4. 하단 탭바와 바텀시트 컨테이너, 시스템 알림(small icon, 액센트 색).
5. `docs/DESIGN.md` 에 `design-handoff/DESIGN.md` 를 옮기고 `AGENTS.md` 의 디자인 규칙을 갱신한다.

## 지킬 것
- 화면 코드에 hex 를 직접 쓰지 않는다. `LocalWakepointColors` 의 역할만 쓴다. 8자리 값의 알파를 바꾸거나 불투명하게 만들지 않는다.
- 빨강(`negative*`)은 필드 오류와 실패 메시지에만 쓴다. 삭제 · 차단 · 로그아웃에 쓰지 않는다.
- 그림자(elevation)를 추가하지 않는다. 굵기는 400 과 500 만 쓴다.
- ViewModel, Repository, 내비게이션 경로, 권한 처리 등 로직은 건드리지 않는다. 동작이 없는 요소(비밀번호 찾기, 설정, 고객센터, 사운드 목록의 선택 · 가져오기 · 녹음)는 모양만 만들고 동작을 지어내지 않는다.
- 명세에 없는 기능(스누즈, 지도 반경 원, 미리듣기 등)을 추가하지 않는다.
- 카카오 로그인 버튼은 기존 공식 이미지 에셋을 그대로 둔다.
- 친구에게 상대의 위치 · 도착 여부 · 알람 울림 상태를 보여 주는 UI 를 만들지 않는다 (`docs/DESTINATION_ALARM_PRIVACY.md`).
- Edge-to-edge 인셋을 처리한다. 알람 울림 화면은 잠금화면 위에서 중지 버튼 하나가 가장 크게 보여야 한다.
- 하드코딩된 한글 문자열은 손대는 화면에 한해 `strings.xml` 로 옮긴다. 디자인의 자리 표시 문구로 기존 문자열을 덮어쓰지 않는다.
- DESIGN.md 8절의 “아직 정하지 않은 것”은 기본값으로 구현하고, 마지막 보고에 다시 적는다.

## 확인 방법
- 단계마다 `./gradlew assembleDebug` 가 성공해야 한다. 기존 테스트가 있으면 `./gradlew test` 도 통과해야 한다.
- 화면마다 `@Preview` 를 라이트와 다크(`uiMode = UI_MODE_NIGHT_YES`) 두 개씩, DESIGN.md 6절의 상태별로 둔다.
- 화면 하나를 끝낼 때마다 `docs/DESIGN_PROGRESS.md` 의 체크리스트(화면 × 라이트/다크)를 갱신하고 커밋한다. 디자인 PNG 와 다르게 구현한 곳은 이유와 함께 같은 파일에 적는다.
- 마지막에 `grep -rn "Color(0x" app/src/main --include=*.kt` 결과가 `core/design/Theme.kt` 에만 나오는지 확인해 출력한다.

## 멈추고 물어볼 때
- 디자인과 `DESIGN_SPEC.md` 가 충돌할 때.
- 공통 컴포넌트의 시그니처를 깨야만 구현할 수 있을 때.
- 같은 빌드 오류를 세 번 고쳐도 해결되지 않을 때.

---

## `/goal` 로 돌릴 때

auto mode 에서 아래를 입력한다. 진행 상황을 deadhd 로 보려면 먼저 `/deadhd` 를 연다.

```text
/goal docs/DESIGN_PROGRESS.md 의 모든 화면이 라이트 · 다크 둘 다 체크된다.
작업 지시는 design-handoff/PROMPT.md 를 따른다.
증명: 마지막에 ./gradlew assembleDebug 성공 출력, DESIGN_PROGRESS.md 의 체크 현황(완료 N / 전체 N),
grep -rn "Color(0x" app/src/main --include=*.kt 결과가 core/design/Theme.kt 에만 나오는 것을 보여 준다.
화면 하나를 시작하고 끝낼 때마다 deadhd 진행 데이터를 갱신한다(시작 now, 빌드 통과 뒤 done, 막히면 blocked 와 이유).
제약: ViewModel · Repository · 내비게이션 로직은 수정하지 않는다. 명세에 없는 기능을 추가하지 않는다.
같은 화면에서 빌드가 3번 실패하면 blocked 로 두고 다음 화면으로 넘어간다. 60턴이 지나면 멈추고 남은 화면을 보고한다.
```
