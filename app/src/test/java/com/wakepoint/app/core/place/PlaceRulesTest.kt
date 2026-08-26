package com.wakepoint.app.core.place

import com.wakepoint.app.domain.model.Place
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceRulesTest {

    private fun place(
        myName: String,
        isHome: Boolean = false,
        isPublic: Boolean = false,
        sharedName: String? = null,
    ) = Place(
        id = myName, myName = myName, address = "서울", latitude = 37.0, longitude = 127.0,
        isPublic = isPublic, sharedName = sharedName, isHome = isHome,
    )

    // --- 슬롯 ---

    @Test
    fun `4개 미만이면 추가 가능`() {
        assertTrue(PlaceRules.canAddPlace(0))
        assertTrue(PlaceRules.canAddPlace(3))
        assertFalse(PlaceRules.canAddPlace(4))
    }

    // --- 저장 규칙 ---

    @Test
    fun `내 이름 없으면 저장 불가`() {
        assertFalse(PlaceRules.isSavable(myName = " ", isPublic = false, sharedName = null))
    }

    @Test
    fun `비공개면 공유 이름 없이 저장 가능`() {
        assertTrue(PlaceRules.isSavable(myName = "여자친구집", isPublic = false, sharedName = null))
    }

    @Test
    fun `공개면 공유 이름 필수`() {
        assertFalse(PlaceRules.isSavable(myName = "여자친구집", isPublic = true, sharedName = " "))
        assertTrue(PlaceRules.isSavable(myName = "여자친구집", isPublic = true, sharedName = "장소 2"))
    }

    @Test
    fun `집 슬롯은 이름 검사 없이 저장 가능`() {
        assertTrue(PlaceRules.isSavable(myName = "", isPublic = false, sharedName = null, isHome = true))
    }

    // --- 삭제 규칙 ---

    @Test
    fun `집은 삭제 불가 일반 장소는 삭제 가능`() {
        assertFalse(PlaceRules.isDeletable(place("집", isHome = true)))
        assertTrue(PlaceRules.isDeletable(place("여자친구집")))
    }

    // --- 공유 이름 제안 ---

    @Test
    fun `공유 이름은 겹치지 않는 가장 작은 번호`() {
        assertEquals("장소 1", PlaceRules.suggestSharedName(emptyList()))
        assertEquals("장소 2", PlaceRules.suggestSharedName(listOf("장소 1")))
        assertEquals("장소 2", PlaceRules.suggestSharedName(listOf("장소 1", "장소 3", null)))
    }

    // --- 정렬 ---

    @Test
    fun `집이 최상단 나머지는 이름순`() {
        val sorted = PlaceRules.sortForList(
            listOf(place("회사"), place("집", isHome = true), place("본가"))
        )
        assertEquals(listOf("집", "본가", "회사"), sorted.map { it.myName })
    }
}
