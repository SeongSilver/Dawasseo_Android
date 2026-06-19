package com.wakepoint.app.data.friend.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query(
        """
        SELECT * FROM friends
        ORDER BY
            CASE status
                WHEN 'Accepted' THEN 0
                WHEN 'Pending' THEN 1
                WHEN 'Blocked' THEN 2
                ELSE 3
            END,
            nickname ASC
        """
    )
    fun observeFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE status = 'Accepted' ORDER BY nickname ASC")
    fun observeAcceptedFriends(): Flow<List<FriendEntity>>

    @Upsert
    suspend fun upsertFriends(friends: List<FriendEntity>)

    @Query("DELETE FROM friends")
    suspend fun clearFriends()

    @Query("DELETE FROM friends WHERE id = :friendshipId")
    suspend fun deleteFriend(friendshipId: String)
}
