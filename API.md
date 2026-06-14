# API.md — 다왔어 API 연동 흐름

> Supabase, Kakao Local API, FCM 연동 흐름 정리.
> Android 네이티브(Kotlin + supabase-kt) 기준.

---

## 1. Supabase 클라이언트 설정

```kotlin
// core/supabase/SupabaseClient.kt
val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
    install(Storage)
}
```

---

## 2. 인증 (Auth)

### 이메일 회원가입 / 로그인

```kotlin
// 회원가입
supabase.auth.signUpWith(Email) {
    email = "user@example.com"
    password = "password"
}

// 로그인
supabase.auth.signInWith(Email) {
    email = "user@example.com"
    password = "password"
}

// 세션 조회
val session = supabase.auth.currentSessionOrNull()
```

### Google 로그인
Google Sign-In으로 idToken 획득 후 Supabase 연동:

```kotlin
supabase.auth.signInWith(IDToken) {
    idToken = googleIdToken
    provider = Google
}
```

### 카카오 로그인
Kakao SDK로 로그인 후 받은 idToken을 Supabase로 전달:

```kotlin
// 1. Kakao 로그인
UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
    // token.idToken 획득
}

// 2. Supabase 연동 (OIDC)
supabase.auth.signInWith(IDToken) {
    idToken = kakaoIdToken
    provider = Kakao   // 커스텀 OIDC provider 설정 필요
}
```

> 카카오톡 미설치 시 `loginWithKakaoAccount`(웹 로그인)로 폴백.

---

## 3. 알람 API (Postgrest)

### 알람 생성 (본인)

```kotlin
supabase.from("alarms").insert(
    AlarmDto(
        ownerId = userId,
        createdBy = userId,
        label = "집에 거의 다 왔어요",
        targetLat = 37.5665,
        targetLng = 126.9780,
        radiusKm = 0.5
    )
)
```

### 친구 대신 알람 생성

```kotlin
// createdBy = 나, ownerId = 친구
// RLS 정책상 accepted 권한이 있어야 insert 성공
supabase.from("alarms").insert(
    AlarmDto(
        ownerId = friendUserId,
        createdBy = myUserId,
        label = "친구가 설정해준 알람",
        targetLat = 37.5665,
        targetLng = 126.9780,
        radiusKm = 1.0
    )
)
// → 성공 시 FCM 발송 (아래 5번)
```

### 알람 목록 조회

```kotlin
val alarms = supabase.from("alarms")
    .select {
        filter {
            eq("owner_id", userId)
            eq("is_active", true)
        }
        order("created_at", Order.DESCENDING)
    }
    .decodeList<AlarmDto>()
```

### 알람 트리거 기록

```kotlin
supabase.from("alarms").update(
    { set("is_active", false); set("triggered_at", Clock.System.now()) }
) {
    filter { eq("id", alarmId) }
}
```

---

## 4. Realtime 동기화

친구가 내 알람을 설정하면 실시간 수신:

```kotlin
val channel = supabase.channel("my-alarms")

val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
    table = "alarms"
    filter("owner_id", FilterOperator.EQ, userId)
}

changes
    .onEach { action ->
        when (action) {
            is PostgresAction.Insert -> { /* 새 알람 → 로컬 추가 */ }
            is PostgresAction.Delete -> { /* 알람 삭제 → 로컬 제거 */ }
            is PostgresAction.Update -> { /* 알람 갱신 */ }
            else -> {}
        }
    }
    .launchIn(viewModelScope)

channel.subscribe()
```

---

## 5. FCM (Firebase Cloud Messaging)

### 토큰 등록

```kotlin
// WakepointFirebaseMessagingService.kt
override fun onNewToken(token: String) {
    // Supabase user_profiles.push_token 업데이트
    scope.launch {
        supabase.from("user_profiles").update(
            { set("push_token", token) }
        ) {
            filter { eq("id", currentUserId) }
        }
    }
}
```

### 푸시 발송 (Supabase Edge Function)

클라이언트는 직접 FCM 서버 키를 갖지 않는다. Edge Function을 경유한다.

```kotlin
// 친구 대신 알람 설정 후 호출
supabase.functions.invoke(
    function = "send-push",
    body = SendPushRequest(
        targetUserId = friendUserId,
        title = "알람이 설정됐어요 📍",
        body = "${myNickname}님이 알람을 설정해줬어요"
    )
)
```

