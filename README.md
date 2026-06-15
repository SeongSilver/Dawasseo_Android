# 다왔어 (Dawasseo)

> 목적지에 가까워지면 자동으로 울리는 위치 기반 알람 앱.
> 본인이 직접 설정하기 어려운 상황에서 가족·친구가 대신 알람을 설정해줄 수 있다.

---

## 프로젝트 소개

**다왔어**는 출발지·도착지를 설정하면 목적지 반경에 진입했을 때 자동으로 알람이 울리는 Android 앱이다.
단순한 위치 알람을 넘어, 동의 기반으로 가족·친구가 타인의 알람을 원격 설정할 수 있는 것이 핵심 차별점이다.

### 핵심 페르소나
1. **술 취한 친구** — 지하철에서 잠든 친구 대신 동행이 집 근처 알람을 설정
2. **가족 케어** — 부모가 어린 자녀의 귀가 알람을, 자녀가 노부모의 이동 알람을 원격 설정

> 대리 알람 설정은 단순 편의가 아니라 **디지털 약자(어린이·노인)를 위한 안전망**으로 포지셔닝된다.

---

## 주요 기능

| 영역 | 기능 |
|------|------|
| 인증 | 이메일 / Google / 카카오 로그인 |
| 알람 | 지도 목적지 선택, 반경 설정(300m~50km), 백그라운드 위치 추적, 반경 진입 시 트리거 |
| 알람음 | 기본 알람음 / 녹음 커스텀 알람음 (가족 목소리 등) |
| 소셜 | 친구 추가(이메일·카카오 초대·QR), 대리 알람 권한 요청/수락, 친구 대신 알람 설정 |
| 실시간 | Supabase Realtime 알람 동기화 |
| 알림 | FCM 푸시 (대리 알람 설정·권한 요청 알림) |

---

## 개발 환경

| 항목 | 기준 |
|------|------|
| 플랫폼 | Android Native (단일 타겟) |
| IDE | Android Studio |
| 언어 | Kotlin (strict null-safety) |
| UI | Jetpack Compose + Material 3 |
| 아키텍처 | MVVM + Repository (ViewModel + StateFlow) |
| 비동기 | Kotlin Coroutines + Flow |
| DI | Hilt |
| 로컬 저장소 | DataStore (설정/세션) + Room (캐시) |
| 백엔드 | Supabase (Auth·DB·Realtime·Storage) |
| Push | Firebase Cloud Messaging |
| 위치 | Fused Location Provider + Foreground Service / WorkManager |
| 지도·검색 | Google Maps SDK + Kakao Local API |
| 카카오 | Kakao Android SDK (로그인·공유) |
| 녹음·재생 | MediaRecorder + Media3/ExoPlayer |
| 네비게이션 | Navigation Compose (typed route) |
| 빌드 | Gradle Kotlin DSL |

> iOS / Web 대응은 현재 범위에서 제외한다.

---

## 디렉토리 구조

```
app/
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    java/com/wakepoint/app/
      MainActivity.kt
      WakepointApplication.kt
      core/
        data/                 # DataStore, Room, repository helpers
        design/               # Compose theme, colors, typography, components
        location/             # 거리 계산, 위치 정책
        notification/         # 알람 채널, FCM 헬퍼
        supabase/             # Supabase 클라이언트
      feature/
        auth/ home/ alarms/ friends/ profile/ invite/
      data/
        alarm/ friend/ user/ recording/
      domain/
        model/ usecase/
      service/
        LocationTrackingService.kt
        AlarmWorker.kt
        WakepointFirebaseMessagingService.kt
      navigation/
        WakepointNavGraph.kt
supabase/migrations/
docs/
  SETUP.md · SCHEMA.md · API.md · DESIGN.md · IMPLEMENTATION_NOTES.md
```

---

## 실행 방법

### 1. 사전 준비
- Android Studio 최신 버전 설치
- JDK 17 이상
- `local.properties`에 API 키 설정 (→ docs/SETUP.md 참고)
- `google-services.json` 배치 (Firebase 콘솔에서 발급)

### 2. 빌드 & 실행

```powershell
.\gradlew assembleDebug          # Debug APK 빌드
.\gradlew installDebug           # 연결된 기기에 설치
.\gradlew bundleRelease          # Release AAB 생성
.\gradlew test                   # Unit test
.\gradlew connectedAndroidTest   # Instrumentation test
.\gradlew lint                   # Android lint
```

> Android Studio에서 Run Configuration을 `app` 모듈로 두고 **실기기 우선** 검증한다.
> 백그라운드 위치 추적·알람·FCM은 에뮬레이터에서 정확히 동작하지 않을 수 있다.

---

## 개발 현황

✅ **기획 완료** — 위치 알람, 대리 설정, 권한 요청, 커스텀 알람음, 법적 문서, Supabase 스키마 확정

🔄 **진행 예정**
1. Android Native 프로젝트 재구성 (Kotlin + Compose)
2. Supabase 연동 이관 (Auth·DB·Realtime·Storage)
3. 지도/위치/알람 핵심 플로우 구현
4. 친구/권한/FCM 구현
5. 커스텀 알람음 구현
6. 실기기 검증 (발열·배터리·권한 정책)
7. 위치정보 사업자 신고 (출시 전 법적 의무)
8. 스토어 배포 (production AAB)

---

## 참고 문서
- [SETUP.md](./docs/SETUP.md) — 개발 환경 세팅 및 키 설정
- [SCHEMA.md](./docs/SCHEMA.md) — Supabase DB 스키마 및 RLS
- [API.md](./docs/API.md) — API 연동 흐름
- [DESIGN.md](./docs/DESIGN.md) — 디자인 시스템
- [IMPLEMENTATION_NOTES.md](./docs/IMPLEMENTATION_NOTES.md) — 구현 전 확인 사항

---

## 주의사항
- 백그라운드 위치 추적은 발열·배터리 이슈가 크므로 **Balanced Priority** + 추적 중단 조건을 반드시 지킨다.
- 위치 권한은 단계적으로 요청한다: foreground → background.
- Android 13+ 알림 권한 `POST_NOTIFICATIONS`를 별도 요청한다.
- Supabase **service role key는 절대 앱에 포함하지 않는다.** (anon key만)
- 위치정보 사업자 신고 및 개인정보 처리 문구는 출시 전 법률 기준으로 재확인한다.

