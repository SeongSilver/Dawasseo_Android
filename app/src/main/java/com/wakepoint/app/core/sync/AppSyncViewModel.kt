package com.wakepoint.app.core.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.data.alarm.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * 앱 스코프의 동기화 트리거. 앱이 포그라운드로 돌아올 때(onResume) 서버의 최신 알람을
 * 로컬에 반영해, 다른 탭에 머무는 동안 도착한 친구 알람/타 기기 변경을 뒤늦지 않게 가져온다.
 */
@HiltViewModel
class AppSyncViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {
    fun refresh() {
        viewModelScope.launch {
            runCatching { alarmRepository.refreshAlarms() }
        }
    }
}
