package com.wakepoint.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.location.KakaoLocalRepository
import com.wakepoint.app.data.location.PlaceSearchResult
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val alarmLabel: String = "",
    val radiusOption: String = DEFAULT_ALARM_RADIUS_OPTION,
    val isSavingAlarm: Boolean = false,
    val saveErrorMessage: String? = null,
    val saveSucceeded: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<PlaceSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchErrorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val authRepository: AuthRepository,
    private val kakaoLocalRepository: KakaoLocalRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateAlarmLabel(value: String) {
        _uiState.update {
            it.copy(
                alarmLabel = value,
                saveErrorMessage = null
            )
        }
    }

    fun updateRadiusOption(value: String) {
        _uiState.update {
            it.copy(
                radiusOption = value,
                saveErrorMessage = null
            )
        }
    }

    fun updateSearchQuery(value: String) {
        _uiState.update {
            it.copy(
                searchQuery = value,
                searchErrorMessage = null
            )
        }
    }

    fun searchPlaces() {
        val query = _uiState.value.searchQuery
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSearching = true,
                    searchErrorMessage = null
                )
            }
            val result = kakaoLocalRepository.searchKeyword(query)
            _uiState.update { current ->
                result.fold(
                    onSuccess = { places ->
                        current.copy(
                            isSearching = false,
                            searchResults = places,
                            searchErrorMessage = if (places.isEmpty()) "검색 결과가 없습니다." else null
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSearching = false,
                            searchErrorMessage = error.message ?: "장소 검색에 실패했습니다."
                        )
                    }
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                searchErrorMessage = null
            )
        }
    }

    fun saveAlarm(
        target: LatLng,
        targetAddress: String,
        fallbackLabel: String
    ) {
        val state = _uiState.value
        val label = state.alarmLabel.trim().ifBlank { fallbackLabel }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingAlarm = true,
                    saveErrorMessage = null,
                    saveSucceeded = false
                )
            }

            val result = runCatching {
                val session = authRepository.authSession.first()
                    ?: error("로그인이 필요합니다.")
                alarmRepository.saveAlarm(
                    Alarm(
                        id = UUID.randomUUID().toString(),
                        ownerId = session.userId,
                        createdBy = session.userId,
                        label = label,
                        targetLat = target.latitude,
                        targetLng = target.longitude,
                        targetAddress = targetAddress,
                        radiusKm = state.radiusOption.toRadiusKm(),
                        isActive = true,
                        triggeredAt = null,
                        soundType = SoundType.Default,
                        soundUri = null
                    )
                )
            }

            _uiState.update { current ->
                result.fold(
                    onSuccess = {
                        current.copy(
                            alarmLabel = "",
                            isSavingAlarm = false,
                            saveSucceeded = true
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSavingAlarm = false,
                            saveErrorMessage = error.message ?: "알람 저장에 실패했습니다."
                        )
                    }
                )
            }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSucceeded = false) }
    }

    fun syncLocationTracking() {
        viewModelScope.launch {
            alarmRepository.syncLocationTracking()
        }
    }
}

private fun String.toRadiusKm(): Double {
    val radiusKm = when {
        endsWith("km") -> removeSuffix("km").toDoubleOrNull() ?: 0.5
        endsWith("m") -> removeSuffix("m").toDoubleOrNull()?.div(1000.0) ?: 0.5
        else -> 0.5
    }
    return radiusKm.coerceIn(MIN_ALARM_RADIUS_KM, MAX_ALARM_RADIUS_KM)
}

const val DEFAULT_ALARM_RADIUS_OPTION = "500m"
const val MIN_ALARM_RADIUS_KM = 0.1
const val MAX_ALARM_RADIUS_KM = 50.0

val ALARM_RADIUS_OPTIONS = listOf("100m", "300m", "500m", "1km", "3km", "10km", "50km")
