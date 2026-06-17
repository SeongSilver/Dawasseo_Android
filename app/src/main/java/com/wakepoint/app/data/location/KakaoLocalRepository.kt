package com.wakepoint.app.data.location

import com.wakepoint.app.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class PlaceSearchResult(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double
)

@Singleton
class KakaoLocalRepository @Inject constructor() {
    suspend fun searchKeyword(query: String): Result<List<PlaceSearchResult>> = runCatching {
        check(BuildConfig.KAKAO_REST_API_KEY.isNotBlank()) {
            "Kakao REST API 키가 비어 있습니다."
        }
        check(query.isNotBlank()) {
            "검색어를 입력해 주세요."
        }

        withContext(Dispatchers.IO) {
            val keywordResults = requestKeywordSearch(query.trim())
            val addressResults = requestAddressSearch(query.trim())
            (keywordResults + addressResults).distinctBy { result ->
                result.id.ifBlank { "${result.lat},${result.lng},${result.name}" }
            }
        }
    }

    suspend fun resolveAddress(lat: Double, lng: Double): Result<String> = runCatching {
        check(BuildConfig.KAKAO_REST_API_KEY.isNotBlank()) {
            "Kakao REST API 키가 비어 있습니다."
        }

        withContext(Dispatchers.IO) {
            val url = URL("https://dapi.kakao.com/v2/local/geo/coord2address.json?x=$lng&y=$lat")
            val documents = executeKakaoRequest(url).optJSONArray("documents")
            val item = documents?.optJSONObject(0)
            val roadAddress = item?.optJSONObject("road_address")
            val address = item?.optJSONObject("address")
            roadAddress.optStringOrBlank("address_name")
                .ifBlank { address.optStringOrBlank("address_name") }
                .ifBlank { "지도에서 선택한 위치" }
        }
    }

    private fun requestKeywordSearch(query: String): List<PlaceSearchResult> {
        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val url = URL("https://dapi.kakao.com/v2/local/search/keyword.json?query=$encodedQuery")
        val documents = executeKakaoRequest(url).optJSONArray("documents")
        return buildList {
            if (documents == null) return@buildList
            for (index in 0 until documents.length()) {
                val item = documents.optJSONObject(index) ?: continue
                val lat = item.optString("y").toDoubleOrNull() ?: continue
                val lng = item.optString("x").toDoubleOrNull() ?: continue
                val name = item.optString("place_name").ifBlank { "검색 결과" }
                val address = item.optString("road_address_name")
                    .ifBlank { item.optString("address_name") }
                add(
                    PlaceSearchResult(
                        id = item.optString("id").ifBlank { "keyword:$lat,$lng:$name" },
                        name = name,
                        address = address,
                        lat = lat,
                        lng = lng
                    )
                )
            }
        }
    }

    private fun requestAddressSearch(query: String): List<PlaceSearchResult> {
        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val url = URL("https://dapi.kakao.com/v2/local/search/address.json?query=$encodedQuery")
        val documents = executeKakaoRequest(url).optJSONArray("documents")
        return buildList {
            if (documents == null) return@buildList
            for (index in 0 until documents.length()) {
                val item = documents.optJSONObject(index) ?: continue
                val lat = item.optString("y").toDoubleOrNull() ?: continue
                val lng = item.optString("x").toDoubleOrNull() ?: continue
                val roadAddress = item.optJSONObject("road_address")
                val address = item.optJSONObject("address")
                val roadAddressName = roadAddress.optStringOrBlank("address_name")
                val addressName = address.optStringOrBlank("address_name")
                val buildingName = roadAddress.optStringOrBlank("building_name")
                val displayName = buildingName
                    .ifBlank { roadAddressName }
                    .ifBlank { addressName }
                    .ifBlank { item.optString("address_name") }
                add(
                    PlaceSearchResult(
                        id = "address:$lat,$lng:$displayName",
                        name = displayName,
                        address = roadAddressName.ifBlank { addressName },
                        lat = lat,
                        lng = lng
                    )
                )
            }
        }
    }

    private fun executeKakaoRequest(url: URL): JSONObject {
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
        }

        val responseText = connection.readResponseText()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(parseErrorMessage(responseText))
        }
        return JSONObject(responseText)
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
        if (responseText.isBlank()) return "장소 검색에 실패했습니다."
        return runCatching {
            JSONObject(responseText).optString("message").takeIf { it.isNotBlank() }
        }.getOrNull() ?: "장소 검색에 실패했습니다."
    }
}

private fun JSONObject?.optStringOrBlank(name: String): String {
    return this?.optString(name).orEmpty()
}
