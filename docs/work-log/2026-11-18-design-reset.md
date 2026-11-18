# 2026-11-18 Design Reset

브랜치: `design-reset`

## 완료한 작업

- 화면별 디자인 요구사항 명세 `docs/DESIGN_SPEC.md`를 작성했다.
  - 화면 흐름, 공통 컴포넌트 API, 공통 상태 표현, 플랫폼 제약, 필요한 브랜드 자산
  - Splash, 로그인, 회원가입, 홈(지도/검색 시트/알람 설정 시트), 알람 목록, 알람음 목록, 친구, 프로필, 알람 울림, 시스템 알림, 하단 탭바
  - 디자인 적용 전 선행 정리 항목과 권장 적용 순서
- 프로젝트 내부 디자인 요소를 모두 제거했다.
  - `core/design/Color.kt`, `Type.kt` 삭제. `Theme.kt`는 MaterialTheme 기본값 래퍼만 남김
  - `Component.kt`, `RadiusSelector.kt`는 API만 유지하고 색/모양/크기/폰트 지정 제거. `SectionLabel`, `MessageText`, `ButtonRow` 공통 헬퍼 추가
  - Pretendard 폰트 4종과 `font/pretendard.xml` 삭제, `styles.xml`의 fontFamily 제거
  - 로고 PNG 3종 삭제. 앱 아이콘은 임시 `ic_launcher.xml`로 변경, 지도 마커는 기본 마커로 변경, 로고 자리는 앱 이름 텍스트로 대체
  - 화면 7개(auth, profile, home, alarms, friends, alarmalert, navigation)에서 색상, FontWeight, 모양, 테두리, 배경, 고정 크기 제거
  - 구 디자인 시안 `docs/design/screens/*.png` 12장 삭제
- 문서와 규칙을 갱신했다.
  - `docs/DESIGN.md`는 미확정 상태와 적용 지점만 남김
  - `AGENTS.md`, `docs/GOTCHAS.md`, `README.md`의 디자인 규칙을 "미확정, DESIGN_SPEC.md 참조"로 교체

## 유지한 것

- 카카오 로그인 버튼 이미지와 고정 크기 (카카오 브랜드 가이드 준수)
- 공통 컴포넌트 함수 이름과 시그니처 (화면 호출부 호환)
- 모든 기능 동작, ViewModel, Repository, 서비스 코드
- 코드에 하드코딩된 한글 문자열 (strings.xml 이관은 별도 작업)

## 검증

- `assembleDebug`: 성공
- `test`: 성공
- 실기기 확인: 미실행

## 남은 이슈

- 앱 아이콘이 임시 벡터다. 새 브랜드 자산이 필요하다.
- 알림 small icon과 액센트 색이 기본값이다.
- 친구 화면 한글 문자열 약 40곳이 코드에 남아 있다.

## 다음 작업

1. 새 디자인 토큰 확정 후 `Theme.kt`에 주입
2. `Component.kt` 스타일 적용
3. 브랜드 자산 추가 (앱 아이콘, 로고, 마커, 알림 아이콘, 폰트)
4. `DESIGN_SPEC.md` 우선순위 순으로 화면 레이아웃 적용
