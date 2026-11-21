# 디자인 적용 진행 현황

기준: `design-handoff/DESIGN.md`. 화면 하나를 끝내고 `./gradlew assembleDebug` 가 성공하면 해당 칸을 `[x]` 로 바꾼다.

완료: 52 / 88

## 공통

- [x] `core/design/Theme.kt` — 색 역할(라이트 · 다크), 타입, 모양
- [x] `core/design/Component.kt`, `RadiusSelector.kt` — 공통 컴포넌트 스타일
- [x] 하단 탭바, 바텀시트 컨테이너
- [x] 시스템 알림 small icon, 액센트 색
- [x] `docs/DESIGN.md`, `AGENTS.md` 갱신

## 화면

| 파일 이름 | 화면 · 상태 | 명세 절 | 라이트 | 다크 |
|---|---|---|---|---|
| `Login-default` | 로그인 · 기본 | 2 | [x] | [x] |
| `Login-submitting` | 로그인 · 제출 중 | 2 | [x] | [x] |
| `Login-error` | 로그인 · 오류 | 2 | [x] | [x] |
| `Login-info` | 로그인 · 안내 | 2 | [x] | [x] |
| `Alarms-list` | 알람 목록 · 기본 (활성/비활성) | 5 | [ ] | [ ] |
| `Alarms-open` | 알람 목록 · A안 카드 펼침 | 5 | [ ] | [ ] |
| `Alarms-edit` | 알람 수정 · B안 별도 화면 | 5 | [ ] | [ ] |
| `Alarms-empty` | 알람 목록 · 빈 상태 | 5 | [ ] | [ ] |
| `Alarms-updating` | 알람 목록 · 갱신 중 + 오류 | 5 | [ ] | [ ] |
| `Alarms-delete` | 알람 목록 · 삭제 확인 | 5 | [ ] | [ ] |
| `Sheet-default` | 알람 설정 시트 · 기본 | 4.3 | [x] | [x] |
| `Sheet-noperm` | 알람 설정 시트 · 알림 권한 없음 | 4.3 | [x] | [x] |
| `Sheet-saving` | 알람 설정 시트 · 저장 중 | 4.3 | [x] | [x] |
| `Sheet-error` | 알람 설정 시트 · 오류 | 4.3 | [x] | [x] |
| `Sheet-friend` | 알람 보내기 시트 · 친구 화면 | 4.3 | [x] | [x] |
| `Friends-requests` | 친구 · 요청 탭 | 7 | [ ] | [ ] |
| `Friends-list` | 친구 · 친구 탭 (카드 펼침) | 7 | [ ] | [ ] |
| `Friends-empty` | 친구 · 빈 상태 | 7 | [ ] | [ ] |
| `Friends-error` | 친구 · 오류 | 7 | [ ] | [ ] |
| `Friends-block` | 친구 · 차단 확인 | 7 | [ ] | [ ] |
| `TabBar` | 하단 탭바 · 선택 상태 4종 | 11 | [x] | [x] |
| `Ringing` | 알람 울림 · 별칭 + 주소 | 9 | [x] | [x] |
| `Ringing-alias` | 알람 울림 · 별칭만 | 9 | [x] | [x] |
| `Splash` | 스플래시 | 1 | [x] | [x] |
| `Home` | 홈 · 위치 권한 있음 | 4.1 | [x] | [x] |
| `Home-noperm` | 홈 · 위치 권한 없음 (기본 위치) | 4.1 | [x] | [x] |
| `Place-idle` | 장소 검색 시트 · 입력 전 | 4.2 | [x] | [x] |
| `Place-searching` | 장소 검색 시트 · 검색 중 | 4.2 | [x] | [x] |
| `Place-results` | 장소 검색 시트 · 결과 있음 | 4.2 | [x] | [x] |
| `Place-none` | 장소 검색 시트 · 결과 없음 | 4.2 | [x] | [x] |
| `Place-error` | 장소 검색 시트 · 오류 | 4.2 | [x] | [x] |
| `Signup-default` | 회원가입 · 기본 | 3 | [x] | [x] |
| `Signup-submitting` | 회원가입 · 제출 중 | 3 | [x] | [x] |
| `Signup-error` | 회원가입 · 오류 (인라인) | 3 | [x] | [x] |
| `Signup-info` | 회원가입 · 안내 | 3 | [x] | [x] |
| `FriendSearch-idle` | 친구 검색 시트 · 입력 전 | 7.4 | [ ] | [ ] |
| `FriendSearch-searching` | 친구 검색 시트 · 검색 중 | 7.4 | [ ] | [ ] |
| `FriendSearch-results` | 친구 검색 시트 · 결과 (선택됨) | 7.4 | [ ] | [ ] |
| `FriendSearch-none` | 친구 검색 시트 · 결과 없음 | 7.4 | [ ] | [ ] |
| `FriendSearch-error` | 친구 검색 시트 · 오류 | 7.4 | [ ] | [ ] |
| `Sounds` | 알람음 목록 | 6 | [ ] | [ ] |
| `Sounds-recording` | 알람음 · 녹음 시트 | 6 | [ ] | [ ] |
| `Profile` | 프로필 | 8 | [x] | [x] |
| `Notifications` | 시스템 알림 2종 | 10 | [x] | [x] |

## 막힌 화면 (blocked)

| 화면 | 이유 |
|---|---|

