package com.wakepoint.app.data.place.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakepoint.app.domain.model.Place

/**
 * 내 장소 로컬 테이블(v2 STEP2). 기기 로컬 전용 — 주소·좌표는 서버로도 친구에게도
 * 나가지 않는다(친구 알람 전송 시 공유 이름만 노출, 좌표는 알람 생성 시점에만 사용).
 */
@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "my_name") val myName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "is_public", defaultValue = "0") val isPublic: Boolean = false,
    @ColumnInfo(name = "shared_name") val sharedName: String? = null,
    @ColumnInfo(name = "is_home", defaultValue = "0") val isHome: Boolean = false,
)

internal fun PlaceEntity.toDomain(): Place = Place(
    id = id,
    myName = myName,
    address = address,
    latitude = latitude,
    longitude = longitude,
    isPublic = isPublic,
    sharedName = sharedName,
    isHome = isHome,
)

internal fun Place.toEntity(): PlaceEntity = PlaceEntity(
    id = id,
    myName = myName,
    address = address,
    latitude = latitude,
    longitude = longitude,
    isPublic = isPublic,
    sharedName = sharedName,
    isHome = isHome,
)
