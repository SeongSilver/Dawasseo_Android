package com.wakepoint.app.data.place

import com.wakepoint.app.domain.model.Place
import kotlinx.coroutines.flow.Flow

/**
 * 내 장소 로컬 저장소(v2 STEP2). 기기 로컬 전용 — 원격 동기화 없음.
 * (Supabase 신규 테이블 프로비저닝 권한 확보 시 공유 이름만 동기화하는 후속 예정.)
 */
interface PlaceRepository {
    fun observePlaces(): Flow<List<Place>>
    suspend fun getPlace(id: String): Place?
    suspend fun savePlace(place: Place)
    suspend fun deletePlace(id: String)
}
