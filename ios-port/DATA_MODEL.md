# DATA_MODEL — 엔티티/필드/타입/관계

로컬 DB: Room `WakepointDatabase` v8 (스키마: app/schemas/.../8.json). 테이블 2개 + DataStore(Preferences) + 파일 시스템(사운드).
iOS 대응: SwiftData(또는 Core Data/GRDB) + UserDefaults(App Group) + FileManager.

## 1. alarms (AlarmEntity ↔ 도메인 Alarm ↔ 서버 AlarmDto)

| 필드(도메인) | 컬럼 | 타입 | 기본값 | 서버 동기화 | 설명 |
|---|---|---|---|---|---|
| id | id (PK) | String (UUID) | — | O | |
| ownerId | owner_id | String | — | O | 알람 소유자(수신자) user id |
| createdBy | created_by | String | — | O | 생성자 — ownerId와 다르면 "친구가 설정" |
| label | label | String | — | O | 위치 별칭 |
| targetLat | target_lat | Double | — | O | |
| targetLng | target_lng | Double | — | O | |
| targetAddress | target_address | String | — | O | |
| radiusKm | radius_km | Double | 0.5 | O | 0.1~50 |
| isActive | is_active | Bool | — | O | |
| soundType | sound_type | String enum: "default"/"custom" | — | O | 도메인 enum SoundType { Default, Custom } |
| soundUri | sound_uri | String? | null | O | 커스텀 사운드 파일 URI |
| soundEnabled | sound_enabled | Bool | true | X(로컬) | 소리 재생 여부 |
| vibrate | vibrate | Bool | true | X(로컬) | 진동 여부 |
| isFavorite | is_favorite | Bool | false | X(로컬) | 즐겨찾기 |
| repeating | repeating | Bool | false | X(로컬) | 계속 유지(반경 이탈 후 재진입 시 재발동) |
| pendingSync | pending_sync | Bool | false | X(로컬) | 서버 미반영 표시, 다음 동기화 때 재전송 |

AlarmDto(서버 페이로드): id, ownerId, createdBy, label, targetLat, targetLng, targetAddress, radiusKm, isActive, soundType("default"/"custom"), soundUri — JSON 수동 직렬화(JSONObject). 로컬 전용 필드(soundEnabled, vibrate, isFavorite, repeating, pendingSync)는 DTO에 없음.

AlarmDao 주요 쿼리: observeAlarms / observeActiveAlarms / countActiveAlarms / getAllAlarms / getAlarm(id) / pendingSyncAlarms / upsertAlarm / setPendingSync / deleteSyncedAlarmsNotIn(ids) / deleteSyncedAlarms / updateAlarmSettings / setAlarmActive / setFavorite / deleteAlarm.

## 2. friends (FriendEntity ↔ 도메인 Friend)

| 필드 | 컬럼 | 타입 | 기본값 | 설명 |
|---|---|---|---|---|
| id | id (PK) | String | — | friendship id |
| userId | user_id | String | — | 나 |
| friendId | friend_id | String | — | 상대 user id |
| nickname | nickname | String | — | |
| email | email | String | — | |
| avatarUrl | avatar_url | String? | null | |
| status | status | String enum | Accepted | FriendStatus: Pending/Accepted/Rejected/Blocked |
| permissionStatus | permission_status | String enum | None | PermissionStatus: None/Pending/Accepted/Rejected — 알람 대리 설정 권한 |
| permissionExpiresAt | permission_expires_at | String? (ISO8601) | null | |
| createdAt | created_at | String? (ISO8601) | null | |

FriendDao: observeFriends / observeAcceptedFriends / snapshotFriends / upsertFriends / clearFriends / deleteFriend.

## 3. 도메인 전용/네트워크 모델 (로컬 DB 미저장)

- **UserProfile**: id, email, nickname, avatarUrl?, pushToken? — 서버 `user_profiles` 테이블.
- **AlarmPermission / AlarmPermissionRequest**: id, requesterId, targetId, status(PermissionStatus), requesterProfile?, targetProfile?, expiresAt?, createdAt? — 서버 `alarm_permissions`.
- **FriendSearchResult**: profile(UserProfile), existingFriendStatus(FriendStatus?).
- **AuthSession**: userId, email, nickname, accessToken, refreshToken?, expiresAtEpochSeconds — iOS는 Keychain 저장 권장.
- **AuthOperationResult**: sessionStarted(Bool), message(String?).
- **PlaceSearchResult** (Kakao Local): id, name, address, lat, lng.
- **AlarmSound**: id, title, subtitle, uri, kind(SoundKind: Bundled/Recorded/Imported/System). 파생 soundType = Bundled→Default, 그 외→Custom.
- **SupabaseConfig**: url, anonKey (빌드 설정 주입; iOS는 xcconfig/Info.plist).

## 4. DataStore (UserPreferencesDataStore → iOS UserDefaults/Keychain)

- pushToken (FCM/APNs 토큰)
- defaultSoundUri (기본 알람음)
- pendingFriendAlarms: Set<String> — 푸시로 받았지만 아직 처리 못 한 친구 알람 DTO(JSON 문자열) 큐
- pendingDeletions: Set<String> — 서버 삭제 실패한 알람 id 큐
- (세션 토큰 저장도 이 계층 — iOS는 Keychain으로)

## 5. 관계 요약

- Alarm.ownerId / createdBy → UserProfile.id (서버측 FK)
- Friend.userId, friendId → UserProfile.id
- AlarmPermission.requesterId/targetId → UserProfile.id; Friend.permissionStatus는 alarm_permissions에서 파생돼 friends 로컬 행에 비정규화 저장
- Alarm.soundUri → AlarmSound.uri (파일 시스템; 접두사 bundled_/rec_/imp_/system_, 녹음 확장자 .m4a, 커스텀 디렉터리 `alarm_sounds/`)

## 6. Room 마이그레이션 이력 (참고)

v1→v8 마이그레이션 존재(sound_enabled/vibrate/is_favorite/repeating/pending_sync 등 로컬 컬럼이 점진 추가됨). iOS는 v8 스키마 기준으로 신규 설계하면 됨.
