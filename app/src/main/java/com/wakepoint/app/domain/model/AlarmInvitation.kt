package com.wakepoint.app.domain.model

/**
 * 친구가 보낸 목적지 알람 초대.
 * 서버에는 초대 설정값과 처리 상태만 저장한다. 대상자의 위치, 도착, 알람 울림 정보는 어디에도 두지 않는다.
 * docs/DESTINATION_ALARM_PRIVACY.md 참조.
 */
data class AlarmInvitation(
    val id: String,
    val requesterId: String,
    val targetId: String,
    val label: String,
    val targetAddress: String,
    val targetLat: Double,
    val targetLng: Double,
    val radiusKm: Double,
    val status: InvitationStatus,
    val expiresAt: String? = null,
    val createdAt: String? = null,
    val requesterProfile: UserProfile? = null,
    val targetProfile: UserProfile? = null
)

enum class InvitationStatus(private val value: String) {
    Pending("pending"),
    Accepted("accepted"),
    Rejected("rejected"),
    Expired("expired"),
    Cancelled("cancelled");

    fun toValue(): String = value

    companion object {
        fun fromValue(value: String?): InvitationStatus {
            val normalized = value?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.value == normalized } ?: Pending
        }
    }
}
