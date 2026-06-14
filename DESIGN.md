# DESIGN.md - 다왔어 디자인 시스템

> Android Native + Jetpack Compose + Material 3 기준의 UI 디자인 가이드.

---

## 디자인 원칙

- 위치 기반 알람 앱답게 사용자가 현재 상태와 다음 행동을 빠르게 파악할 수 있어야 한다.
- 지도, 알람 상태, 목적지 정보가 화면의 중심이 된다.
- 가족/친구 대리 설정 기능은 안전하고 신뢰감 있게 보여야 한다.
- 장식보다 명확성, 접근성, 반복 사용 편의성을 우선한다.
- `#0066cc`를 유일한 주요 액센트로 사용한다.

---

## 컬러 토큰

| 이름 | 값 | 용도 |
|------|----|------|
| Primary | `#0066cc` | 모든 주요 인터랙티브 요소, FAB, 선택 상태 |
| Primary Active | `#2997ff` | 하단 탭 활성 상태 |
| Success | `#10B981` | 활성 알람, 권한 수락, 성공 상태 |
| Danger | `#ef4444` | 삭제, 오류, 위험 액션 |
| Canvas | `#ffffff` | 기본 화면 배경 |
| Parchment | `#f5f5f7` | 섹션 배경, 카드 배경, 입력 필드 |
| Border | `#e0e0e0` | 카드, 구분선, 입력 테두리 |
| Dark Tile | `#272729` | 마이페이지 상단 등 다크 섹션 |
| Ink | `#1d1d1f` | 제목, 주요 본문 |
| Muted | `#7a7a7a` | 보조 텍스트, 비활성 탭 |
| Tab Bar | `#000000` | 하단 탭바 배경 |

### 지도 마커

```text
stroke: rgba(0,102,204,0.9)
fill:   rgba(0,102,204,0.15)
```

---

## 타이포그래피

- Compose Material 3 기본 Typography를 기반으로 한다.
- font weight는 `300`, `400`, `600`만 사용한다.
- `500` weight는 사용하지 않는다.
- 화면 제목은 명확하게, 카드/리스트 내부 텍스트는 과하게 크지 않게 유지한다.
- 문자열은 코드에 하드코딩하지 않고 `strings.xml`로 분리한다.

---

## 컴포넌트 규칙

### 버튼

- 주요 버튼은 full rounded shape를 사용한다.
- pressed 상태에는 scale 또는 alpha 피드백을 준다.
- Primary 액션은 `#0066cc`를 사용한다.
- 위험 액션은 `#ef4444`를 사용하되 남용하지 않는다.

### 카드

- radius: `16dp`
- border: `1dp #e0e0e0`
- 카드 안에 카드를 중첩하지 않는다.
- 그림자는 기본적으로 사용하지 않는다.

### 입력 필드

- radius: `12dp`
- background: `#f5f5f7`
- 에러 상태는 `#ef4444`로 표시한다.
- 검색창은 full rounded shape를 사용한다.

### FAB

- size: `56dp`
- background: `#0066cc`
- 지도 홈에서 알람 생성, 현재 위치 이동 같은 핵심 액션에만 사용한다.

### 하단 탭바

- background: `#000000`
- active: `#2997ff`
- inactive: `#7a7a7a`
- 항상 화면 하단에 고정한다.

---

## 화면별 방향

### Home

- 지도와 현재 알람 상태가 첫 화면의 중심이다.
- 목적지 검색, 반경 설정, 알람 생성 액션이 빠르게 이어져야 한다.
- 지도 마커와 반경 원은 Primary 기반 스타일을 사용한다.

### Alarms

- 활성 알람과 비활성/지난 알람을 명확히 구분한다.
- 알람 카드에는 목적지, 반경, 생성자, 활성 상태를 표시한다.
- 삭제나 비활성화 액션은 실수 방지를 고려한다.

### Friends

- 친구 목록, 권한 요청, 수락/거절 상태를 분명히 보여준다.
- 대리 알람 설정은 사용자의 동의 상태를 먼저 확인하게 한다.
- 권한이 없는 상태에서는 요청 액션을 우선 제공한다.

### Profile

- 계정, 알림, 위치 권한, 약관/개인정보 메뉴를 정리한다.
- 다크 섹션이 필요한 경우 `Dark Tile`을 사용한다.

### Ringing

- 알람 중지 액션이 가장 크고 명확해야 한다.
- 커스텀 알람음 재생 중에도 화면을 즉시 이해할 수 있어야 한다.
- 진동, 반복 재생, 알림 채널 동작을 고려한다.

---

## Compose 구현 위치

```text
app/src/main/java/com/wakepoint/app/core/design/
  Color.kt
  Theme.kt
  Type.kt
  Component.kt
```

공통 컴포넌트 예시:

- `WakepointButton`
- `WakepointTextField`
- `WakepointSearchBar`
- `AlarmCard`
- `FriendCard`
- `PermissionStatusChip`
- `WakepointBottomBar`

---

## 금지 사항

- `#0066cc` 외 두 번째 주요 강조색을 만들지 않는다.
- 그라데이션 배경을 사용하지 않는다.
- 장식용 그림자, 과한 blur, 불필요한 카드 중첩을 사용하지 않는다.
- 지도 마커 등 기능적 요소 외에는 그림자를 제한한다.
- Android 권한 안내 화면에서 과도한 마케팅 문구를 사용하지 않는다.

