package com.wakepoint.app.data.auth

import kotlinx.coroutines.flow.Flow

data class AuthSession(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String?
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

    suspend fun completeOAuthSignIn(callbackUri: String): Result<AuthOperationResult>

    suspend fun signOut()
}
