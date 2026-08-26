# MANIFEST — AndroidManifest.xml → iOS 대응

## 권한 매핑

| Android 권한 | 용도 | iOS 대응 |
|---|---|---|
| ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION | 목적지 거리 계산 | `NSLocationWhenInUseUsageDescription` |
| ACCESS_BACKGROUND_LOCATION | 백그라운드 추적 | `NSLocationAlwaysAndWhenInUseUsageDescription` + Background Modes: Location updates |
| FOREGROUND_SERVICE(_LOCATION) | 추적 포그라운드 서비스 | 해당 없음 — allowsBackgroundLocationUpdates=true, showsBackgroundLocationIndicator |
| POST_NOTIFICATIONS | 도착 알람/추적 알림 | UNUserNotificationCenter.requestAuthorization(.alert .sound .badge) |
| USE_FULL_SCREEN_INTENT / SYSTEM_ALERT_WINDOW | 잠금화면 위 풀스크린 알람 | iOS 상응 없음 — Time-Sensitive Interruption Level(+ Critical Alerts entitlement은 Apple 특별 승인 필요) |
| RECORD_AUDIO | 알람음 녹음 | `NSMicrophoneUsageDescription` |
| READ_MEDIA_AUDIO / READ_EXTERNAL_STORAGE(≤32) | 오디오 파일 가져오기 | UIDocumentPicker(보안 스코프 URL) — 권한 키 불필요. Apple Music 라이브러리 접근 시엔 `NSAppleMusicUsageDescription` |
| INTERNET | 네트워크 | 기본 허용(ATS 준수, 모든 엔드포인트 https) |
| VIBRATE | 알람 진동 | 기본 허용 — CoreHaptics/AudioServicesPlaySystemSound |
| RECEIVE_BOOT_COMPLETED | 재부팅 후 추적 재개 | 상응 없음 — Region Monitoring은 재부팅 후에도 시스템이 앱을 깨움(대체 설계) |

## Info.plist 필수 키 (초안)

```
NSLocationWhenInUseUsageDescription = "목적지까지 남은 거리를 확인해 도착 알람을 울리기 위해 위치가 필요해요."
NSLocationAlwaysAndWhenInUseUsageDescription = "앱을 닫아도 도착 알람이 울리도록 백그라운드 위치 접근이 필요해요."
NSMicrophoneUsageDescription = "나만의 알람음을 녹음하기 위해 마이크가 필요해요."
UIBackgroundModes = [ location, remote-notification, audio* ]   (* 알람 사운드 백그라운드 재생 채택 시)
CFBundleURLTypes = [ { CFBundleURLSchemes: [ com.wakepoint.app ] } ]   ← auth-callback 딥링크
LSApplicationQueriesSchemes = [ kakaotalk, kakaokompassauth ]   ← 카카오톡 공유/로그인
UIAppFonts = [ SpoqaHanSansNeo-*.otf 5종 ]
```

## Deep Link

- Android: `com.wakepoint.app://auth-callback` (Kakao/Supabase OAuth 콜백, MainActivity VIEW intent-filter)
- iOS: 동일 커스텀 URL Scheme 등록 + `onOpenURL`에서 completeOAuthSignIn 처리. (Universal Links는 Supabase redirect 허용 목록에 커스텀 스킴 등록으로 충분)

## 컴포넌트 매핑

| Android 컴포넌트 | 속성 | iOS 대응 |
|---|---|---|
| MainActivity (launcher + VIEW 딥링크) | singleTask 유사 | SwiftUI App + onOpenURL |
| AlarmAlertActivity | showWhenLocked, turnScreenOn, excludeFromRecents, singleTop | 포그라운드: fullScreenCover 알람 뷰. 잠금화면 자동 표시는 불가 → time-sensitive 알림(사운드 포함)으로 대체 |
| LocationTrackingService (foregroundServiceType=location) | 상시 알림 표시 | CLLocationManager(background) + 파란 상태 표시줄 인디케이터; 상시 알림 개념 없음 |
| AlarmAlertService (shortService) | 사운드/진동 재생 | 포그라운드 AVAudioPlayer; 백그라운드는 알림 커스텀 사운드(≤30s) |
| WakepointFirebaseMessagingService | FCM data 메시지 | APNs + Notification Service Extension(mutable-content)으로 친구 알람 DB 반영 |
| BootCompletedReceiver (BOOT_COMPLETED, MY_PACKAGE_REPLACED) | 추적 재개 | Region Monitoring 재기동 특성 + 앱 실행 시 syncTracking |
| AlarmStopReceiver | 알림 액션 "알람 끄기" | UNNotificationAction("알람 끄기") + delegate 처리 |
| meta-data GOOGLE_MAPS_API_KEY | 지도 | MapKit 사용 시 불필요 (Google Maps iOS SDK 채택 시 AppDelegate에서 키 주입) |
| backup_rules / data_extraction_rules | 백업 제외 규칙 | iCloud 백업 제외 필요 시 isExcludedFromBackup |

## Background Modes / Capabilities 정리

- Location updates (필수 — 핵심 기능)
- Remote notifications (친구 알람 수신)
- Push Notifications capability + APNs 키
- Region Monitoring(별도 capability 불필요, 알람 최대 20개 동시 모니터링 제한 주의 — 초과 시 가까운 순 우선)
- (선택) Time Sensitive Notifications entitlement, Critical Alerts(특별 승인)
