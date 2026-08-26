package com.wakepoint.app.core.place

import com.wakepoint.app.domain.model.Place

/**
 * v2 STEP2 내 장소 규칙(명세 2-1~2-4)의 순수 로직. UI/영속성과 분리해 결정적으로 테스트한다.
 *
 * - 슬롯: 집(고정) 포함 최대 [MAX_PLACES]개.
 * - 저장: 내 이름 필수, 공개 시 친구가 보는 이름도 필수.
 * - 집: 이름 "집" 고정, 삭제 불가.
 * - 공유 이름 제안: 주소가 드러나지 않는 중립 이름("장소 N").
 */
object PlaceRules {

    const val MAX_PLACES = 4

    /** 집 고정 슬롯의 표시 이름. */
    const val HOME_NAME = "집"

    /** 슬롯이 남아 있어야 추가 가능. */
    fun canAddPlace(currentCount: Int): Boolean = currentCount < MAX_PLACES

    /**
     * 저장 버튼 활성 조건: 내 이름 필수. 공개(ON)면 친구가 보는 이름도 필수(명세 2-3).
     * 집 슬롯은 이름이 고정이라 내 이름 검사를 건너뛴다.
     */
    fun isSavable(myName: String, isPublic: Boolean, sharedName: String?, isHome: Boolean = false): Boolean {
        val nameOk = isHome || myName.isNotBlank()
        val sharedOk = !isPublic || !sharedName.isNullOrBlank()
        return nameOk && sharedOk
    }

    /** 집 고정 슬롯은 삭제할 수 없다(명세 2-4는 일반 장소에만 해당). */
    fun isDeletable(place: Place): Boolean = !place.isHome

    /**
     * 공개 시 기본 공유 이름 제안: 이미 쓰는 "장소 N"과 겹치지 않는 가장 작은 N.
     * 친구에게 주소·실명이 드러나지 않는 중립 이름을 유도한다.
     */
    fun suggestSharedName(existingSharedNames: Collection<String?>): String {
        val used = existingSharedNames.filterNotNull().toSet()
        var n = 1
        while ("장소 $n" in used) n++
        return "장소 $n"
    }

    /** 목록 정렬: 집 먼저, 그 다음 내 이름 가나다순(명세 2-1 집 고정 슬롯 최상단). */
    fun sortForList(places: List<Place>): List<Place> =
        places.sortedWith(compareByDescending<Place> { it.isHome }.thenBy { it.myName })
}
