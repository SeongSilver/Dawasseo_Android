# Verification Matrix Harness

작업별로 어느 검증을 실행할지 정하는 하네스다.
모든 작업에 모든 검증을 붙이지 말고 변경 위험에 맞춰 선택한다.

## 기본 원칙

- Kotlin 코드 변경: `.\gradlew assembleDebug`
- Repository, mapper, 계산 로직 변경: `.\gradlew test`
- Android permission, 지도, notification 변경: 수동 emulator 또는 실기기 검증
- docs-only 변경: 빌드 생략 가능, 최종 응답에 생략 사유 명시
- dependency 변경: `.\gradlew assembleDebug` 필수

## 작업별 최소 검증

| 작업 | 자동 검증 | 수동 검증 |
| --- | --- | --- |
| 문서만 변경 | 생략 가능 | 파일 링크 확인 |
| UI copy/string | `assembleDebug` | 화면 overflow 확인 |
| Compose 화면 변경 | `assembleDebug` | 해당 화면 진입 |
| Auth flow | `assembleDebug`, `test` | 회원가입/로그인/로그아웃 |
| Supabase Repository | `assembleDebug`, `test` | 실DB insert/update/delete |
| Room DAO/Entity | `assembleDebug`, `test` | 목록 반영 |
| Google Maps | `assembleDebug` | 지도 렌더링, marker, camera |
| 위치 권한 | `assembleDebug` | 권한 허용/거부/재진입 |
| 위치 추적 service | `assembleDebug`, `test` | foreground notification, mock location |
| 알람 트리거 | `assembleDebug`, `test` | 반경 진입, notification, inactive update |
| Kakao Local | `assembleDebug` | 검색 성공/empty/error |
| Notification 권한 | `assembleDebug` | Android 13+ 허용/거부 |
| Gradle dependency | `assembleDebug` | 앱 실행 |

## 수동 검증 기록 형식

작업 로그나 최종 응답에 아래 형식으로 짧게 남긴다.

```text
검증:
- assembleDebug: 성공
- test: 성공
- 수동: Android 13 emulator에서 알림 권한 허용 후 arrival notification 표시 확인
```

검증하지 못한 경우에는 이유를 남긴다.

```text
검증:
- 수동 실기기 위치 이동 테스트: 미실행, 연결된 실기기 없음
```

## Escalation 기준

아래 오류가 나오면 같은 Gradle 명령을 escalated 권한으로 재시도한다.

- `SocketException`
- `Permission denied: getsockopt`
- dependency metadata 다운로드 실패
- Gradle wrapper distribution 다운로드 실패

