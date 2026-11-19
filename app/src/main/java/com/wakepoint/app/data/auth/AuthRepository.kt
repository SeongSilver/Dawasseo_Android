package com.wakepoint.app.data.auth

import com.wakepoint.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

data class AuthSession(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long
)

data class AuthOperationResult(
    val sessionStarted: Boolean,
    val message: String? = null
)

interface AuthRepository {
    val authSession: Flow<AuthSession?>

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<AuthOperationResult>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        nickname: String
    ): Result<AuthOperationResult>

    fun kakaoOAuthUrl(): String

    fun googleOAuthUrl(): String

    suspend fun requestPasswordReset(email: String): Result<Unit>

    suspend fun fetchCurrentProfile(): Result<UserProfile>

    suspend fun updateNickname(nickname: String): Result<UserProfile>

    /**
     * DataStore에 저장된 FCM 토큰을 로그인 사용자의 `user_profiles.push_token`에 반영한다.
     * 저장된 토큰이나 세션이 없으면 아무것도 하지 않고 성공을 반환한다.
     */
    suspend fun syncPushToken(): Result<Unit>

    suspend fun completeOAuthSignIn(callbackUri: String): Result<AuthOperationResult>

    suspend fun refreshSessionIfNeeded(): Result<AuthSession?>

    suspend fun requireValidSession(): AuthSession

    suspend fun signOut()
}
