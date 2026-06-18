package com.wakepoint.app.data.auth

import android.net.Uri
import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.core.supabase.SupabaseConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val config: SupabaseConfig,
    private val preferences: UserPreferencesDataStore
) : AuthRepository {
    override val authSession: Flow<AuthSession?> = preferences.authSession
    private val refreshMutex = Mutex()

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<AuthOperationResult> = runCatching {
        ensureConfigured()
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
        val response = postAuth("/auth/v1/token?grant_type=password", body)
        val session = parseSession(response)
            ?: error("로그인 응답에서 세션을 확인하지 못했습니다.")
        preferences.saveAuthSession(session)
        AuthOperationResult(sessionStarted = true)
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        nickname: String
    ): Result<AuthOperationResult> = runCatching {
        ensureConfigured()
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("data", JSONObject().put("nickname", nickname))
        val response = postAuth("/auth/v1/signup", body)
        val session = parseSession(response)
        if (session != null) {
            upsertUserProfile(
                session = session,
                nickname = nickname
            )
            preferences.saveAuthSession(session)
            AuthOperationResult(sessionStarted = true)
        } else {
            AuthOperationResult(
                sessionStarted = false,
                message = "가입 확인 메일을 확인한 뒤 로그인해주세요."
            )
        }
    }

    override fun kakaoOAuthUrl(): String {
        ensureConfigured()
        val redirectTo = URLEncoder.encode(OAUTH_REDIRECT_URI, Charsets.UTF_8.name())
        val scopes = URLEncoder.encode(KAKAO_OAUTH_SCOPES, Charsets.UTF_8.name())
        return "${config.url.trimEnd('/')}/auth/v1/authorize" +
            "?provider=kakao&redirect_to=$redirectTo&scopes=$scopes"
    }

    override suspend fun completeOAuthSignIn(
        callbackUri: String
    ): Result<AuthOperationResult> = runCatching {
        ensureConfigured()
        val params = Uri.parse(callbackUri).authCallbackParams()
        val errorMessage = params["error_description"] ?: params["error"]
        if (!errorMessage.isNullOrBlank()) {
            error(errorMessage)
        }

        val accessToken = params["access_token"]
            ?: error("카카오 로그인 콜백에서 Supabase 세션을 확인하지 못했습니다.")
        val user = fetchUser(accessToken)
        val userId = user.optString("id").takeIf { it.isNotBlank() }
            ?: error("카카오 로그인 사용자 정보를 확인하지 못했습니다.")
        val email = user.optString("email").takeIf { it.isNotBlank() }
            ?: "$userId@kakao.local"
        val session = AuthSession(
            userId = userId,
            email = email,
            accessToken = accessToken,
            refreshToken = params["refresh_token"],
            expiresAtEpochSeconds = params.tokenExpiresAt()
        )

        upsertUserProfile(
            session = session,
            nickname = user.oauthNickname() ?: email.substringBefore("@")
        )
        preferences.saveAuthSession(session)
        AuthOperationResult(sessionStarted = true)
    }

    override suspend fun refreshSessionIfNeeded(): Result<AuthSession?> = runCatching {
        refreshMutex.withLock {
            val session = authSession.first() ?: return@withLock null
            if (!session.shouldRefresh()) {
                return@withLock session
            }

            val refreshToken = session.refreshToken
            if (refreshToken.isNullOrBlank()) {
                preferences.clearAuthSession()
                return@withLock null
            }

            val body = JSONObject().put("refresh_token", refreshToken)
            val response = runCatching {
                postAuth("/auth/v1/token?grant_type=refresh_token", body)
            }.getOrElse {
                preferences.clearAuthSession()
                throw it
            }
            val refreshedSession = parseSession(response)
                ?: run {
                    preferences.clearAuthSession()
                    error("세션 갱신 응답을 확인하지 못했습니다.")
                }
            preferences.saveAuthSession(refreshedSession)
            refreshedSession
        }
    }

    override suspend fun requireValidSession(): AuthSession {
        return refreshSessionIfNeeded().getOrThrow()
            ?: error("로그인이 필요합니다.")
    }

    override suspend fun signOut() {
        val session = authSession.first()
        if (session != null && config.isConfigured) {
            runCatching {
                postAuth(
                    path = "/auth/v1/logout",
                    body = JSONObject(),
                    bearerToken = session.accessToken
                )
            }
        }
        preferences.clearAuthSession()
    }

    private fun ensureConfigured() {
        check(config.isConfigured) {
            "Supabase 설정이 비어 있습니다. local.properties에 SUPABASE_URL과 SUPABASE_ANON_KEY를 입력해주세요."
        }
    }

    private suspend fun postAuth(
        path: String,
        body: JSONObject,
        bearerToken: String = config.anonKey
    ): JSONObject = withContext(Dispatchers.IO) {
        val endpoint = URL("${config.url.trimEnd('/')}$path")
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", config.anonKey)
            setRequestProperty("Authorization", "Bearer $bearerToken")
        }

        connection.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val responseText = connection.readResponseText()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(parseErrorMessage(responseText))
        }
        if (responseText.isBlank()) JSONObject() else JSONObject(responseText)
    }

    private suspend fun upsertUserProfile(
        session: AuthSession,
        nickname: String
    ) = withContext(Dispatchers.IO) {
        val endpoint = URL("${config.url.trimEnd('/')}/rest/v1/user_profiles?on_conflict=id")
        val body = JSONObject()
            .put("id", session.userId)
            .put("email", session.email)
            .put("nickname", nickname)

        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", config.anonKey)
            setRequestProperty("Authorization", "Bearer ${session.accessToken}")
            setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
        }

        connection.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val responseText = connection.readResponseText()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(parseErrorMessage(responseText))
        }
    }

    private suspend fun fetchUser(accessToken: String): JSONObject = withContext(Dispatchers.IO) {
        val endpoint = URL("${config.url.trimEnd('/')}/auth/v1/user")
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("apikey", config.anonKey)
            setRequestProperty("Authorization", "Bearer $accessToken")
        }

        val responseText = connection.readResponseText()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(parseErrorMessage(responseText))
        }
        JSONObject(responseText)
    }

    private fun HttpURLConnection.readResponseText(): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        if (stream == null) return ""
        return stream.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
    }

    private fun parseSession(response: JSONObject): AuthSession? {
        val accessToken = response.optString("access_token").takeIf { it.isNotBlank() }
            ?: return null
        val user = response.optJSONObject("user") ?: return null
        val userId = user.optString("id").takeIf { it.isNotBlank() } ?: return null
        val email = user.optString("email").takeIf { it.isNotBlank() } ?: return null
        return AuthSession(
            userId = userId,
            email = email,
            accessToken = accessToken,
            refreshToken = response.optString("refresh_token").takeIf { it.isNotBlank() },
            expiresAtEpochSeconds = response.tokenExpiresAt()
        )
    }

    private fun parseErrorMessage(responseText: String): String {
        if (responseText.isBlank()) return "인증 요청에 실패했습니다."
        return runCatching {
            val json = JSONObject(responseText)
            listOf("msg", "message", "error_description", "error")
                .firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
        }.getOrNull() ?: "인증 요청에 실패했습니다."
    }

    private fun Uri.authCallbackParams(): Map<String, String> {
        val params = mutableMapOf<String, String>()
        queryParameterNames.forEach { name ->
            getQueryParameter(name)?.let { value -> params[name] = value }
        }
        fragment
            ?.split("&")
            ?.filter { it.contains("=") }
            ?.forEach { pair ->
                val key = pair.substringBefore("=")
                val value = pair.substringAfter("=")
                params[key] = URLDecoder.decode(value, Charsets.UTF_8.name())
            }
        return params
    }

    private fun JSONObject.oauthNickname(): String? {
        val metadata = optJSONObject("user_metadata") ?: return null
        return listOf("nickname", "name", "full_name")
            .firstNotNullOfOrNull { key -> metadata.optString(key).takeIf { it.isNotBlank() } }
    }

    private fun AuthSession.shouldRefresh(): Boolean {
        if (expiresAtEpochSeconds <= 0L) return true
        return expiresAtEpochSeconds - Instant.now().epochSecond <= REFRESH_WINDOW_SECONDS
    }

    private fun JSONObject.tokenExpiresAt(): Long {
        val expiresAt = optLong("expires_at", 0L)
        if (expiresAt > 0L) return expiresAt

        val expiresIn = optLong("expires_in", 0L)
        return if (expiresIn > 0L) {
            Instant.now().epochSecond + expiresIn
        } else {
            0L
        }
    }

    private fun Map<String, String>.tokenExpiresAt(): Long {
        val expiresAt = this["expires_at"]?.toLongOrNull()
        if (expiresAt != null && expiresAt > 0L) return expiresAt

        val expiresIn = this["expires_in"]?.toLongOrNull()
        return if (expiresIn != null && expiresIn > 0L) {
            Instant.now().epochSecond + expiresIn
        } else {
            0L
        }
    }

    companion object {
        const val OAUTH_REDIRECT_URI = "com.wakepoint.app://auth-callback"
        private const val KAKAO_OAUTH_SCOPES = "profile_nickname profile_image"
        private const val REFRESH_WINDOW_SECONDS = 300L
    }
}
