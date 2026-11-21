# 다왔어 디자인 규칙 (DESIGN.md)

작성일 2026-11-21. 이 문서가 UI 의 단일 기준이다.
원본과 화면별 산출물은 `design-handoff/` 에 있다: `screens/{light,dark}/<이름>.html`(1px = 1dp,
`data-layer` 가 컴포넌트 이름), `png/{light,dark}/<이름>.png`, `tokens/colors.json`.

구현 상태와 디자인과 다르게 구현한 곳은 `docs/DESIGN_PROGRESS.md` 에 적는다.

> 이 문서는 2026-11-21 에 기존 v1.5 디자인 가이드를 대체했다. 코드의 단일 소스(SSOT)는
> `app/src/main/java/com/wakepoint/app/core/design/` 의 `Theme.kt`(색) · `Type.kt`(타입) ·
> `Shape.kt`(모서리) · `Spacing.kt`(간격·치수) · `Component.kt`(공통 컴포넌트) 다.
> 앱에서 `Color(0x…)` 리터럴이 허용되는 곳은 `Theme.kt` 하나뿐이다.

## 1. 원칙

- 디자인 언어는 Material 3 (Compose `androidx.compose.material3`). M3 에 없는 UI 는 만들지 않는다.
- 색은 아래 표의 역할만 쓴다. 화면 코드에 hex 를 직접 쓰지 않는다. 새 색을 만들거나 섞거나 알파를 바꾸지 않는다.
- 라이트와 다크 두 테마. 시스템 설정을 따른다.
- 빨강(Negative)은 **입력 오류와 실패 메시지에만** 쓴다. 삭제 · 차단 · 로그아웃 버튼, 배지, 장식에 쓰지 않는다.
- Brand 파랑은 핵심 액션과 선택 상태에만: 채움 버튼, 켜진 스위치, 선택 탭, 선택 체크, 글자 버튼, 포커스 테두리, 목적지 마커.
- 굵기는 Regular 400 과 Medium 500 두 가지. Bold 를 쓰지 않는다.
- 그림자는 쓰지 않는다. 면은 채움과 테두리로 구분한다.
- 로딩은 스피너 없이 버튼 문구 변경(“처리 중” · “저장 중” · “검색 중”) + 비활성.

## 2. 색 역할

`design-handoff/tokens/colors.json` 과 `design-handoff/tokens/Theme.reference.kt` 에 같은 값이 있다. `#RRGGBBAA` 의 끝 두 자리는 알파다 (Compose 에서는 `Color(0xAARRGGBB)`).

