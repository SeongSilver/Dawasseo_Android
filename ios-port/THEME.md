# THEME — 디자인 토큰

색상은 colors.xml이 아닌 Compose `core/design/Color.kt`에 정의됨(라이트 테마 단일). 앱 테마: Theme.Material.Light.NoActionBar + windowLightStatusBar=true.

## 색상 토큰 (Color.kt)

| 토큰 | HEX | 용도 |
|---|---|---|
| WakepointPrimary | #0066CC | 브랜드/버튼 기본 |
| WakepointPrimaryHover | #005CB8 | pressed/hover |
| WakepointPrimaryActive | #0052A3 | active |
| WakepointSuccess | #10B981 | 성공/켜짐 |
| WakepointDanger | #EF4444 | 삭제/오류 |
| WakepointDangerBg | #FEF2F2 | 오류 배경 |
| WakepointCanvas | #FFFFFF | 카드/시트 배경 |
| WakepointBackground | #F5F5F7 | 페이지 배경 |
| WakepointParchment | #F5F5F7 | 입력 필드 채움(별칭) |
| WakepointMapBase | #E3E7EA | 지도 플레이스홀더 |
| WakepointDarkTile | #272729 | 다크 타일 |
| WakepointTabBar | #FFFFFF | 플로팅 탭 바 |
| WakepointInk | #171719 | 본문 텍스트 |
| WakepointIconLight | #C2C4C8 | 셰브런/빈 상태 아이콘 |
| WakepointIconInactive | #989BA2 | 비활성 탭/검색 아이콘 |
| (base) NeutralLine | #70737C | 구분선 계열 베이스 |
| (base) NeutralText | #37383C | 보조 텍스트 베이스 |

iOS: Asset Catalog Color Set으로 등록(라이트 전용, 다크 모드 값은 미정의 — 결정 필요).

## 타이포그래피 (Type.kt — Spoqa Han Sans Neo)

폰트 파일(assets/font/ 복사됨): spoqa_han_sans_neo_{thin,light,regular,medium,bold}.otf
매핑: Thin→Thin, Light→Light, Normal→Regular, Medium·SemiBold→Medium, Bold→Bold.

| 스타일 | size/lineHeight | weight | letterSpacing |
|---|---|---|---|
| displayMedium | 34/45 | Bold | -0.027em |
| headlineLarge | 28/38 | Bold | -0.024em |
| headlineMedium | 24/30 | Bold | -0.02em |
| titleLarge | 22/28 | Bold | -0.02em |
| titleMedium | 19/24 | Bold | -0.015em |
| titleSmall | 15/20 | Bold | -0.01em |
| bodyLarge | 16/24 | Medium | -0.01em |
| bodyMedium | 15/22 | Medium | 0 |
| bodySmall | 13/20 | Medium | 0 |
| labelLarge | 16/20 | SemiBold(→Medium otf) | 0 |
| labelMedium | 14/18 | SemiBold(→Medium otf) | 0 |
| labelSmall | 12/14 | Bold | 0.02em |

iOS: 폰트 등록(UIAppFonts) 후 Font.custom("SpoqaHanSansNeo-…", size:) + Text 스타일 확장으로 위 스케일 재현. (sp→pt 1:1 시작, Dynamic Type 지원 여부 결정 필요.)

## Corner Radius 토큰 (사용 빈도 기준)

| 값 | 용도 |
|---|---|
| 24dp | 다이얼로그/큰 시트 (WakepointErrorDialog, Component) |
| 16dp | 카드/기본 컴포넌트 (가장 빈번) |
| 14dp | 입력 필드/RadiusSelector 칩 |

## Spacing 스케일 (feature+design 사용 빈도 상위)

4, 6, 8, 10, 12, 14, 16, 18, 20, 24, 28, 32 dp — 4dp 기반 + 14/18 예외 존재.
주요 치수: 버튼/필드 높이 54~56dp, 아이템 행 64~72dp.

## 기타

- 상태바: 라이트 콘텐츠 배경(windowLightStatusBar=true) — iOS .dark 상태바 스타일(검정 글자).
- 앱 아이콘: adaptive icon XML(ic_launcher_foreground/background) — 벡터라 iOS용 1024px PNG 재제작 필요. 로고 벡터: logo_square.xml, logo_point.xml (SVG로 변환 후 Asset Catalog 등록).
- 지도 스타일: Google Maps 기본. 탭 바는 플로팅(흰 배경, 그림자) 커스텀 컴포넌트.
