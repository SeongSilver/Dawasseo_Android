# TASKS_NOW (2026-07-30 기준)

근거: `rg TODO/FIXME`(소스 내 0건), `git status`(미커밋 13파일), `docs/work-log/2026-06-16-*`, `docs/CHECKLISTS.md`, `app/build.gradle.kts` 의존성 스캔.

## 즉시 착수 가능

1. [app/src/main/java/com/wakepoint/app/core/location/AlarmRetrigger.kt 외 미커밋 13파일] 알람 재발동(repeating) 작업 검토 후 커밋 마무리 | 선행조건 없음 | S
2. [app/src/main/java/com/wakepoint/app/feature/friends/FriendsScreen.kt:655] 하드코딩 placeholder "이메일 또는 닉네임"을 strings.xml 리소스로 추출 | 선행조건 없음 | S
3. [app/src/main/java/com/wakepoint/app/feature/auth/AuthScreen.kt:310] 뒤로가기 IconButton contentDescription=null → 접근성/테스트용 라벨 부여 | 선행조건 없음 | S
4. [app/src/main/res/values/strings.xml:23-31] MVP에서 숨긴 전화번호 인증용 미사용 문자열(signup_id_hint, signup_birth_hint, signup_phone_hint, signup_code_hint, signup_verify) 정리 | 선행조건 없음 | S
5. [app/src/main 전체] testTag 사용 0건 — 로그인 버튼·탭바·알람 카드 등 핵심 UI에 Modifier.testTag 부여로 E2E 셀렉터 안정화 | 선행조건 없음 | S
6. [app/src/main/java/com/wakepoint/app/data/alarm/remote/AlarmDto.kt:7-19] soundEnabled/vibrate/isFavorite가 DTO에 없어 서버 왕복 시 유실 — 로컬 전용 정책인지 주석/SCHEMA.md에 명시(또는 컬럼 확장 결정) | 선행조건 없음(문서화 기준) | M
7. [app/src/main/java/com/wakepoint/app/core/network/NetworkRetry.kt:23-35] 지수 백오프에 상한(maxDelayMs)·지터 없음 — 캡 추가 | 선행조건 없음 | S
8. [app/build.gradle.kts:93] 단위테스트 스택 보강: kotlinx-coroutines-test, mockk(또는 mockito-kotlin), turbine, org.json:json 추가 — ViewModel/Repository/DTO-JSON 테스트의 선행조건 | 선행조건 없음 | S
9. [docs/CHECKLISTS.md:17-18] 빌드 명령이 Windows 형식(`.\gradlew`) — macOS(`./gradlew`) 병기로 갱신 | 선행조건 없음 | S
10. [docs/work-log/2026-06-16-current-status.md] 이후 커밋(재발동 수정, 다중선택 삭제, 폰트 교체 등) 반영해 상태 문서 최신화 | 선행조건 없음 | S

## Blocked (선행조건 있음)

11. [docs/work-log/2026-06-16-alarm-next-steps.md:33] Android 13+ 알림 권한 허용/거부 UX 수동 검증 | 선행: 실기기/에뮬레이터 | M
12. [app/src/main/java/com/wakepoint/app/service/LocationTrackingService.kt] mock location으로 반경 진입 트리거·재발동(REARM→FIRE) 검증 | 선행: 에뮬레이터 | M
13. [docs/work-log/2026-06-16-alarm-next-steps.md:36] Supabase live DB migration push | 선행: Supabase CLI 로그인+네트워크 | M
14. [app/src/main/java/com/wakepoint/app/MainActivity.kt:39-52] Kakao OAuth 콜백(com.wakepoint.app://auth-callback) 플로우 검증 | 선행: 카카오 계정/외부 브라우저 | M
15. [app/src/androidTest] 로그인 이후 화면(알람 CRUD, 지도)의 E2E — Hilt 테스트 더블(FakeAuthRepository) 또는 세션 픽스처 주입 필요 | 선행: hilt-android-testing 의존성+CustomTestRunner | L
16. [app/src/test] AlarmsViewModel·DefaultAlarmRepository 단위테스트 | 선행: 8번(coroutines-test/mockk) | M
17. [app/src/main/java/com/wakepoint/app/data/alarm/remote/AlarmDto.kt:38-51] alarmDtoFromJson 파싱 단위테스트 — org.json이 로컬 유닛테스트에서 스텁이라 현재 불가 | 선행: 8번(org.json:json testImplementation) | S

## E2E 선행작업 (의존성 추가 필요 시에만)

- 현재 gradle에 이미 있음: compose-bom ui-test-junit4, androidx.test.ext:junit:1.2.1, espresso-core:3.6.1, ui-test-manifest → 작성한 E2E 3개는 추가 의존성 없이 컴파일 가능.
- 권장 추가(명시 의존성으로 고정): `androidTestImplementation("androidx.test:runner:1.6.2")`, `androidTestImplementation("androidx.test:rules:1.6.1")`
- 로그인 이후 플로우 E2E 확장 시(15번): `androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")` + kspAndroidTest hilt-compiler

## 이번 작업에서 추가된 테스트

- 단위: NetworkRetryTest, AlarmEntityTest, AlarmDtoTest, AlarmsUiStateTest (app/src/test)
- E2E: AppLaunchE2ETest, SignUpNavigationE2ETest, LoginFormE2ETest (app/src/androidTest)