| 역할 | Montage 토큰 | 다크 | 라이트 | 쓰는 곳 |
|---|---|---|---|---|
| `background` | Background Neutral/Primary | `#1B1C1E` | `#FFFFFF` | 화면 배경, 테두리 카드 배경, 테두리 필드의 라벨 배경 |
| `backgroundSecondary` | Background Neutral/Secondary | `#0F0F10` | `#F7F7F8` | 하단 탭바 |
| `surfaceElevated` | Surface Elevated/Primary | `#212225` | `#FFFFFF` | 바텀시트, 다이얼로그, 지도 위 검색바와 현재 위치 버튼, 알림 카드 |
| `surfaceElevatedSecondary` | Surface Elevated/Secondary | `#141415` | `#F7F7F8` | 시트 안 읽기 전용 행, 장소 검색 결과 목록, 시트 안 안내 메시지 |
| `fill` | Surface Neutral/Secondary | `#70737C38` | `#70737C14` | 채움 필드, 화면 위 읽기 전용 행, 보조(tonal) 버튼, 친구 검색바, 진행 pill, 안내 메시지, 꺼진 스위치 트랙, 빈 상태 아이콘 원 |
| `fillWeak` | Surface Neutral/Tertiary | `#70737C1F` | `#70737C0D` | 중립 pill, 알람 울림 바깥 원 |
| `fillStrong` | Surface Neutral/Strong | `#70737C47` | `#70737C29` | 아바타 배경, 알람 울림 중앙 원 |
| `card` | Dark: Surface Neutral/Tertiary · Light: Background Neutral/Secondary | `#70737C1F` | `#F7F7F8` | 활성 알람 카드, 프로필 메뉴 카드, 사운드 목록 카드, 빈 상태 카드 |
| `disabledContainer` | Surface Disable/Primary | `#2E2F33` | `#F4F4F5` | 비활성 버튼 · 스위치 배경 |
| `disabledContent` | Foreground Disable/Primary | `#989BA229` | `#37383C29` | 비활성 버튼 글자, 비활성 스위치 핸들 |
| `text` | Foreground Neutral/Primary | `#F7F7F8` | `#171719` | 본문 · 제목, 보조 버튼 글자 |
| `textStrong` | Foreground Neutral/Strong | `#FFFFFF` | `#000000` | 로고 심볼 |
| `textSecondary` | Foreground Neutral/Secondary | `#C2C4C8E0` | `#2E2F33E0` | 보조 텍스트, 일반 아이콘 |
| `textTertiary` | Foreground Neutral/Tertiary | `#AEB0B69C` | `#37383C9C` | 필드 라벨, 필드 보조 문구 |
| `inactive` | Foreground Inactive/Primary | `#5A5C63` | `#989BA2` | 비선택 탭(하단 탭바, 요청/친구 탭), 꺼진 스위치 핸들 · 테두리, 시트 핸들 |
| `line` | Line Neutral/Primary Opaque | `#37383C` | `#E1E2E4` | 필드 · 테두리 버튼 · 카드 테두리, 구분선 |
| `dim` | Effect Dimmer/Primary | `#171719BD` | `#17171985` | 시트 · 다이얼로그 뒤 scrim (이 값 그대로, 추가 알파 금지) |
| `brand` | Surface Brand/Primary · Foreground Brand/Primary | `#3385FF` | `#0066FF` | 채움 버튼, 켜진 스위치, 탭 인디케이터 · 배지, 선택 체크, 글자 버튼, 선택 탭, 목적지 마커 |
| `brandPressed` | Surface Brand/Heavy | `#0066FF` | `#0054D1` | 채움 버튼 pressed |
| `onBrand` | Static/White | `#FFFFFF` | `#FFFFFF` | brand 채움 위 글자 · 아이콘, 스위치 핸들 |
| `brandSubtle` | Surface Brand/Subtle | `#3385FF0D` | `#0066FF0D` | 선택된 행(친구 검색, 알람음), 하단 탭 선택 인디케이터 |
| `brandLineStrong` | Line Brand/Strong | `#3385FF6E` | `#0066FF6E` | 필드 포커스 테두리 |
| `brandFocus` | Line Brand/Focus | `#3385FF1F` | `#0066FF1F` | 포커스 링 |
| `negative` | Foreground Negative/Primary | `#FF6363` | `#FF4242` | 오류 아이콘 |
| `negativeText` | Dark: Foreground Negative/Primary · Light: Foreground Negative/Strong | `#FF6363` | `#E52222` | 오류 메시지 글자, 필드 오류 라벨 · 보조 문구 |
| `negativeSurface` | Surface Negative/Primary | `#FF636314` | `#FF424214` | 오류 메시지 배경 |
| `negativeLine` | Line Negative/Primary | `#FF63636E` | `#FF42426E` | 오류 메시지 테두리 |
| `negativeLineStrong` | Line Negative/Strong | `#FF636385` | `#FF424285` | 필드 오류 테두리 |
| `positive` | Foreground Positive/Primary | `#1ED45A` | `#00BF40` | 완료 pill(친구 · 승인) 글자와 체크 |
| `positiveSurface` | Surface Positive/Primary | `#1ED45A14` | `#00BF4014` | 완료 pill 배경 |
| `cautionary` | Foreground Cautionary/Primary | `#FFA938` | `#FF9200` | 알림 권한 안내 글자 |
| `cautionarySurface` | Surface Cautionary/Primary | `#FFA93814` | `#FF920014` | 알림 권한 안내 배경 |

M3 컴포넌트의 기본 색에 기대지 말고 각 컴포넌트의 `colors` 파라미터에 위 역할을 넘긴다. 특히:

