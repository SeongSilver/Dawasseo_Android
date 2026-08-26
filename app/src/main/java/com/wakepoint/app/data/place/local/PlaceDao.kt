package com.wakepoint.app.data.place.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places ORDER BY is_home DESC, my_name ASC")
    fun observePlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    suspend fun get(id: String): PlaceEntity?

    @Query("SELECT COUNT(*) FROM places")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(entity: PlaceEntity)

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun delete(id: String)
}
