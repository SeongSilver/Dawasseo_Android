# DESIGN.md - 다왔어 디자인 시스템

> 현재 디자인은 확정되지 않았다. 이 문서는 새 디자인이 확정되면 토큰과 규칙을 기록하는 자리다.

## 현재 상태 (2026-11-18)

- 기존 디자인 요소(색상 토큰, Pretendard 폰트, 타이포그래피, 모양, 로고, 시안 이미지)는 모두 제거했다.
- 앱은 Material 3 기본값으로 렌더링된다.
- 화면별 디자인 요구사항은 `docs/DESIGN_SPEC.md`에 정리돼 있다. 새 디자인은 그 문서의 요구사항을 충족해야 한다.

## 적용 지점

```text
app/src/main/java/com/wakepoint/app/core/design/
  Theme.kt          # WakepointTheme: colorScheme, typography, shapes 주입
  Component.kt      # 공통 컴포넌트 API (스타일 비어 있음)
  RadiusSelector.kt # 반경 드롭다운
app/src/main/res/
  drawable/         # 앱 아이콘, 로고, 마커, 알림 아이콘 (현재 임시 ic_launcher.xml만 있음)
  font/             # 비어 있음
  values/styles.xml # XML 테마 (폰트 미지정)
```

## 확정 시 기록할 항목

- 컬러 토큰과 다크 모드 대응
- 타이포그래피 (폰트, 크기, weight)
- 모양 (버튼, 카드, 입력, 시트 radius)
- 간격 체계
- 브랜드 자산 (앱 아이콘, 로고, 마커, 알림 small icon)
- 컴포넌트별 규칙
- 금지 사항