- `ModalBottomSheet`: `containerColor = surfaceElevated`, `scrimColor = dim`.
- `NavigationBar`: `containerColor = backgroundSecondary`. 항목은 선택 `brand` (아이콘 · 라벨), 인디케이터 `brandSubtle`, 비선택 `inactive`.
- `Switch`: 켜짐 트랙 `brand` / 핸들 `onBrand`, 꺼짐 트랙 `fill` / 테두리와 핸들 `inactive`. 핸들 아이콘 없음.
- 채움 필드 배경 `fill`, 밑줄 `line`. 테두리 필드 테두리 `line`.

## 3. 타입 — Pretendard

| 스타일 | 크기/줄높이 (sp) | 굵기 | 자간 (sp) | 쓰는 곳 |
|---|---|---|---|---|
| displayLarge | 57/64 | 400 | -0.25 | 녹음 경과 시간 |
| displayMedium | 45/52 | 400 | 0 | 알람 울림 “도착했습니다” |
| headlineSmall | 24/32 | 400 | 0 | 시트 · 다이얼로그 제목, 알람 중지 버튼 라벨 |
| titleLarge | 22/28 | 400 | 0 | 상단 바 제목, 빈 상태 제목 (브랜드 워드마크는 22/28 · 500) |
| titleMedium | 16/24 | 500 | 0.15 | 섹션 제목, 행의 이름, 버튼 m 라벨 |
| titleSmall | 14/20 | 500 | 0.1 | 요청/친구 탭 |
| bodyLarge | 16/24 | 400 | 0.5 | 본문, 필드 입력값, 목록 행 제목 |
| bodyMedium | 14/20 | 400 | 0.25 | 보조 설명, 메시지 |
| bodySmall | 12/16 | 400 | 0.4 | 떠오른 필드 라벨, 필드 보조 문구, 행 부제 |
| labelLarge | 14/20 | 500 | 0.1 | 버튼 s 라벨, 상태 pill, 섹션 라벨 |
| labelMedium | 12/16 | 500 | 0.5 | 하단 탭 라벨 |
| labelSmall | 11/16 | 500 | 0.5 | 탭 배지 숫자 |

## 4. 모양 · 간격

- 모서리: 텍스트 필드 4dp · 상태 pill 8dp · 메시지 영역 12dp · 카드와 묶음 행 16dp · 시트(위쪽) · 다이얼로그 · 빈 상태 카드 28dp · 버튼 · 검색바 · 스위치 · 아바타는 완전 원형.
- 간격은 4dp 그리드: 4 · 8 · 12 · 16 · 24 · 32 · 48. 좌우 여백은 폼과 시트 24dp, 목록 16dp. 카드 안 여백 16dp, 카드 사이 12dp, 필드 사이 16dp.
- 터치 영역 최소 48dp.
- 버튼은 누르는 동안 모서리가 한 단계 작아진다 (m: 원형 → 12dp).

## 5. 공통 컴포넌트 (`core/design/Component.kt`, API 는 유지)

