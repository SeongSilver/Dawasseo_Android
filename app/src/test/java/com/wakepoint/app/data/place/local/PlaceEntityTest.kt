package com.wakepoint.app.data.place.local

import com.wakepoint.app.domain.model.Place
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceEntityTest {

    @Test
    fun `도메인 왕복 변환에서 필드가 보존된다`() {
        val place = Place(
            id = "p1",
            myName = "여자친구집",
            address = "서울 마포구 어딘가 123",
            latitude = 37.5551,
            longitude = 126.9236,
            isPublic = true,
            sharedName = "장소 2",
            isHome = false,
        )
        assertEquals(place, place.toEntity().toDomain())
    }

    @Test
    fun `집 고정 슬롯도 왕복 보존`() {
        val home = Place(
            id = "home",
            myName = "집",
            address = "서울 강남구",
            latitude = 37.49,
            longitude = 127.02,
            isHome = true,
        )
        assertEquals(home, home.toEntity().toDomain())
    }
}
