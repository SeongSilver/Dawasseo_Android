# CLAUDE.md — 다왔어(Dawasseo) Android

Jetpack Compose 앱. 디자인 시스템은 `app/src/main/java/com/wakepoint/app/core/design/` 에 있다.
UI를 수정할 때는 아래 **디자인 가이드**를 반드시 지킬 것.

---

## Git 워크플로우 (PR 플로우 — 2026-08-26 전환)

`gh` CLI 인증 완료(계정 SeongSilver). **main 직접 커밋·직접 머지 금지** — 모든 작업은 PR로:

1. `main`에서 작업 브랜치 분기 (`feature/…`, `fix/…`, `chore/…`, `docs/…`).
2. 작업 단위별 커밋 → `git push -u origin <branch>`.
3. `gh pr create --base main` — 본문에 작업 내역·테스트 결과 요약.
4. 검증(빌드 + 유닛테스트, UI 변경 시 계측 E2E) 그린이면 `gh pr merge --merge --delete-branch`
   (머지 커밋 방식 — 기존 `--no-ff` 이력과 일관). 1인 저장소라 자체 승인 없이 바로 머지한다.
5. phase/작업 종료 시 `docs/work-log/`에 작업·에러 로그 md 기록(기존 관례 유지).

---

## 디자인 가이드 (강제 규칙)

> 기준 문서: `docs/DESIGN.md`. 원본 산출물: `design-handoff/`
> (`screens/{light,dark}/<이름>.html` 은 1px = 1dp 이고 `data-layer` 가 컴포넌트 이름,
> `png/{light,dark}/<이름>.png`, `tokens/colors.json`).
> 구현 현황·디자인과 다른 점: `docs/DESIGN_PROGRESS.md`.
> 단일 소스(SSOT): `core/design/` 의 `Theme.kt`(색) · `Type.kt`(타입) · `Shape.kt`(모서리) ·
> `Spacing.kt`(간격·치수) · `Component.kt`·`RadiusSelector.kt`(공통 컴포넌트) · `Preview.kt`(미리보기).
>
> (2026-11-21 에 기존 v1.5 단일 라이트 테마 가이드를 대체했다.)

### 0. 절대 규칙
- **하드코딩 금지**: 화면(feature)·내비 코드에 hex(`Color(0x…)`)·`16.dp`·`18.sp` 같은 리터럴을
  직접 쓰지 않는다. 반드시 `LocalWakepointColors.current.<역할>`, `WakepointSpacing`,
  `WakepointSize`, `WakepointShape`/`WakepointRadius`, `MaterialTheme.typography.<role>` 을 쓴다.
  `Color(0x…)` 리터럴이 허용되는 파일은 `core/design/Theme.kt` 하나뿐이다.
- **색을 만들거나 섞지 않는다.** 8자리 색의 알파를 바꾸거나 불투명하게 만들지 않는다.
- **라이트·다크 두 테마.** `WakepointTheme()` 이 `isSystemInDarkTheme()` 을 따른다.
  화면을 손대면 `@WakepointScreenPreviews`(라이트·다크 한 쌍)로 상태별 미리보기를 남긴다.
- 디자인에 없는 값이 필요하면 **임의로 만들지 말고** 가장 가까운 기존 토큰을 쓰거나 사람에게 물어볼 것.
- **비즈니스 로직/ViewModel/데이터 레이어/내비게이션/권한 처리는 UI 리디자인 중 수정하지 않는다.**

### 1. 색 — 역할(role) 토큰 (`Theme.kt`, `LocalWakepointColors`)
`background` · `backgroundSecondary` · `surfaceElevated` · `surfaceElevatedSecondary` ·
`fill` · `fillWeak` · `fillStrong` · `card` · `disabledContainer` · `disabledContent` ·
`text` · `textStrong` · `textSecondary` · `textTertiary` · `inactive` · `line` · `dim` ·
`brand` · `brandPressed` · `onBrand` · `brandSubtle` · `brandLineStrong` · `brandFocus` ·
`negative` · `negativeText` · `negativeSurface` · `negativeLine` · `negativeLineStrong` ·
`positive` · `positiveSurface` · `cautionary` · `cautionarySurface`.
역할별 쓰임은 `docs/DESIGN.md` 2절 표를 그대로 따른다.

- **빨강(`negative*`)은 입력 오류와 실패 메시지에만.** 삭제·차단·로그아웃 버튼, 배지, 장식에 쓰지 않는다.
- **파랑(`brand`)은 핵심 액션과 선택 상태에만**: 채움 버튼, 켜진 스위치, 선택 탭, 선택 체크,
  글자 버튼, 포커스 테두리, 목적지 마커.
- M3 컴포넌트의 기본 색에 기대지 말고 각 컴포넌트의 `colors` 파라미터에 역할을 넘긴다.