| 앱 컴포넌트 | 구현 | 수치 · 색 |
|---|---|---|
| `WakepointButton` | 채움 Button | 높이 56 (카드 · 행 안은 40), 가로 패딩 24 (40 높이는 16), 아이콘 24 (20), 배경 `brand` 글자 `onBrand`. 비활성 `disabledContainer` / `disabledContent`. 알람 중지만 높이 96 · 폭 312 · headlineSmall |
| `WakepointSecondaryButton` | 테두리 Button (취소 · 삭제 · 거절 · 차단 · 가져오기 · 요청 취소) 또는 tonal (친구 추가 · 권한 요청 · 정지) | 테두리 1dp `line`, 글자 `text`. tonal 은 배경 `fill`, 글자 `text`. 파괴적 액션도 빨강을 쓰지 않고 아이콘으로 구분 |
| `WakepointTextField` | 화면 배경 위는 OutlinedTextField, 시트와 카드 안은 채움 TextField. 한 폼에 한 종류 | 높이 56, 모서리 4, 라벨은 안에 있다가 입력하면 위로. 포커스 3dp `brandLineStrong`, 오류 3dp `negativeLineStrong` + 오류 아이콘 `negative` + 라벨 · 보조 문구 `negativeText` |
| `WakepointCard` | 채움 Card (`card`) 또는 테두리 Card (`background` + 1dp `line`) | 모서리 16, 안 여백 16. 활성 알람 = 채움, 비활성 알람 = 테두리 + 주소 글자 `textSecondary` |
| `WakepointConfirmDialog` | AlertDialog | 배경 `surfaceElevated`, 모서리 28, 아이콘 24 `textSecondary`, 버튼 2개 가로 (취소 테두리 / 실행 채움 + 아이콘) |
| `WakepointHeader` | TopAppBar 64dp | 왼쪽 심볼 32 + “다왔어”, 오른쪽 액션 슬롯 |
| `StatusPill` | Surface + 라벨 | 높이 32, 모서리 8, 아이콘 18. 완료(친구 · 승인) `positiveSurface` + `positive` + 체크 / 진행(요청 중 · 요청 받음 · 대기) `fill` + `text` + 시계 / 부정(거절됨 · 차단됨 · 거절) 테두리 `line` + `text` + 금지 아이콘 / 중립(권한 없음) `fillWeak` + `textSecondary` |
| `SoundOptionRow` | ListItem | 높이 64 ~ 72, 선택됨 = 배경 `brandSubtle` + 오른쪽 체크 `brand` |
| `BottomSheetHandle` | 기본 DragHandle | 32×4, `inactive` |
| `RadiusSelector` | ExposedDropdownMenuBox | 필드는 `WakepointTextField` 와 같은 모양 + 아래 화살표 |
| 메시지 영역 | Row | 오류: 배경 `negativeSurface`, 테두리 1dp `negativeLine`, 아이콘 `negative`, 글자 `negativeText`. 안내: 배경 `fill`(시트 안은 `surfaceElevatedSecondary`), 글자 `textSecondary`. 알림 권한 안내만 `cautionarySurface` + `cautionary` |

아이콘은 Material Symbols 계열이다 (Compose 의 material-icons 로 대체 가능): location_on(홈 탭, 목적지, 현재 위치 버튼) · alarm(알람 탭) · groups(친구 탭) · person(프로필 탭) · add · arrow_back · keyboard_arrow_down/up · chevron_forward · check · schedule · do_not_disturb_on · search · mic · delete · error · volume_up · music_note · send · settings · download · chat_bubble(고객센터) · move_item(로그아웃). 선택된 홈 · 알람 탭은 채움형 아이콘.

## 6. 화면 목록

각 화면은 `design-handoff/` 아래에 `screens/light/<이름>.html`, `screens/dark/<이름>.html`, `png/light/<이름>.png`, `png/dark/<이름>.png` 네 파일이 있다. HTML 은 1px = 1dp 이고 요소의 `data-layer` 속성에 대응하는 컴포넌트 이름이 있다.

