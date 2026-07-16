package com.wakepoint.app.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wakepoint.app.data.alarm.AlarmRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 주기적으로 서버의 최신 알람(친구 발신/타 기기 변경)을 로컬에 반영하고 위치 추적을 재동기화한다.
 * FCM 실시간 수신·앱 resume 새로고침을 보완하는 백업 경로(앱을 한동안 열지 않아도 catch-up).
 * 로그아웃/미설정 상태면 refreshAlarms 가 그대로 no-op 으로 반환된다.
 */
@HiltWorker
class AlarmWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val alarmRepository: AlarmRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        return runCatching { alarmRepository.refreshAlarms() }
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
    }
}