### 2. 타입 (`Type.kt`, Material3 role 에 매핑)
displayLarge 57/64·400 · displayMedium 45/52·400 · headlineSmall 24/32·400 ·
titleLarge 22/28·400 · titleMedium 16/24·500 · titleSmall 14/20·500 ·
bodyLarge 16/24·400 · bodyMedium 14/20·400 · bodySmall 12/16·400 ·
labelLarge 14/20·500 · labelMedium 12/16·500 · labelSmall 11/16·500.

- **굵기는 400 과 500 두 가지만.** Bold 를 쓰지 않고 인라인 `fontSize`/`fontWeight` 도 쓰지 않는다.
- 폰트: 스펙은 Pretendard 지만 `res/font` 에 리소스가 없어 시스템 폰트(`FontFamily.Default`)로
  구현되어 있다. 폰트 파일을 넣으면 `Type.kt` 의 `WakepointFontFamily` 한 곳만 바꾸면 된다.

### 3. 간격 — 4dp 그리드 (`Spacing.kt`)
`xs`=4 · `sm`=8 · `md`=12 · `lg`=16 · `xl`=24 · `xxl`=32 · `xxxl`=48 만 쓴다.
의미 토큰: 목록 좌우 여백 `screenHorizontal`(16) · 폼·시트 좌우 여백 `formHorizontal`(24) ·
카드 안 여백 `cardPadding`(16) · 카드↔카드 `cardGap`(12) · 필드↔필드 `fieldGap`(16) ·
섹션↔섹션 `sectionGap`(24) · 섹션 타이틀↔콘텐츠 `sectionTitleGap`(8) · 아이콘↔텍스트 `iconTextGap`(8).
치수(`WakepointSize`): 버튼 56(카드·행 안 40, 알람 중지 96×312) · 필드 56 · pill 32 ·
묶음·설정 행 56 · 사운드 행 64 · 상단 바 64 · 최소 터치 48 · 아이콘 24(작은 20, pill 18).

### 4. 모서리 (`Shape.kt`)
`field`(4, 텍스트 필드) · `pill`(8, 상태 배지) · `message`(12, 메시지 영역·버튼 pressed) ·
`card`(16, 카드·묶음 행) · `dialog`/`sheet`(28, 다이얼로그·시트·빈 상태) ·
`full`(버튼·검색바·스위치·아바타). 버튼은 누르는 동안 한 단계 작아진다(원형 → 12).

### 5. 컴포넌트 (`Component.kt` — API 는 유지, 파라미터 추가 시 기본값을 준다)
`WakepointButton`(채움) · `WakepointSecondaryButton`(테두리) · `WakepointTintButton`(tonal) ·
`WakepointTextButton`(글자) · `WakepointDangerButton`(= 테두리, 빨강 아님) ·
`WakepointTextField`(`filled` 로 채움/테두리 선택, 라벨이 떠오름) · `WakepointCard`(`filled`) ·
`WakepointMessage`(Error/Info/InfoOnSheet/Caution) · `StatusPill` · `WakepointSwitch` ·
`WakepointAvatar` · `WakepointLogo` · `WakepointHeader` · `WakepointPageHeader` ·
`WakepointCircleIconButton` · `WakepointDivider` · `WakepointEmptyState` ·
`BottomSheetHandle` · `BottomSheetTitle` · `SoundOptionRow` · `SettingsRow` · `RadiusSelector`.

- **그림자(elevation)를 추가하지 않는다.** 면은 채움과 테두리로 구분한다.
- 로딩은 스피너 없이 버튼 문구 변경("처리 중"·"저장 중"·"검색 중") + 비활성으로 표현한다.
- 시트 하단 버튼은 스크롤 밖 고정, 취소 : 실행 = 1 : 1.4, 실행(긍정)이 오른쪽.
- Edge-to-edge 인셋을 처리한다. 스크롤은 목록과 시트 본문 안에서만.

### 6. 리터럴이 허용되는 예외 (스페이싱/폰트/색 아님)
테두리 두께, 아이콘 크기 상수, 브랜드 에셋 치수(카카오 버튼), 탭바·펄스 원처럼
디자인 고유의 구조적 치수에 이름을 붙인 상수(`TAB_BAR_HEIGHT`, `RING_OUTER`,
`BOTTOM_TAB_CLEARANCE` 등)는 토큰 대상이 아니므로 리터럴 사용을 허용한다.

### 7. 개인정보 제약
알람을 대신 설정해 준 친구에게 상대의 위치·도착 여부·알람 울림 상태를 보여 주는 UI 는
어떤 형태로도 만들지 않는다 (`docs/DESTINATION_ALARM_PRIVACY.md`).