| 파일 이름 | 화면 · 상태 | 명세 절 | 구현 위치 |
|---|---|---|---|
| `Login-default` | 로그인 · 기본 | 2 | `feature/auth/AuthScreen.kt · AuthScreen` |
| `Login-submitting` | 로그인 · 제출 중 | 2 | `feature/auth/AuthScreen.kt · AuthScreen` |
| `Login-error` | 로그인 · 오류 | 2 | `feature/auth/AuthScreen.kt · AuthScreen` |
| `Login-info` | 로그인 · 안내 | 2 | `feature/auth/AuthScreen.kt · AuthScreen` |
| `Alarms-list` | 알람 목록 · 기본 (활성/비활성) | 5 | `feature/alarms/AlarmsScreen.kt` |
| `Alarms-open` | 알람 목록 · A안 카드 펼침 | 5 | `feature/alarms/AlarmsScreen.kt` |
| `Alarms-edit` | 알람 수정 · B안 별도 화면 | 5 | `feature/alarms/AlarmsScreen.kt` |
| `Alarms-empty` | 알람 목록 · 빈 상태 | 5 | `feature/alarms/AlarmsScreen.kt` |
| `Alarms-updating` | 알람 목록 · 갱신 중 + 오류 | 5 | `feature/alarms/AlarmsScreen.kt` |
| `Alarms-delete` | 알람 목록 · 삭제 확인 | 5 | `feature/alarms/AlarmsScreen.kt` |
| `Sheet-default` | 알람 설정 시트 · 기본 | 4.3 | `AlarmSetupSheet (홈 · 친구 공용)` |
| `Sheet-noperm` | 알람 설정 시트 · 알림 권한 없음 | 4.3 | `AlarmSetupSheet (홈 · 친구 공용)` |
| `Sheet-saving` | 알람 설정 시트 · 저장 중 | 4.3 | `AlarmSetupSheet (홈 · 친구 공용)` |
| `Sheet-error` | 알람 설정 시트 · 오류 | 4.3 | `AlarmSetupSheet (홈 · 친구 공용)` |
| `Sheet-friend` | 알람 보내기 시트 · 친구 화면 | 4.3 | `AlarmSetupSheet (홈 · 친구 공용)` |
| `Friends-requests` | 친구 · 요청 탭 | 7 | `feature/friends/FriendsScreen.kt` |
| `Friends-list` | 친구 · 친구 탭 (카드 펼침) | 7 | `feature/friends/FriendsScreen.kt` |
| `Friends-empty` | 친구 · 빈 상태 | 7 | `feature/friends/FriendsScreen.kt` |
| `Friends-error` | 친구 · 오류 | 7 | `feature/friends/FriendsScreen.kt` |
| `Friends-block` | 친구 · 차단 확인 | 7 | `feature/friends/FriendsScreen.kt` |
| `TabBar` | 하단 탭바 · 선택 상태 4종 | 11 | `navigation/WakepointNavGraph.kt` |
| `Ringing` | 알람 울림 · 별칭 + 주소 | 9 | `feature/alarmalert/AlarmAlertActivity.kt` |
| `Ringing-alias` | 알람 울림 · 별칭만 | 9 | `feature/alarmalert/AlarmAlertActivity.kt` |
| `Splash` | 스플래시 | 1 | `feature/auth/AuthScreen.kt · SplashScreen` |
| `Home` | 홈 · 위치 권한 있음 | 4.1 | `feature/home/HomeScreen.kt` |
| `Home-noperm` | 홈 · 위치 권한 없음 (기본 위치) | 4.1 | `feature/home/HomeScreen.kt` |
| `Place-idle` | 장소 검색 시트 · 입력 전 | 4.2 | `feature/home/HomeScreen.kt · 장소 검색 시트` |
| `Place-searching` | 장소 검색 시트 · 검색 중 | 4.2 | `feature/home/HomeScreen.kt · 장소 검색 시트` |
| `Place-results` | 장소 검색 시트 · 결과 있음 | 4.2 | `feature/home/HomeScreen.kt · 장소 검색 시트` |
| `Place-none` | 장소 검색 시트 · 결과 없음 | 4.2 | `feature/home/HomeScreen.kt · 장소 검색 시트` |
| `Place-error` | 장소 검색 시트 · 오류 | 4.2 | `feature/home/HomeScreen.kt · 장소 검색 시트` |
| `Signup-default` | 회원가입 · 기본 | 3 | `feature/auth/AuthScreen.kt · SignUpScreen` |
| `Signup-submitting` | 회원가입 · 제출 중 | 3 | `feature/auth/AuthScreen.kt · SignUpScreen` |
| `Signup-error` | 회원가입 · 오류 (인라인) | 3 | `feature/auth/AuthScreen.kt · SignUpScreen` |
| `Signup-info` | 회원가입 · 안내 | 3 | `feature/auth/AuthScreen.kt · SignUpScreen` |
| `FriendSearch-idle` | 친구 검색 시트 · 입력 전 | 7.4 | `feature/friends/FriendsScreen.kt · 친구 검색 시트` |
| `FriendSearch-searching` | 친구 검색 시트 · 검색 중 | 7.4 | `feature/friends/FriendsScreen.kt · 친구 검색 시트` |
| `FriendSearch-results` | 친구 검색 시트 · 결과 (선택됨) | 7.4 | `feature/friends/FriendsScreen.kt · 친구 검색 시트` |
| `FriendSearch-none` | 친구 검색 시트 · 결과 없음 | 7.4 | `feature/friends/FriendsScreen.kt · 친구 검색 시트` |
| `FriendSearch-error` | 친구 검색 시트 · 오류 | 7.4 | `feature/friends/FriendsScreen.kt · 친구 검색 시트` |
| `Sounds` | 알람음 목록 | 6 | `feature/alarms/AlarmsScreen.kt · SoundListScreen, RecordingSheet` |
| `Sounds-recording` | 알람음 · 녹음 시트 | 6 | `feature/alarms/AlarmsScreen.kt · SoundListScreen, RecordingSheet` |
| `Profile` | 프로필 | 8 | `feature/profile/ProfileScreen.kt` |
| `Notifications` | 시스템 알림 2종 | 10 | `core/notification/AlarmNotificationManager.kt` |

