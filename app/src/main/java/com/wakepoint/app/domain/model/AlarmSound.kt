package com.wakepoint.app.domain.model

/**
 * 알람에 사용할 수 있는 하나의 사운드 항목.
 *
 * @property id 고유 식별자. 번들 사운드는 "bundled_<rawName>", 커스텀 사운드는 파일 이름.
 * @property uri 재생 가능한 URI 문자열. 번들은 android.resource://, 커스텀은 파일 절대 경로.
 * @property kind 사운드 출처 구분.
 */
data class AlarmSound(
    val id: String,
    val title: String,
    val subtitle: String,
    val uri: String,
    val kind: SoundKind
) {
    /** Alarm 도메인 모델에 저장할 사운드 타입. */
    val soundType: SoundType
        get() = if (kind == SoundKind.Bundled) SoundType.Default else SoundType.Custom
}

enum class SoundKind {
    /** res/raw 에 포함된 기본 제공 사운드. */
    Bundled,

    /** 사용자가 앱에서 직접 녹음한 사운드. */
    Recorded,

    /** 사용자가 기기에서 가져온 오디오 파일. */
    Imported
}