## 디자인과 다르게 구현한 곳

| 화면 | 다른 점 | 이유 |
|---|---|---|
| 전체 | 글꼴이 Pretendard 가 아니라 시스템 폰트(`FontFamily.Default`) | `res/font` 에 Pretendard 리소스가 없다. 스펙에 없는 에셋을 임의로 추가하지 않았다. 번들된 Spoqa Han Sans Neo 도 쓰지 않는다(디자인에 없는 글꼴). 폰트 파일을 넣으면 `core/design/Type.kt` 의 `WakepointFontFamily` 한 곳만 바꾸면 된다. |
| 알람 울림 | 문구가 "도착했습니다 / 알람 중지" 가 아니라 "목적지에 도착했어요 / 알람 끄기" | 디자인의 자리 표시 문구로 기존 `strings.xml` 값을 덮어쓰지 않는다. |
| 알람 울림 | 펄스 원의 모프 애니메이션 없음 | 명세에 없는 동작을 추가하지 않는다. 정지 상태의 3겹 원만 그렸다. |
| 전체 | 즐겨찾기 별(구 `WakepointStar` 앰버)이 `brand` 로 바뀜 | 새 팔레트에 별 전용 역할이 없고, 즐겨찾기는 디자인 44개 화면에 없다. |
| 로그인 · 회원가입 | 오류·안내를 팝업(`WakepointMessageDialog`) 대신 인라인 메시지 영역으로 표시. `onDismissMessage` 파라미터는 시그니처 유지용으로만 남음 | 디자인이 인라인 메시지. 내비게이션 호출부 시그니처는 바꾸지 않는다 |
| 회원가입 · 오류 | 필드 오류 외에 가입 버튼 위 인라인 오류 메시지도 지원 | `AuthViewModel.errorMessage` 는 필드 단위가 아닌 단일 문자열(네트워크 실패 등)이라 표시할 자리가 필요 |
| 회원가입 · 비밀번호 보조 문구 | "8자 이상" 대신 "6자 이상 입력하세요" | `AuthViewModel.signUp()` 의 실제 검증이 6자. 비즈니스 로직을 수정하지 않는다 |
| 스플래시 | 워드마크가 32/40·500 이 아니라 `headlineMedium`(28/36), 심볼↔워드마크 간격 12 → 8 | `WakepointLogo` 가 워드마크 스타일·간격을 노출하지 않고, 공통 컴포넌트 시그니처를 깨지 않는다 |
| 회원가입 상단바 | 좌측 패딩 4·간격 4 가 아니라 `WakepointPageHeader` 기본값(좌우 16·간격 8) | 공통 컴포넌트를 그대로 쓰고 다른 화면과 일관되게 유지 |
| 로그인 | 카카오 버튼이 높이 54·라운드 12 점선 자리표시가 아니라 공식 에셋 183×45 그대로 | 카카오 로그인 버튼은 공식 이미지 에셋을 그대로 쓴다 |
| 로그인 | 약관·저작권 푸터가 스크롤 영역 안이 아니라 밖에 고정 | 스크롤 Column 안에서는 `Modifier.weight` 가 동작하지 않는다. 기본 높이에서 시각적으로 동일 |
| 로그인 · 회원가입 | 화면 안에서 `statusBarsPadding()` 을 다시 넣지 않음 | `WakepointNavGraph` 의 `NavHost` 가 이미 상태바 인셋을 적용한다(중복 방지) |
| 프로필 | 메뉴 카드가 `설정`·`고객센터` 2행이 아니라 6행(내 장소·막차 알람·알림 설정·위치 권한 관리·알람 사운드 관리·고객센터) | 실제로 동작하는 기존 진입점이라 지우면 기능이 사라진다. 모양만 디자인에 맞췄다 |
| 프로필 | 디자인에 없는 통계 행(깨워드린 횟수·활성 알람·지켜봐 주는 친구)을 유지 | ViewModel 이 이미 제공하는 기존 기능. 카드 대신 구분선만 있는 평면 행으로 톤을 맞췄다 |
| 프로필 | 묶음 행 높이 64 → 56, 아바타 이니셜 글자 24/32 → 16/24 | `SettingsRow`·`WakepointAvatar` 가 해당 값을 노출하지 않고, 공통 컴포넌트 시그니처를 깨지 않는다 |
| 프로필 | 프로필 행 오른쪽에 `chevron_forward` 유지(디자인엔 없음), 하단 버전 문구 유지 | 닉네임 변경 다이얼로그 진입점이라 탭 가능 표시가 필요하고, 기존 정보를 지우지 않는다 |
| 프로필 · 닉네임 변경 다이얼로그 | 핸드오프에 해당 화면이 없음 | DESIGN.md 5절 다이얼로그 규칙(모서리 28·`surfaceElevated`·headlineSmall·버튼 2개)으로 구현 |
| 시스템 알림 | 문구가 "도착 알람 대기 중 / 활성 알람 2개 / 도착했습니다" 가 아니라 기존 `strings.xml` 값 | 디자인의 자리 표시 문구로 기존 문자열을 덮어쓰지 않는다 |
| 시스템 알림 | 시스템 알림 템플릿 자체는 디자인대로 그릴 수 없음 — small icon(`ic_notification`) + `setColor(R.color.brand)` 만 적용 | OS 가 그리는 영역이라 앱이 레이아웃을 바꿀 수 없다 |