## 7. 플랫폼 · 개인정보 제약

- Edge-to-edge. 상태바 40dp, 내비게이션바 24dp 영역 아래로 배경이 깔린다. 하단 탭바와 시트 하단 버튼은 `navigationBarsPadding`.
- 스크롤은 목록과 시트 본문 안에서만. 헤더, 탭바, 시트 하단 버튼은 고정. 스크롤바를 그리지 않는다.
- 카카오 로그인 버튼은 공식 이미지 에셋을 그대로 쓴다. 디자인의 점선 상자는 자리 표시다.
- 지도의 내 위치 점은 Google Maps SDK 기본 표시다. 지도 스타일은 테마별 스타일 JSON 으로 맞춘다 (바탕 `backgroundSecondary` 계열의 무채색).
- 알람을 대신 설정해 준 친구에게 상대의 위치, 도착 여부, 알람 울림 상태를 보여 주는 UI 는 어떤 형태로도 만들지 않는다 (`docs/DESTINATION_ALARM_PRIVACY.md`).

## 8. 아직 정하지 않은 것 (기본값)

| 항목 | 기본값 | 다른 안 |
|---|---|---|
| 알람 수정 | B안: 별도 수정 화면 (`Alarms-edit`) | A안: 카드 펼침 (`Alarms-open`) |
| 친구 추가 진입점 | 헤더의 “친구 추가” 버튼만. FAB 없음 | FAB 복원 |
| 요청이 0건일 때 친구 화면 기본 탭 | “친구” 탭 | — |
| 알람 울림 화면 테마 | 시스템 테마를 따름 | 항상 다크 |

## 9. 구현 메모

- 글꼴: 스펙은 Pretendard 지만 `res/font` 에 리소스가 없어 시스템 폰트로 구현했다.
  폰트 파일을 넣으면 `core/design/Type.kt` 의 `WakepointFontFamily` 한 곳만 바꾸면 된다.
- 테마 전환: `WakepointTheme()` 이 `isSystemInDarkTheme()` 을 따른다. @Preview 는
  `core/design/Preview.kt` 의 `@WakepointScreenPreviews` · `@WakepointComponentPreviews`
  (라이트 · 다크 한 쌍) 를 쓴다.
- 파괴적 액션(삭제 · 차단 · 로그아웃)은 `WakepointDangerButton` 을 쓰더라도 빨강이 아니라
  테두리 버튼 + 아이콘으로 그려진다. 빨강은 입력 오류와 실패 메시지 전용이다.

## 10. 알려진 한계

- 문구 일부(`auth_intro`, `alarm_empty`, 약관 안내, 오류 문구, 회원가입 도움말)는 자리 표시다. `strings.xml` 의 실제 값을 쓴다.
- 브랜드 심볼(`design-handoff/assets/ic_brand_mark.xml` → `app/src/main/res/drawable/ic_brand_mark.xml`)은 초안이다. 앱 아이콘 · 마커 · 알림 아이콘 제작 전에 벡터 툴에서 다듬어야 한다.
- 대비가 4.5:1 에 못 미치는 조합이 있다 (팔레트 값을 그대로 쓴 결과): 필드 라벨, 비선택 탭, 다크의 채움 버튼 흰 글자(3.5:1), 라이트의 완료 pill(2.3:1) · 알림 권한 안내(2.1:1) · 오류 메시지(4.1:1).
- 360dp 폭, 키보드가 올라온 상태는 따로 그리지 않았다.
