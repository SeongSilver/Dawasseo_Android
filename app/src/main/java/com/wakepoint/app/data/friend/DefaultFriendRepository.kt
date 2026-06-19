package com.wakepoint.app.data.friend

import com.wakepoint.app.core.supabase.SupabaseConfig
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.friend.local.FriendDao
import com.wakepoint.app.data.friend.local.toDomain
import com.wakepoint.app.data.friend.local.toEntity
import com.wakepoint.app.domain.model.Friend
import com.wakepoint.app.domain.model.FriendStatus
import com.wakepoint.app.domain.model.PermissionStatus
import com.wakepoint.app.domain.model.UserProfile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class DefaultFriendRepository @Inject constructor(
    private val friendDao: FriendDao,
    private val authRepository: AuthRepository,
    private val config: SupabaseConfig
) : FriendRepository {
    override fun observeFriends(): Flow<List<Friend>> {
        return friendDao.observeFriends().map { friends ->
            friends.map { it.toDomain() }
        }
    }

    override fun observeAcceptedFriends(): Flow<List<Friend>> {
        return friendDao.observeAcceptedFriends().map { friends ->
            friends.map { it.toDomain() }
        }
    }

    override suspend fun refreshFriends() {
        val session = authRepository.requireValidSession()
        val remoteFriends = fetchRemoteFriends(
            currentUserId = session.userId,
            accessToken = session.accessToken
        )
        friendDao.clearFriends()
        friendDao.upsertFriends(remoteFriends.map { it.toEntity() })
    }

    override suspend fun searchUsers(query: String): List<FriendSearchResult> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < MIN_SEARCH_QUERY_LENGTH) return emptyList()

        val session = authRepository.requireValidSession()
        refreshFriends()
        val existingFriends = friendDao.snapshotFriends()
        val profiles = searchUserProfiles(
            query = trimmedQuery,
            currentUserId = session.userId,
            accessToken = session.accessToken
        )
        return profiles.map { profile ->
            val relation = existingFriends.firstOrNull {
                it.userId == profile.id || it.friendId == profile.id
            }
            FriendSearchResult(
                profile = profile,
                existingFriendStatus = relation?.status?.toFriendStatus()
            )
        }
    }

    override suspend fun sendFriendRequest(friendUserId: String) {
        val session = authRepository.requireValidSession()
        require(friendUserId != session.userId) {
            "본인에게 친구 요청을 보낼 수 없습니다."
        }
        val body = JSONObject()
            .put("user_id", session.userId)
            .put("friend_id", friendUserId)
            .put("status", FriendStatus.Pending.toRemoteStatus())
        executeRemoteRequest(
            path = "/rest/v1/friends",
            method = "POST",
            body = body,
            accessToken = session.accessToken,
            prefer = "return=minimal"
        )
        refreshFriends()
    }

    override suspend fun acceptFriendRequest(friendshipId: String) {
        updateFriendStatus(friendshipId, FriendStatus.Accepted)
    }

    override suspend fun rejectFriendRequest(friendshipId: String) {
        updateFriendStatus(friendshipId, FriendStatus.Rejected)
    }

    override suspend fun blockFriend(friendshipId: String) {
        updateFriendStatus(friendshipId, FriendStatus.Blocked)
    }

    override suspend fun deleteFriend(friendshipId: String) {
        val session = authRepository.requireValidSession()
        executeRemoteRequest(
            path = "/rest/v1/friends?id=eq.$friendshipId",
            method = "DELETE",
            body = null,
            accessToken = session.accessToken,
            prefer = "return=minimal"
        )
        friendDao.deleteFriend(friendshipId)
    }

    private suspend fun updateFriendStatus(
        friendshipId: String,
        status: FriendStatus
    ) {
        val session = authRepository.requireValidSession()
        executeRemoteRequest(
            path = "/rest/v1/friends?id=eq.$friendshipId",
            method = "PATCH",
            body = JSONObject().put("status", status.toRemoteStatus()),
            accessToken = session.accessToken,
            prefer = "return=minimal"
        )
        refreshFriends()
    }

    private suspend fun fetchRemoteFriends(
        currentUserId: String,
        accessToken: String
    ): List<Friend> = withContext(Dispatchers.IO) {
        val rows = executeRemoteRequest(
            path = "/rest/v1/friends?or=(user_id.eq.$currentUserId,friend_id.eq.$currentUserId)&select=*",
            method = "GET",
            body = null,
            accessToken = accessToken,
            prefer = "return=representation"
        ).toJsonArray()

        val relationRows = (0 until rows.length()).map { index ->
            rows.getJSONObject(index)
        }
        val otherUserIds = relationRows
            .mapNotNull { row ->
                val userId = row.optString("user_id")
                val friendId = row.optString("friend_id")
                val otherUserId = when (currentUserId) {
                    userId -> friendId
                    friendId -> userId
                    else -> null
                }
                otherUserId?.takeIf { it.isNotBlank() }
            }
            .distinct()

        val profiles = fetchUserProfiles(otherUserIds, accessToken).associateBy { it.id }
        relationRows.mapNotNull { row ->
            val userId = row.optString("user_id")
            val friendId = row.optString("friend_id")
            val otherUserId = if (currentUserId == userId) friendId else userId
            val profile = profiles[otherUserId] ?: return@mapNotNull null
            Friend(
                id = row.optString("id"),
                userId = userId,
                friendId = friendId,
                nickname = profile.nickname,
                email = profile.email,
                avatarUrl = profile.avatarUrl,
                status = row.optString("status").toFriendStatus(),
                permissionStatus = PermissionStatus.Pending,
                createdAt = row.optString("created_at").takeIf { it.isNotBlank() }
            )
        }
    }

    private suspend fun fetchUserProfiles(
        userIds: List<String>,
        accessToken: String
    ): List<UserProfile> {
        if (userIds.isEmpty()) return emptyList()
        val response = executeRemoteRequest(
            path = "/rest/v1/user_profiles?id=in.(${userIds.joinToString(",")})&select=id,email,nickname,avatar_url,push_token",
            method = "GET",
            body = null,
            accessToken = accessToken,
            prefer = "return=representation"
        )
        val rows = response.toJsonArray()
        return (0 until rows.length()).map { index ->
            val row = rows.getJSONObject(index)
            UserProfile(
                id = row.optString("id"),
                email = row.optString("email"),
                nickname = row.optString("nickname"),
                avatarUrl = row.optString("avatar_url").takeIf { it.isNotBlank() },
                pushToken = row.optString("push_token").takeIf { it.isNotBlank() }
            )
        }
    }

    private suspend fun searchUserProfiles(
        query: String,
        currentUserId: String,
        accessToken: String
    ): List<UserProfile> {
        val encodedQuery = URLEncoder.encode("*$query*", Charsets.UTF_8.name())
        val response = executeRemoteRequest(
            path = "/rest/v1/user_profiles?or=(email.ilike.$encodedQuery,nickname.ilike.$encodedQuery)&select=id,email,nickname,avatar_url,push_token&limit=20",
            method = "GET",
            body = null,
            accessToken = accessToken,
            prefer = "return=representation"
        )
        val rows = response.toJsonArray()
        return (0 until rows.length()).mapNotNull { index ->
            val row = rows.getJSONObject(index)
            val id = row.optString("id")
            if (id.isBlank() || id == currentUserId) return@mapNotNull null
            UserProfile(
                id = id,
                email = row.optString("email"),
                nickname = row.optString("nickname"),
                avatarUrl = row.optString("avatar_url").takeIf { it.isNotBlank() },
                pushToken = row.optString("push_token").takeIf { it.isNotBlank() }
            )
        }
    }

    private suspend fun executeRemoteRequest(
        path: String,
        method: String,
        body: JSONObject?,
        accessToken: String,
        prefer: String
    ): String = withContext(Dispatchers.IO) {
        check(config.isConfigured) {
            "Supabase 설정이 비어 있습니다. local.properties에 SUPABASE_URL과 SUPABASE_ANON_KEY를 입력해주세요."
        }
        val connection = (URL("${config.url.trimEnd('/')}$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = body != null
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", config.anonKey)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Prefer", prefer)
        }

        if (body != null) {
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }
        }

        val responseText = connection.readResponseText()
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(parseErrorMessage(responseText))
        }
        responseText
    }

    private fun String.toJsonArray(): JSONArray {
        return if (isBlank()) JSONArray() else JSONArray(this)
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
        if (responseText.isBlank()) return "친구 요청 처리에 실패했습니다."
        return runCatching {
            val json = JSONObject(responseText)
            listOf("message", "details", "hint", "code")
                .firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
        }.getOrNull() ?: "친구 요청 처리에 실패했습니다."
    }
}

private fun FriendStatus.toRemoteStatus(): String {
    return name.lowercase()
}

private fun String.toFriendStatus(): FriendStatus {
    return when (lowercase()) {
        "accepted" -> FriendStatus.Accepted
        "rejected" -> FriendStatus.Rejected
        "blocked" -> FriendStatus.Blocked
        else -> FriendStatus.Pending
    }
}

private const val MIN_SEARCH_QUERY_LENGTH = 2
