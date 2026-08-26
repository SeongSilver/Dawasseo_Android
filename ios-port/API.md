# API — 네트워크 레이어

Retrofit 없음. `HttpURLConnection` 직접 사용 + 수동 JSON(JSONObject). 재시도 래퍼 `retryingNetwork()` 존재.
백엔드: **Supabase** (Auth + PostgREST) / **Kakao Local API** (장소 검색) / **FCM** (푸시 수신).
상세 스키마·RLS는 복사된 `docs/API.md`, `docs/SCHEMA.md` 참조. 서버 함수는 리포 루트 `supabase/` 디렉터리에 존재.

## 인증 방식

- Supabase 공통 헤더: `apikey: <SUPABASE_ANON_KEY>`, `Authorization: Bearer <access_token|anon_key>`
- PostgREST 쓰기: `Prefer: return=representation` / upsert 시 `resolution=merge-duplicates` 계열 + `?on_conflict=`
- Kakao: `Authorization: KakaoAK <KAKAO_REST_API_KEY>`
- 키 주입: local.properties → BuildConfig (SUPABASE_URL, SUPABASE_ANON_KEY, KAKAO_REST_API_KEY, KAKAO_NATIVE_APP_KEY, GOOGLE_MAPS_API_KEY, FIREBASE_PROJECT_ID). iOS: xcconfig + Info.plist.
- 비-2xx 응답 → AuthHttpException(statusCode).

## Supabase Auth (`{SUPABASE_URL}/auth/v1`)

| 메서드 | 경로 | 용도 | 요청 | 응답 |
|---|---|---|---|---|
| POST | /auth/v1/token?grant_type=password | 이메일 로그인 | {email, password} | access_token, refresh_token, expires_at, user{...} → AuthSession |
| POST | /auth/v1/token?grant_type=refresh_token | 세션 갱신 | {refresh_token} | 동일 |
| POST | /auth/v1/signup | 회원가입 | {email, password, data:{nickname}} | 세션 또는 이메일 확인 대기 |
| POST | /auth/v1/recover | 비밀번호 재설정 메일 | {email} | 204 |
| POST | /auth/v1/logout | 로그아웃 | Bearer 토큰 | 204 |
| GET | /auth/v1/authorize?provider=kakao&redirect_to=com.wakepoint.app://auth-callback | Kakao OAuth 시작(외부 브라우저) | — | 콜백 URI fragment의 access_token/refresh_token 파싱(completeOAuthSignIn) |
| GET/PUT | /auth/v1/user | 사용자 조회/닉네임 변경 | — | user 객체 |

## Supabase PostgREST (`{SUPABASE_URL}/rest/v1`)

### alarms (DefaultAlarmRepository)
| 메서드 | 경로 | 용도 |
|---|---|---|
| GET | /rest/v1/alarms?owner_id=eq.{userId} | 내 알람 fetch(동기화) |
| POST | /rest/v1/alarms?on_conflict=id (Prefer: merge-duplicates) | 알람 업서트 (AlarmDto) |
| PATCH | /rest/v1/alarms?id=eq.{id} | 부분 수정(활성/라벨 등) |
| DELETE | /rest/v1/alarms?id=eq.{id} | 알람 삭제 |

### user_profiles (DefaultAuthRepository, DefaultFriendRepository)
| 메서드 | 경로 | 용도 |
|---|---|---|
| POST | /rest/v1/user_profiles?on_conflict=id | 프로필 업서트(가입/로그인 시) |
| PATCH | /rest/v1/user_profiles?id=eq.{userId} | 닉네임/push_token 갱신 |
| GET | /rest/v1/user_profiles?or=(email.ilike...,nickname.ilike...) | 사용자 검색 |
| GET | /rest/v1/user_profiles?id=in.({ids}) | 프로필 일괄 조회 |

### friends (DefaultFriendRepository)
| 메서드 | 경로 | 용도 |
|---|---|---|
| GET | /rest/v1/friends?or=(user_id.eq...,friend_id.eq...) | 친구 목록 fetch |
| POST | /rest/v1/friends | 친구 요청 생성 (status=pending) |
| PATCH | /rest/v1/friends?id=eq.{id} | 수락/거절/차단 (status 변경) |
| DELETE | /rest/v1/friends?id=eq.{id} | 친구 삭제 |

### alarm_permissions (DefaultFriendRepository)
| 메서드 | 경로 | 용도 |
|---|---|---|
| GET | /rest/v1/alarm_permissions?or=(...) | 권한 요청 목록 |
| POST | /rest/v1/alarm_permissions | 알람 대리 설정 권한 요청 |
| PATCH | /rest/v1/alarm_permissions?id=eq.{id} | 수락/거절 |

## Kakao Local API (KakaoLocalRepository)

| 메서드 | 경로 | 용도 |
|---|---|---|
| GET | https://dapi.kakao.com/v2/local/search/keyword.json?query= | 키워드 장소 검색 |
| GET | https://dapi.kakao.com/v2/local/search/address.json?query= | 주소 검색 (키워드 결과와 병합, dedupe) |
| GET | https://dapi.kakao.com/v2/local/geo/coord2address.json?x={lng}&y={lat} | 역지오코딩(좌표→주소) |

응답 → PlaceSearchResult(id, name, address, lat, lng).
iOS 대안: Kakao REST 그대로 사용 가능(플랫폼 무관 REST). 또는 MKLocalSearch/CLGeocoder로 대체(한국 주소 품질은 Kakao 우세).

## 푸시 (WakepointFirebaseMessagingService)

- FCM data 메시지로 "친구가 보낸 알람"(AlarmDto JSON) 수신 → pendingFriendAlarms 큐 저장 → 로컬 DB 반영 → 추적 서비스 갱신.
- 토큰: onNewToken → PushTokenSyncManager.persistAndUpload → user_profiles.push_token PATCH.
- iOS 대응: FCM iOS SDK(APNs 연동) 또는 서버를 APNs 직접 발송으로 변경 필요. 서버 발송부는 `supabase/` 함수 확인.
