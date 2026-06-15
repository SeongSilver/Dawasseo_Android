# SETUP.md — 다왔어 Android 개발 환경 세팅

> Android Studio 기준 개발 환경 구성 및 외부 서비스 키 설정 가이드.

---

## 1. 기본 개발 환경

### Android Studio
- 최신 안정 버전 설치 ([developer.android.com/studio](https://developer.android.com/studio))
- 설치 시 Android SDK, Android Virtual Device 함께 설치

### JDK
- **JDK 17 이상** 필요
- Android Studio 내장 JDK 사용 권장 (File → Settings → Build Tools → Gradle → Gradle JDK)

### SDK 설정
Android Studio → SDK Manager에서 확인:

| 항목 | 권장값 |
|------|--------|
| compileSdk | 34 이상 |
| minSdk | 26 (Android 8.0) — 백그라운드 위치 정책 고려 |
| targetSdk | 34 이상 |
| Build Tools | 최신 |

### Gradle
- Gradle Kotlin DSL (`build.gradle.kts`) 사용
- Android Studio가 프로젝트 열 때 자동으로 Gradle Wrapper 다운로드

---

## 2. local.properties 키 설정

프로젝트 루트의 `local.properties`에 키를 추가한다. **이 파일은 절대 Git에 커밋하지 않는다.**

```properties
# Android SDK 경로 (Android Studio가 자동 생성)
sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk

# Supabase
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=sb_pub_xxxxx

# Google Maps
GOOGLE_MAPS_API_KEY=AIzaSyxxxxx

# Kakao
KAKAO_NATIVE_APP_KEY=xxxxx
KAKAO_REST_API_KEY=xxxxx

# Firebase
FIREBASE_PROJECT_ID=dawasseo-xxxxx
```

### build.gradle.kts에서 BuildConfig로 주입

```kotlin
// app/build.gradle.kts
import java.util.Properties

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${localProps["SUPABASE_URL"]}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProps["SUPABASE_ANON_KEY"]}\"")
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = localProps["KAKAO_NATIVE_APP_KEY"] ?: ""
    }
    buildFeatures { buildConfig = true }
}
```

> 코드에서는 `BuildConfig.SUPABASE_URL` 형태로 접근.

---

## 3. Supabase 설정

1. [supabase.com](https://supabase.com)에서 프로젝트 생성 (지역: Northeast Asia / Seoul)
2. SQL Editor에서 `SCHEMA.md`의 DDL + RLS 정책 실행
3. Storage에서 `alarm-sounds` 버킷 생성 (→ SCHEMA.md 참고)
4. Authentication → Providers에서 Google, Kakao(커스텀) 활성화
5. **Project Settings → API**에서 키 복사
   - Project URL → `SUPABASE_URL`
   - `anon` `public` 키 → `SUPABASE_ANON_KEY`

> ⚠️ `service_role` 키는 절대 앱에 포함하지 않는다. Edge Function 서버에서만 사용.

### Kotlin 의존성 (supabase-kt)

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(platform("io.github.jan-tennert.supabase:bom:VERSION"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.ktor:ktor-client-android:KTOR_VERSION")
}
```

---

## 4. Firebase (FCM) 설정

1. [Firebase Console](https://console.firebase.google.com)에서 프로젝트 생성
2. Android 앱 추가
   - 패키지명: `com.wakepoint.app`
   - SHA-1 인증서 지문 등록 (아래 명령어로 추출)
3. `google-services.json` 다운로드 → `app/` 디렉토리에 배치
4. 빌드 스크립트에 google-services 플러그인 추가

### SHA-1 추출

```powershell
# 디버그 키스토어 SHA-1
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### build.gradle.kts

```kotlin
// 프로젝트 레벨
plugins {
    id("com.google.gms.google-services") version "4.4.x" apply false
}

// app 레벨
plugins {
    id("com.google.gms.google-services")
}
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:BOM_VERSION"))
    implementation("com.google.firebase:firebase-messaging")
}
```

---

## 5. Kakao SDK 설정

1. [developers.kakao.com](https://developers.kakao.com)에서 앱 생성
2. **플랫폼 → Android 등록**
   - 패키지명: `com.wakepoint.app`
   - 키 해시 등록 (아래 명령어로 추출)
3. **카카오 로그인 활성화** + Redirect URI 등록
4. 앱 키 → 네이티브 앱 키 복사 → `KAKAO_NATIVE_APP_KEY`

### 키 해시 추출

```powershell
keytool -exportcert -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore" -storepass android -keypass android | openssl sha1 -binary | openssl base64
```

### AndroidManifest.xml

```xml
<!-- 카카오 로그인 리다이렉트 -->
<activity android:name="com.kakao.sdk.auth.AuthCodeHandlerActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="kakao${KAKAO_NATIVE_APP_KEY}" android:host="oauth" />
    </intent-filter>
</activity>
```

### 초기화 (Application)

```kotlin
// WakepointApplication.kt
class WakepointApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
```

---

## 6. Google Maps 설정

1. [Google Cloud Console](https://console.cloud.google.com)에서 프로젝트 생성
2. **APIs & Services → Library**에서 활성화
   - Maps SDK for Android
3. **사용자 인증 정보 → API 키 생성** → `GOOGLE_MAPS_API_KEY`
4. **키 제한 필수**: Android 앱 (패키지명 + SHA-1)

> Maps SDK는 무료. Geocoding/Places 사용 시 월 $200 크레딧 내 과금. 예산 알림 설정 권장.

### AndroidManifest.xml

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${GOOGLE_MAPS_API_KEY}" />
```

---

## 7. 권한 설정 (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.VIBRATE" />
```

> 위치 권한은 단계적 요청: foreground → background.
> Android 13+ 알림 권한 `POST_NOTIFICATIONS`는 런타임에 별도 요청.

---

## 8. 빌드 & 실행

```powershell
.\gradlew assembleDebug          # Debug APK
.\gradlew installDebug           # 기기에 설치
.\gradlew bundleRelease          # Release AAB
.\gradlew test                   # Unit test
.\gradlew connectedAndroidTest   # Instrumentation test
.\gradlew lint                   # Lint
```

---

## 세팅 체크리스트

- [ ] Android Studio + JDK 17 설치
- [ ] local.properties 키 6개 입력
- [ ] Supabase 프로젝트 생성 + 스키마 적용 + alarm-sounds 버킷
- [ ] Firebase 프로젝트 + google-services.json 배치 + SHA-1 등록
- [ ] Kakao 앱 + Android 플랫폼 + 키 해시 등록
- [ ] Google Maps API 키 + Android 제한 설정
- [ ] AndroidManifest 권한 설정
- [ ] 실기기 빌드 확인

