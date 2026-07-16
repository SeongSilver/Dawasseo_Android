package com.wakepoint.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.wakepoint.app.core.error.UserFacingError
import com.wakepoint.app.core.location.STANDARD_RADIUS_OPTIONS
import com.wakepoint.app.core.location.radiusOptionToKm
import com.wakepoint.app.data.alarm.AlarmRepository
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.location.KakaoLocalRepository
import com.wakepoint.app.data.location.PlaceSearchResult
import com.wakepoint.app.data.sound.SoundRepository
import com.wakepoint.app.domain.model.Alarm
import com.wakepoint.app.domain.model.SoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val searchErrorMessage: String? = null,
    val selectedSoundTitle: String = "기본 벨소리",
    val selectedSoundUri: String? = null,
    val selectedSoundIsCustom: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrate: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val authRepository: AuthRepository,
    private val kakaoLocalRepository: KakaoLocalRepository,
    private val soundRepository: SoundRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            soundRepository.observeSelectedDefaultSound().collect { sound ->
                _uiState.update { current ->
                    current.copy(
                        selectedSoundTitle = sound?.title ?: "기본 벨소리",
                        selectedSoundUri = sound?.uri,
                        selectedSoundIsCustom = sound?.soundType == SoundType.Custom
                    )
                }
            }
        }
    }

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

    fun updateVibrate(value: Boolean) {
        _uiState.update { it.copy(vibrate = value) }
    }

    fun updateSoundEnabled(value: Boolean) {
        _uiState.update { it.copy(soundEnabled = value) }
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
                            searchErrorMessage = UserFacingError.of(error)
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

    fun resolveTargetAddress(
        target: LatLng,
        fallback: String,
        onResolved: (String) -> Unit
    ) {
        viewModelScope.launch {
            val address = kakaoLocalRepository.resolveAddress(
                lat = target.latitude,
                lng = target.longitude
            ).getOrElse { fallback }
            onResolved(address.ifBlank { fallback })
        }
    }

    fun saveAlarm(
        target: LatLng,
        targetAddress: String,
        fallbackLabel: String
    ) {
        val state = _uiState.value
        // 별칭을 입력하지 않으면 해당 위치의 건물/도로명 주소를 별칭으로 사용한다.
        val label = state.alarmLabel.trim()
            .ifBlank { targetAddress.trim() }
            .ifBlank { fallbackLabel }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingAlarm = true,
                    saveErrorMessage = null,
                    saveSucceeded = false
                )
            }

            val result = runCatching {
                val session = authRepository.requireValidSession()
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
                        soundType = if (state.selectedSoundIsCustom) SoundType.Custom else SoundType.Default,
                        soundUri = state.selectedSoundUri,
                        soundEnabled = state.soundEnabled,
                        vibrate = state.vibrate
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
                            saveErrorMessage = UserFacingError.of(error)
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
            // 서버의 최신 알람(친구가 보낸 알람 포함)을 먼저 당겨와 추적 대상에 반영한다.
            runCatching { alarmRepository.refreshAlarms() }
            alarmRepository.syncLocationTracking()
        }
    }
}

private fun String.toRadiusKm(): Double = radiusOptionToKm(this)

const val DEFAULT_ALARM_RADIUS_OPTION = "500m"

/** 화면들이 참조하는 반경 옵션. 단일 진실 공급원(core/location)에 위임. */
val ALARM_RADIUS_OPTIONS = STANDARD_RADIUS_OPTIONS
