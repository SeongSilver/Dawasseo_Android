# 디자인 적용 진행 현황

기준: `design-handoff/DESIGN.md`. 화면 하나를 끝내고 `./gradlew assembleDebug` 가 성공하면 해당 칸을 `[x]` 로 바꾼다.

완료: 30 / 88

## 공통

- [x] `core/design/Theme.kt` — 색 역할(라이트 · 다크), 타입, 모양
- [x] `core/design/Component.kt`, `RadiusSelector.kt` — 공통 컴포넌트 스타일
- [x] 하단 탭바, 바텀시트 컨테이너
- [ ] 시스템 알림 small icon, 액센트 색
- [x] `docs/DESIGN.md`, `AGENTS.md` 갱신

## 화면

| 파일 이름 | 화면 · 상태 | 명세 절 | 라이트 | 다크 |
|---|---|---|---|---|
| `Login-default` | 로그인 · 기본 | 2 | [ ] | [ ] |
| `Login-submitting` | 로그인 · 제출 중 | 2 | [ ] | [ ] |
| `Login-error` | 로그인 · 오류 | 2 | [ ] | [ ] |
| `Login-info` | 로그인 · 안내 | 2 | [ ] | [ ] |
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
| `Splash` | 스플래시 | 1 | [ ] | [ ] |
| `Home` | 홈 · 위치 권한 있음 | 4.1 | [x] | [x] |
| `Home-noperm` | 홈 · 위치 권한 없음 (기본 위치) | 4.1 | [x] | [x] |
| `Place-idle` | 장소 검색 시트 · 입력 전 | 4.2 | [x] | [x] |
| `Place-searching` | 장소 검색 시트 · 검색 중 | 4.2 | [x] | [x] |
| `Place-results` | 장소 검색 시트 · 결과 있음 | 4.2 | [x] | [x] |
| `Place-none` | 장소 검색 시트 · 결과 없음 | 4.2 | [x] | [x] |
| `Place-error` | 장소 검색 시트 · 오류 | 4.2 | [x] | [x] |
| `Signup-default` | 회원가입 · 기본 | 3 | [ ] | [ ] |
| `Signup-submitting` | 회원가입 · 제출 중 | 3 | [ ] | [ ] |
| `Signup-error` | 회원가입 · 오류 (인라인) | 3 | [ ] | [ ] |
| `Signup-info` | 회원가입 · 안내 | 3 | [ ] | [ ] |
| `FriendSearch-idle` | 친구 검색 시트 · 입력 전 | 7.4 | [ ] | [ ] |
| `FriendSearch-searching` | 친구 검색 시트 · 검색 중 | 7.4 | [ ] | [ ] |
| `FriendSearch-results` | 친구 검색 시트 · 결과 (선택됨) | 7.4 | [ ] | [ ] |
| `FriendSearch-none` | 친구 검색 시트 · 결과 없음 | 7.4 | [ ] | [ ] |
| `FriendSearch-error` | 친구 검색 시트 · 오류 | 7.4 | [ ] | [ ] |
| `Sounds` | 알람음 목록 | 6 | [ ] | [ ] |
| `Sounds-recording` | 알람음 · 녹음 시트 | 6 | [ ] | [ ] |
| `Profile` | 프로필 | 8 | [ ] | [ ] |
| `Notifications` | 시스템 알림 2종 | 10 | [ ] | [ ] |

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
