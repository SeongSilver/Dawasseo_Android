# Checklists

다왔어 Android 작업 전후에 반복해서 쓰는 검증 체크리스트다.
작업 단위가 작아도 해당 영역 체크리스트를 최소 1회 통과시키는 것을 기본으로 한다.

## 작업 시작

- `AGENTS.md`와 `docs/work-log/2026-06-16-current-status.md`를 읽고 현재 범위를 확인했다.
- `git status --short`로 기존 변경 사항을 확인했다.
- 내가 만들 변경과 무관한 사용자 변경을 되돌리지 않는다.
- 민감 키를 코드, 문서, 로그에 그대로 남기지 않는다.
- 새 환경 변수는 `local.properties` 또는 CI secret 기준으로만 연결한다.

## 빌드 검증

- 코드 변경 후 `.\gradlew assembleDebug`를 실행했다.
- 로직 변경 후 `.\gradlew test`를 실행했다.
- 네트워크 제한으로 실패한 Gradle 명령은 escalated 권한으로 재시도했다.
- docs-only 변경은 빌드를 생략할 수 있지만, 최종 응답에 생략 사유를 남긴다.

## 인증

- 이메일 회원가입 성공 시 Supabase 세션 또는 이메일 확인 안내가 정상적으로 표시된다.
- 로그인 성공 후 홈 화면으로 진입한다.
- 로그아웃 후 인증 화면으로 돌아온다.
- 서버 오류 메시지는 사용자에게 이해 가능한 문구로 변환된다.
- 전화번호 인증 UI는 MVP에서 필수 흐름이 아니다.

## 지도와 위치

- `GOOGLE_MAPS_API_KEY`가 `local.properties`에 있고 BuildConfig로 주입된다.
- 지도 화면이 GoogleMap으로 렌더링된다.
- foreground 위치 권한 요청이 동작한다.
- 권한 허용 후 현재 위치를 가져오면 지도 카메라가 내 위치로 이동한다.
- 위치를 가져오지 못하면 fallback 위치로 동작한다.
- 지도 탭 또는 검색 결과 선택으로 목적지 좌표가 state에 저장된다.

## 알람 생성과 목록

- 홈 바텀시트의 좌표, 반경, 별칭이 create action에 전달된다.
- `AlarmRepository.saveAlarm()`이 Supabase insert를 먼저 시도한다.
- insert 성공 후 Room cache가 갱신된다.
- 알람 목록 화면이 목업이 아니라 `observeAlarms()` 데이터를 표시한다.
- 활성/지난 알람이 분리되어 보인다.
- 비활성화와 삭제 action이 Supabase와 Room에 반영된다.

## 위치 추적과 트리거

- 활성 알람이 1개 이상이면 위치 추적이 시작된다.
- 활성 알람이 0개면 위치 추적이 중단된다.
- 위치 업데이트 priority는 Balanced 기준이다.
- 반경 진입 판단은 `DistanceCalculator.calculateDistance()`를 사용한다.
- 트리거 후 알림이 표시된다.
- 트리거된 알람은 `is_active=false`, `triggered_at` 값으로 갱신된다.

## Kakao Local 검색

- `KAKAO_REST_API_KEY`가 `local.properties`에 있고 BuildConfig로 주입된다.
- 검색창 클릭 시 장소 검색 UI가 열린다.
- 키워드 검색 결과가 표시된다.
- 검색 결과 선택 시 지도 카메라가 이동한다.
- 선택 좌표와 주소/장소명이 알람 생성 state에 반영된다.

## 출시 전 보안

- Supabase service role key가 앱에 포함되지 않았다.
- Google Maps API key는 package name + SHA-1 제한이 걸려 있다.
- Kakao Android key hash가 등록되어 있다.
- Android 13+ `POST_NOTIFICATIONS` 권한 UX가 있다.
- Android 10+ background location 권한은 foreground와 분리해서 요청한다.
- 위치정보 관련 법적 문구와 신고 필요성을 재확인했다.

