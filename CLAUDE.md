# CLAUDE.md — 다왔어(Dawasseo) Android

Jetpack Compose 앱. 디자인 시스템은 `app/src/main/java/com/wakepoint/app/core/design/` 에 있다.
UI를 수정할 때는 아래 **디자인 가이드**를 반드시 지킬 것.

---

## 디자인 가이드 (v1.5 통일 리디자인 — 강제 규칙)

> 출처: `design/v1.5-redesign.dc.html` (claude.ai 디자인 프로젝트 "세 가지 디자인 컨셉 보드").
> 단일 소스(SSOT): `core/design/Type.kt`, `Spacing.kt`, `Shape.kt`, `Color.kt`.

### 0. 절대 규칙
- **하드코딩 금지**: 화면(feature)·내비 코드에 `16.dp`, `18.sp` 같은 스페이싱/폰트 리터럴을 직접 쓰지 말 것.
  반드시 아래 토큰(`WakepointSpacing`, `WakepointSize`, `WakepointShape`/`WakepointRadius`,
  `MaterialTheme.typography`, `Wakepoint*` 컬러)을 참조한다.
- **화면 타이틀은 전부 동일 스타일**: 탭 화면 타이틀(알람/친구/마이 등)은 `headlineLarge`(T1, 28/Bold) 하나로 통일.
  화면마다 크기·굵기가 달라선 안 된다.
- **좌우 여백·섹션 간격은 전 화면 동일 토큰**: 화면 좌우 여백은 `WakepointSpacing.screenHorizontal`(16),
  섹션 간격은 `WakepointSpacing.sectionGap`(24)로 통일.
- 디자인에 없는 값이 필요하면 **임의로 만들지 말고** 가장 가까운 기존 토큰을 쓰거나 사람에게 물어볼 것.
- **비즈니스 로직/ViewModel/데이터 레이어는 UI 리디자인 중 수정하지 않는다.** UI 레이어만 변경.

### 1. 타이포 — 6단계 (`Type.kt`, Material3 role에 매핑)
| 단계 | 크기/굵기 | Material role |
|---|---|---|
| T1 화면 타이틀 | 28 / Bold | `headlineLarge`, `headlineMedium` |
| T2 섹션 타이틀 | 18 / SemiBold | `titleSmall` |
| T3 카드 제목 | 16 / SemiBold | `titleMedium` |
| T4 본문·기본 | 15 / Regular | `bodyLarge`, `bodyMedium` |
| T5 보조 | 13 / Regular | `bodySmall` |
| T6 캡션·배지 | 11 / Medium | `labelSmall` |
| 버튼 텍스트 | 15 / SemiBold | `labelLarge`, `labelMedium` |

- 인라인 `fontSize`/`fontWeight` 지정 금지 — 위 role만 사용.
- 예외(6단계 밖, 그대로 유지): `displayMedium`(스플래시), `titleLarge`(22 — 전체화면 알람 알림 hero·다이얼로그 제목).
- 폰트: 디자인 스펙은 Pretendard지만 프로젝트에 폰트 리소스가 없어 기존 번들 폰트(Spoqa Han Sans Neo) 유지.
  Spoqa는 SemiBold(600)가 없어 Medium(500)으로 매핑된다.

### 2. 간격 — 8pt 그리드 (`Spacing.kt`, `WakepointSpacing`)
4·8·12·16·24·32 만 사용: `xs`=4, `sm`=8, `md`=12, `lg`=16, `xl`=24, `xxl`=32.
의미 토큰:
- 화면 좌우 여백 `screenHorizontal`(16) · 카드 내부 패딩 `cardPadding`(16)
- 카드↔카드 `cardGap`(12) · 섹션↔섹션 `sectionGap`(24)
- 섹션 타이틀↔콘텐츠 `sectionTitleGap`(8) · 라벨↔필드 `labelFieldGap`(8) · 아이콘↔텍스트 `iconTextGap`(8)

컴포넌트 높이 (`WakepointSize`): 버튼/필드 52 · 칩 44 · 설정·토글 행 56 · FAB/원형버튼 48.

### 3. 라운드 — 4단계 (`Shape.kt`, `WakepointShape`)
`badge`(8, 배지·작은 요소) · `control`(12, 입력·칩·버튼) · `card`(20, 카드·시트) · `full`(검색·탭바·FAB).
`MaterialTheme.shapes` 에도 small=badge / medium=control / large=card 로 연결됨.

### 4. 컬러 — 역할 토큰 (`Color.kt`)
`WakepointPrimary`(#0066CC) · `WakepointPrimaryLight`(#E9F1FB, 선택 칩/탭 pill/아바타) ·
`WakepointBackground`(#F5F5F7) · `WakepointCanvas`(#FFF, 카드) · `WakepointField`(#EFEFF1, 입력·Secondary 버튼) ·
`WakepointInk`(Text1 #171719) · `WakepointTextSecondary`(Text2 #5B5E66) · `WakepointTextTertiary`(Text3 #9CA0A8) ·
`WakepointDanger`/`WakepointDangerLight`(#EF4444/#FDECEC) · `WakepointPositive`/`WakepointPositiveLight`(#0B8A4B/#E6F6EE) ·
`WakepointWarning`/`WakepointWarningLight`(#B25E00/#FFF4E5) · `WakepointInactiveTrack`(#E3E4E8) ·
`WakepointDisabledText`(#C7CAD1) · `WakepointChipBorder`(#DEE0E4) · `WakepointStar`(#FFB020, 즐겨찾기 별).

### 5. 컴포넌트 규칙 (`Component.kt`)
- 버튼 3종: `WakepointButton`(Primary), `WakepointSecondaryButton`(Field), `WakepointDangerButton`(Danger Tonal).
  모두 높이 52·라운드 12·텍스트 15/SemiBold.
- **2분할 버튼은 항상 1:1**, 긍정 액션(저장/추가)은 **오른쪽**, 취소·삭제는 **왼쪽**. 삭제는 Danger Tonal.
- 카드는 `WakepointCard`(라운드 20·패딩 16). 배지는 `StatusPill`(라운드 8·T6). 필드는 `WakepointTextField`(52·라운드 12).
- 반경 선택은 `RadiusSelector`(항상 3열 그리드·칩 44·라운드 12).
- 설정 행은 `SettingsRow`(높이 56·좌우 16·구분선 인셋 16).

### 6. 리터럴이 허용되는 예외 (스페이싱/폰트 아님)
테두리 두께(1/1.5/2dp), elevation(shadow/tonal), 아이콘 크기(20~32dp), 브랜드 에셋 치수(스플래시 로고·카카오 버튼),
탭바/상단바 등 구조적 높이, 그리고 이름 붙인 구조 상수(`BOTTOM_TAB_CLEARANCE`, `BOTTOM_LIST_CLEARANCE`)는
디자인 토큰 대상이 아니므로 리터럴 사용을 허용한다.

### 7. 리디자인 제외 화면
전체화면 알람 알림(`feature/alarmalert/AlarmAlertActivity`, 디자인 #07 "OS 화면")은 리디자인 대상이 아니다.
