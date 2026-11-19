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

    suspend fun completeOAuthSignIn(callbackUri: String): Result<AuthOperationResult>

    suspend fun refreshSessionIfNeeded(): Result<AuthSession?>

    suspend fun requireValidSession(): AuthSession

    suspend fun signOut()
}
