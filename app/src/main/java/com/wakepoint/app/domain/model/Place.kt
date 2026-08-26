package com.wakepoint.app.domain.model

/**
 * v2 STEP2 내 장소. 이름이 이원화된다:
 * [myName] 내가 보는 이름(비공개) ↔ [sharedName] 친구가 보는 이름(공개 시 필수).
 * 주소·좌표는 어떤 경우에도 친구에게 비공개다(친구는 [sharedName] 만 본다).
 *
 * @param isHome "집" 고정 슬롯 — 이름 고정, 삭제 불가(막차 알람의 도착지로 쓰인다).
 */
data class Place(
    val id: String,
    val myName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isPublic: Boolean = false,
    val sharedName: String? = null,
    val isHome: Boolean = false,
)
