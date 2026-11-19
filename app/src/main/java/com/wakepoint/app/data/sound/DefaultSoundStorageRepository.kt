package com.wakepoint.app.data.sound

import android.content.Context
import android.net.Uri
import com.wakepoint.app.core.supabase.SupabaseConfig
import com.wakepoint.app.data.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class DefaultSoundStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val config: SupabaseConfig
) : SoundStorageRepository {

    override suspend fun uploadSound(localUri: Uri, fileName: String): Result<String> = runCatching {
        check(config.isConfigured) {
            "Supabase 설정이 비어 있습니다. local.properties에 SUPABASE_URL과 SUPABASE_ANON_KEY를 입력해주세요."
        }
        val session = authRepository.requireValidSession()
        val objectPath = buildSoundObjectPath(session.userId, fileName)
        val contentType = context.contentResolver.getType(localUri) ?: "audio/mp4"
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(localUri)?.use { it.readBytes() }
        } ?: throw IllegalStateException("사운드 파일을 열 수 없습니다.")

        uploadObject(
            objectPath = objectPath,
            bytes = bytes,
            contentType = contentType,
            accessToken = session.accessToken
        )
        objectPath
    }

    private suspend fun uploadObject(
        objectPath: String,
        bytes: ByteArray,
        contentType: String,
        accessToken: String
    ) = withContext(Dispatchers.IO) {
        val url = URL("${config.url.trimEnd('/')}/storage/v1/object/$ALARM_SOUND_BUCKET/$objectPath")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", contentType)
            setRequestProperty("apikey", config.anonKey)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("x-upsert", "true")
        }
        connection.outputStream.use { output -> output.write(bytes) }

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

    private fun parseErrorMessage(responseText: String): String {
        if (responseText.isBlank()) return "사운드 업로드에 실패했습니다."
        return runCatching {
            val json = JSONObject(responseText)
            listOf("message", "error", "statusCode")
                .firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
        }.getOrNull() ?: "사운드 업로드에 실패했습니다."
    }
}
