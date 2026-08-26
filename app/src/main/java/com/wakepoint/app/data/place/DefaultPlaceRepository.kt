package com.wakepoint.app.data.place

import com.wakepoint.app.data.place.local.PlaceDao
import com.wakepoint.app.data.place.local.toDomain
import com.wakepoint.app.data.place.local.toEntity
import com.wakepoint.app.domain.model.Place
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultPlaceRepository @Inject constructor(
    private val placeDao: PlaceDao,
) : PlaceRepository {

    override fun observePlaces(): Flow<List<Place>> =
        placeDao.observePlaces().map { list -> list.map { it.toDomain() } }

    override suspend fun getPlace(id: String): Place? = placeDao.get(id)?.toDomain()

    override suspend fun savePlace(place: Place) {
        placeDao.upsert(place.toEntity())
    }

    override suspend fun deletePlace(id: String) {
        placeDao.delete(id)
    }
}
