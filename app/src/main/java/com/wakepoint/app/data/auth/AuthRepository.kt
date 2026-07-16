package com.wakepoint.app.data.auth

import kotlinx.coroutines.flow.Flow

data class AuthSession(
    val userId: String,
    val email: String,
    val nickname: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long
)

data class AuthOperationResult(
    val sessionStarted: Boolean,
    val message: String? = null
)

/** 인증/REST 요청이 2xx 가 아닌 HTTP 상태로 실패했음을 나타낸다(네트워크 오류와 구분용). */
class AuthHttpException(
    val statusCode: Int,
    message: String
) : Exception(message)

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

    suspend fun completeOAuthSignIn(callbackUri: String): Result<AuthOperationResult>

    /** 비밀번호 재설정 메일을 요청한다. */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun refreshSessionIfNeeded(): Result<AuthSession?>

    suspend fun requireValidSession(): AuthSession

    suspend fun updateNickname(nickname: String): Result<Unit>

    /** FCM 푸시 토큰을 서버(user_profiles.push_token)에 반영한다. 친구가 알람 푸시를 보내는 데 필요. */
    suspend fun uploadPushToken(token: String): Result<Unit>

    suspend fun signOut()
}