### Edge Function (서버 측)

```typescript
// supabase/functions/send-push/index.ts
serve(async (req) => {
  const { targetUserId, title, body } = await req.json();

  // 대상 push_token 조회
  const { data } = await supabaseAdmin
    .from("user_profiles")
    .select("push_token")
    .eq("id", targetUserId)
    .single();

  if (!data?.push_token) return new Response("No token", { status: 400 });

  // FCM 발송
  await fetch("https://fcm.googleapis.com/v1/projects/PROJECT/messages:send", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      message: {
        token: data.push_token,
        notification: { title, body },
      },
    }),
  });

  return new Response(JSON.stringify({ ok: true }));
});
```

### 푸시 시나리오

| 트리거 | 제목 | 내용 |
|--------|------|------|
| 친구가 알람 설정 | 알람이 설정됐어요 📍 | OO님이 알람을 설정해줬어요 |
| 권한 요청 도착 | 알람 설정 권한 요청 🔔 | OO님이 권한을 요청했어요 |

---

## 6. 친구 & 권한 API

### 이메일로 친구 검색

```kotlin
supabase.from("user_profiles")
    .select(columns = Columns.list("id", "nickname", "avatar_url")) {
        filter { ilike("email", "%$query%") }
        limit(10)
    }
    .decodeList<UserProfileDto>()
```

### 친구 추가 (양방향)

```kotlin
supabase.from("friends").insert(listOf(
    FriendDto(userId = myId, friendId = friendId),
    FriendDto(userId = friendId, friendId = myId)
))
```

### 권한 요청 / 수락

```kotlin
// 요청
supabase.from("alarm_permissions").insert(
    AlarmPermissionDto(requesterId = myId, targetId = friendId, status = "pending")
)

// 수락
supabase.from("alarm_permissions").update(
    { set("status", "accepted") }
) {
    filter { eq("id", permissionId) }
}
```

---

## 7. Kakao Local API (장소 검색)

REST API 키로 키워드 장소 검색. Android에서 Retrofit/Ktor로 호출.

```
GET https://dapi.kakao.com/v2/local/search/keyword.json
Header: Authorization: KakaoAK {KAKAO_REST_API_KEY}
Query:  query={검색어}
```

```kotlin
// Retrofit 예시
interface KakaoLocalApi {
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Header("Authorization") auth: String,  // "KakaoAK $KAKAO_REST_API_KEY"
        @Query("query") query: String
    ): KakaoSearchResponse
}
```

응답에서 `documents[].x`(경도), `y`(위도), `place_name`, `address_name`을 추출해 지도 핀 + 알람 목적지로 사용.

---

## 8. 커스텀 알람음 (Storage)

### 녹음 → 업로드

```kotlin
// 1. MediaRecorder로 녹음 → m4a 파일
// 2. Storage 업로드
val path = "$userId/${System.currentTimeMillis()}.m4a"
supabase.storage.from("alarm-sounds").upload(path, audioBytes)

// 3. alarms 레코드에 경로 저장
supabase.from("alarms").update({
    set("sound_type", "custom")
    set("sound_uri", path)
}) { filter { eq("id", alarmId) } }
```

### 재생 (알람 트리거 시)

```kotlin
// Storage에서 서명된 URL 획득 후 Media3/ExoPlayer 반복 재생
val signedUrl = supabase.storage.from("alarm-sounds")
    .createSignedUrl(path, expiresIn = 60.seconds)
// ExoPlayer로 isLooping = true 재생 + 진동
```

---

## 에러 코드

| 코드 | 상황 | 처리 |
|------|------|------|
| LOCATION_PERMISSION_DENIED | 위치 권한 거부 | 설정 안내 |
| BACKGROUND_PERMISSION_DENIED | 백그라운드 위치 권한 거부 | 단계적 재요청 안내 |
| PERMISSION_NOT_ACCEPTED | 대리 알람 권한 없음 (RLS insert 실패) | 권한 요청 유도 |
| PUSH_TOKEN_NOT_FOUND | FCM 토큰 없음 | 알림 권한 확인 |
| KAKAO_LOGIN_CANCELED | 카카오 로그인 취소 | 재시도 |

