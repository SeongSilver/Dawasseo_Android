package com.wakepoint.app.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.data.auth.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * FCM 푸시 토큰을 로컬에 저장하고 서버(user_profiles.push_token)에 반영한다.
 *
 * 친구가 알람을 보낼 때 상대 기기로 FCM 을 발송하려면 서버에 각 사용자의 토큰이 있어야 한다.
 * - 로그인 후: [sync] 로 현재 토큰을 조회해 업로드
 * - 토큰 갱신(onNewToken): [persistAndUpload] 로 즉시 반영
 * 로그아웃 상태 등으로 업로드에 실패하면 로컬에는 저장되며 다음 로그인 때 [sync] 로 재업로드된다.
 */
@Singleton
class PushTokenSyncManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferencesDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 현재 FCM 토큰을 비동기로 조회해 로컬 저장 + 서버 업로드. */
    fun sync() {
        // Firebase 가 초기화되지 않은 환경(google-services 미설정 등)에서도 앱 흐름이 깨지지 않도록 방어.
        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (!token.isNullOrBlank()) {
                        scope.launch { persistAndUpload(token) }
                    }
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "FCM 토큰 조회 실패", error)
                }
        }.onFailure { Log.w(TAG, "FCM 미초기화 등으로 토큰 동기화 생략", it) }
    }

    /** 주어진 토큰을 로컬 저장 후 서버에 업로드(실패는 무시하고 로컬에는 보존). */
    suspend fun persistAndUpload(token: String) {
        userPreferences.savePushToken(token)
        authRepository.uploadPushToken(token).onFailure {
            Log.w(TAG, "푸시 토큰 업로드 실패(로컬 저장됨, 다음 로그인 시 재시도)", it)
        }
    }

    private companion object {
        const val TAG = "PushTokenSync"
    }
}
