# ARCH — 레이어 구조와 iOS 대응

## 패키지 레이어 (com.wakepoint.app)

| Android 레이어 | 내용 | iOS 대응 (1줄) |
|---|---|---|
| `domain/model` | 순수 데이터 클래스(Alarm, Friend, UserProfile, AlarmSound, AlarmPermission) | Swift struct (Codable/Sendable) 그대로 이식 |
| `data/*` (alarm/friend/auth/sound/location) | Repository 인터페이스 + Default 구현, local(Room DAO/Entity) + remote(DTO) | protocol + 구현체; SwiftData/GRDB 로컬 + URLSession 원격 |
| `core/data/local` WakepointDatabase(Room v8) | alarms, friends 테이블 | SwiftData ModelContainer (또는 Core Data) |
| `core/data/preferences` UserPreferencesDataStore | 토큰/기본사운드/보류 큐 | UserDefaults + Keychain(토큰), @AppStorage |
| `core/di` Hilt(AppModule, @Singleton @Provides) | 생성자 주입 | 경량 DI 컨테이너(Environment/Factory) 또는 swift-dependencies |
| `core/location` | DistanceCalculator(하버사인), RadiusOptions, AlarmRetrigger(순수 함수), LocationTrackingController | 순수 Swift 함수로 그대로 이식 + CLLocationManager 래퍼 |
| `core/notification` | 채널 2개(도착 알람/추적 상태), 풀스크린 인텐트, AlarmStopReceiver | UNUserNotificationCenter(카테고리+액션 "알람 끄기"), time-sensitive/critical alert |
| `core/audio` | AlarmAudioPlayer(루프, 알람 스트림), SoundPreviewPlayer, MediaRecorder 녹음 | AVAudioPlayer(.playback, loop) + AVAudioRecorder(.m4a), AVAudioSession |
| `core/push` PushTokenSyncManager + FCM 서비스 | 토큰 업로드, 친구 알람 수신 | APNs(또는 FCM iOS) + UNNotificationServiceExtension |
| `core/permission` StartupPermissions(Compose 게이트) | 위치/알림/오버레이 권한 요청 흐름 | CLLocationManager(WhenInUse→Always 2단계) + UNAuthorization 요청 뷰 |
| `core/sync` AppSyncViewModel | 포그라운드 복귀 시 서버 동기화 | scenePhase(.active) 감지 후 sync |
| `feature/*` (auth/home/alarms/friends/profile/alarmalert) | Composable 화면 + @HiltViewModel | SwiftUI View + @Observable(또는 ObservableObject) ViewModel |
| `navigation` WakepointNavGraph(navigation-compose, 플로팅 탭 바) | 라우트 문자열 기반 | SwiftUI TabView(커스텀 플로팅 탭 바) + NavigationStack/sheet |
| `service/LocationTrackingService` | location 포그라운드 서비스 + FusedLocationProvider 콜백 | CLLocationManager background updates + CLCircularRegion 모니터링(최대 20개) |
| `service/AlarmAlertService` | shortService — 알람 사운드/진동 재생 | 포그라운드 상태에선 풀스크린 뷰; 백그라운드는 알림 사운드(30초 제한) — 설계 결정 필요 |
| `service/AlarmWorker` + WorkManager(주기 30분) | 주기 동기화 | BGAppRefreshTask/BGProcessingTask (30분 보장 불가, 기회적 실행) |
| `core/boot/BootCompletedReceiver` | 재부팅 후 추적 재개 | iOS 상응 없음 — region monitoring이 재부팅 후에도 앱 재기동시켜 주는 특성 활용 |

## 상태 관리 패턴

- ViewModel마다 단일 `XxxUiState` data class + `StateFlow`(Room Flow와 combine), UI 액션은 ViewModel 메서드.
- 파생 컬렉션(favorite/active/inactive 등)은 UiState의 computed property.
- 에러는 `errorMessage: String?` + consume/clear 함수(1회성 소비), UserFacingError로 문구 통일.
- iOS 대응: `@Observable` UiState 클래스(또는 struct + @Published), Combine/AsyncSequence로 DB observe, 에러도 동일 패턴(alertItem).

## 핵심 흐름

1. 알람 저장: HomeViewModel.saveAlarm → Room upsert(pendingSync=true) → 서버 POST upsert 성공 시 pendingSync=false → LocationTrackingController.syncTracking(activeCount).
2. 위치 업데이트: LocationTrackingService → 각 활성 알람에 대해 거리+정확도버퍼 계산 → decideAlarmTriggerAction → FIRE 시 AlarmAlertService(사운드/진동) + 풀스크린 알림 + (비반복이면) isActive=false.
3. 동기화: 주기 Worker/포그라운드 복귀 → pendingDeletions DELETE → pendingSync POST → GET 후 로컬 정리(deleteSyncedAlarmsNotIn).
4. 친구 알람: FCM 수신 → pendingFriendAlarms 큐 → DB 반영 → 추적 갱신.

## 외부 의존성 → iOS 대체

| Android | iOS |
|---|---|
| Jetpack Compose + Material3 | SwiftUI |
| Room 2.6 | SwiftData / Core Data / GRDB |
| Hilt 2.52 | 수동 DI 또는 swift-dependencies |
| DataStore Preferences | UserDefaults + Keychain |
| WorkManager 2.10 | BGTaskScheduler |
| play-services-location (Fused) | CoreLocation |
| play-services-maps + maps-compose (Google Maps) | MapKit (또는 Google Maps iOS SDK) |
| firebase-messaging (FCM) | APNs + UserNotifications (FCM iOS SDK 선택) |
| HttpURLConnection + JSONObject | URLSession + Codable |
| navigation-compose | NavigationStack + TabView |
| StateFlow/coroutines | async/await + Observation/Combine |
