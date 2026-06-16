package com.wakepoint.app.data.auth

import com.wakepoint.app.core.data.preferences.UserPreferencesDataStore
import com.wakepoint.app.core.supabase.SupabaseConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val config: SupabaseConfig,
    private val preferences: UserPreferencesDataStore
) : AuthRepository {
    override val authSession: Flow<AuthSession?> = preferences.authSession

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
            refreshToken = response.optString("refresh_token").takeIf { it.isNotBlank() }
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
}
