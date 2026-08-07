package com.wakepoint.app.domain.model

data class Alarm(
    val id: String,
    val ownerId: String,
    val createdBy: String,
    val label: String,
    val targetLat: Double,
    val targetLng: Double,
    val targetAddress: String,
    val radiusKm: Double,
    val isActive: Boolean,
    val soundType: SoundType,
    val soundUri: String?,
    val soundEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val isFavorite: Boolean = false,
    /**
     * 계속 유지(반복) 알람 여부. true면 한 번 울려도 비활성화하지 않고,
     * 설정한 반경을 벗어난 뒤 다시 진입하면 재발동한다. (기기 로컬 전용 설정)
     */
    val repeating: Boolean = false,
    /** 로컬에는 저장됐지만 아직 서버에 반영되지 못한 상태(오프라인/일시 오류). true면 다음 동기화 때 재전송. */
    val pendingSync: Boolean = false
)

enum class SoundType {
    Default,
    Custom
}
