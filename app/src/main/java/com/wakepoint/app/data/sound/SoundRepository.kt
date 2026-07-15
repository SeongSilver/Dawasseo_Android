package com.wakepoint.app.data.sound

import android.net.Uri
import com.wakepoint.app.domain.model.AlarmSound
import kotlinx.coroutines.flow.Flow

interface SoundRepository {
    /** 번들(res/raw) + 사용자 녹음/가져온 사운드 전체 목록. */
    fun observeSounds(): Flow<List<AlarmSound>>

    /** 새 알람에 사용할 전역 기본 사운드. 없으면 목록의 첫 항목으로 폴백. */
    fun observeSelectedDefaultSound(): Flow<AlarmSound?>

    /** 전역 기본 사운드를 지정. */
    suspend fun setDefaultSound(sound: AlarmSound)

    /** 기기에서 오디오 파일을 가져와 내부 저장소에 복사. */
    suspend fun importSound(uri: Uri): Result<AlarmSound>

    /** 녹음을 시작(임시 파일에 기록). */
    fun startRecording(): Result<Unit>

    /** 녹음을 종료하고 임시 파일을 확정. */
    fun stopRecording(): Result<Unit>

    /** 확정된 임시 녹음을 카탈로그에 저장. */
    suspend fun saveRecordedSound(): Result<AlarmSound>

    /** 진행 중/확정된 녹음을 폐기. */
    fun discardRecording()

    /** 녹음/가져온 사운드를 삭제(번들 사운드는 삭제 불가). */
    suspend fun deleteSound(sound: AlarmSound): Result<Unit>
}
