package com.wakepoint.app.feature.v2.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * v2 UI 모델의 명세 불변식 잠금(P18 TDD).
 * 근거: design/v2-spec.dc.html (1d·2b·3a·3f·4a), docs/v2-decisions.md.
 * 표현 전용 모델이라 라벨·기본값·상태 집합이 곧 명세다.
 */
class V2ModelsTest {

    // ---- AlarmKind (1d 목록 타입 배지) ----

    @Test
    fun `알람 종류는 위치와 시간 두 가지이며 라벨이 고정된다`() {
        assertEquals(listOf("위치", "시간"), AlarmKind.entries.map { it.label })
    }

    // ---- V2Place (2b 장소 공개/이름 이원화) ----

    @Test
    fun `장소 기본값은 비공개이며 공유 이름과 고정 슬롯이 없다`() {
        val place = V2Place(myName = "회사")
        assertFalse(place.isPublic)
        assertNull(place.sharedName)
        assertFalse(place.isFixed)
    }

    @Test
    fun `공개 장소는 친구에게 보일 공유 이름을 가질 수 있다 - 주소는 모델에 존재하지 않는다`() {
        val place = V2Place(myName = "본가", isPublic = true, sharedName = "장소 2")
        assertEquals("장소 2", place.sharedName)
        // 주소 필드가 애초에 없어 친구에게 노출될 수 없음(설계상 비공개 보장)
        val fieldNames = V2Place::class.java.declaredFields.map { it.name }
        assertFalse("주소류 필드가 모델에 없어야 한다", fieldNames.any { it.contains("address", ignoreCase = true) })
    }

    // ---- FriendPermission (3a 대리설정 권한 3상태) ----

    @Test
    fun `친구 대리설정 권한은 가능-대기-거절 3상태다`() {
        assertEquals(
            listOf("대리 설정 가능", "권한 대기", "거절됨"),
            FriendPermission.entries.map { it.label }
        )
    }

    // ---- SentAlarmStatus (3f 발신자에겐 2단계까지만) ----

    @Test
    fun `보낸 알람 상태는 전송됨-기기등록됨 2단계뿐이다`() {
        assertEquals(2, SentAlarmStatus.entries.size)
        assertEquals(listOf("전송됨", "기기 등록됨"), SentAlarmStatus.entries.map { it.label })
    }

    // ---- LastTrainLevel (4a 막차 3단계) ----

    @Test
    fun `막차 단계는 여유-임박-아슬 순서의 3단계다`() {
        assertEquals(
            listOf(LastTrainLevel.RELAXED, LastTrainLevel.IMMINENT, LastTrainLevel.CRITICAL),
            LastTrainLevel.entries.toList()
        )
    }

    // ---- V2AlarmItem 기본값 ----

    @Test
    fun `알람 아이템 기본값은 활성-비즐겨찾기-비과거-친구설정없음`() {
        val item = V2AlarmItem(name = "집", kind = AlarmKind.LOCATION, meta = "반경 500m")
        assertEquals(true, item.enabled)
        assertEquals(false, item.favorite)
        assertEquals(false, item.past)
        assertNull(item.setByFriendName)
    }
}
