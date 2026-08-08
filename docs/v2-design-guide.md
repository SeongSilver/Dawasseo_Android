# v2 디자인 가이드 (CLAUDE.md 미수정 — 별도 문서)

> CLAUDE.md 는 v1.5 세션과의 충돌 방지를 위해 수정하지 않는다. v2 작업에 적용할 디자인
> 강제 규칙과 토큰 매핑을 여기에 기록한다. **결론: v2 토큰 = v1.5 토큰과 거의 동일**하므로
> 기존 `core/design/*` 을 그대로 재사용하고, 신규 3색만 추가한다.

## 1. 강제 규칙 (v2 화면 코드에 적용)
- 하드코딩 금지: `16.dp`, `18.sp` 등 스페이싱/폰트 리터럴 직접 사용 금지.
  → `WakepointSpacing`, `WakepointSize`, `WakepointRadius`/`WakepointShape`,
  `MaterialTheme.typography`, `Wakepoint*` 컬러만 참조.
- 화면 타이틀은 전부 `headlineLarge`(T1 28/Bold) 로 통일. 신규 화면도 예외 없음.
- 좌우 여백 `WakepointSpacing.screenHorizontal`(16), 섹션 간격 `WakepointSpacing.sectionGap`(24)
  로 통일. 리스트 아이템 간격 `WakepointSpacing.cardGap`(12).
- 리터럴 허용 예외(v1.5와 동일): 테두리 두께(1/1.5/2dp), elevation, 아이콘 크기(20~32),
  브랜드 에셋 치수, 구조적 높이, 명명된 구조 상수.

## 2. v2 명세 토큰 → 기존 토큰 매핑 (재사용)
| v2 스펙 값 | 역할 | 기존 토큰 |
|---|---|---|
| #0066CC | Primary | `WakepointPrimary` |
| #E9F1FB | Primary Light | `WakepointPrimaryLight` |
| #F5F5F7 | 화면 배경 | `WakepointBackground` |
| #EFEFF1 | 입력/취소버튼 | `WakepointField` |
| #FFFFFF | 카드 | `WakepointCanvas` |
| #171719 / #5B5E66 / #9CA0A8 / #C7CAD1 | 텍스트 1~4 | `WakepointInk` / `WakepointText2` / `WakepointText3` / `WakepointDisabledText` |
| #EF4444 / #FDECEC | Danger | `WakepointDanger` / `WakepointDangerLight` |
| #0B8A4B / #E6F6EE | Positive | `WakepointPositive` / `WakepointPositiveLight` |
| #B25E00 / #FFF4E5 | Warning | `WakepointWarning` / `WakepointWarningLight` |
| #E3E4E8 | 토글 OFF 트랙 | `WakepointInactiveTrack` |
| #DEE0E4 | 기본 테두리 | `WakepointChipBorder` |
| #FFB020 | 즐겨찾기 별 | `WakepointStar` |
| 타이포 T1~T6 (28/18/16/15/13/11) | | `headlineLarge/titleSmall/titleMedium/bodyLarge/bodySmall/labelSmall` |
| 간격 4/8/12/16/24/32 | | `WakepointSpacing.xs~xxl` |
| 라운드 8/12/20/full | | `WakepointRadius.badge/control/card` + `WakepointShape.full` |
| 높이 52/44/56 | | `WakepointSize.buttonHeight/chipHeight/rowHeight` |

## 3. 신규 토큰 (기존에 없어 추가 — `core/design/ColorV2.kt`)
| 이름 | 값 | 용도 | 근거 |
|---|---|---|---|
| `WakepointChipActiveBorder` | #1668B8 | 선택된 칩/요일 원형 테두리(2px) | 기존 선택표시는 배경 tint뿐, 2px 진한 테두리색 없음 |
| `WakepointImminentRing` | #F0B429 | 막차 "임박" 카드 inset ring 1.5px | Warning(#B25E00) 텍스트색과 구분되는 링 전용색 |
| `WakepointButtonDisabledBg` | #F4F4F5 | 비활성 Primary 버튼 배경 | 기존 disabled는 텍스트색(#C7CAD1)만, 버튼 BG 없음 |

- 실사용 목업이 정의(6단계) 밖 크기(30/24/23/22/19/17px 등)를 다수 쓰나, **강제 규칙상
  가장 가까운 6단계 role 로 매핑**한다(임의 리터럴 금지). 막차 카운트다운 초대형 숫자(52px)만
  예외적으로 hero 스타일이 필요하면 `titleLarge`(22) 초과라 별도 판단 → decisions 기록.
