package com.wakepoint.app.feature.place

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.core.error.UserFacingError
import com.wakepoint.app.core.place.PlaceRules
import com.wakepoint.app.data.location.KakaoLocalRepository
import com.wakepoint.app.data.location.PlaceSearchResult
import com.wakepoint.app.data.place.PlaceRepository
import com.wakepoint.app.domain.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaceEditUiState(
    val isLoading: Boolean = true,
    /** 편집 대상(신규면 null). */
    val original: Place? = null,
    /** 집 고정 슬롯 편집/등록 여부(이름 고정·삭제 불가). */
    val isHome: Boolean = false,
    /** 공개 ON 시 기본 제안할 공유 이름("장소 N"). */
    val suggestedSharedName: String = "장소 1",
    val searchQuery: String = "",
    val searchResults: List<PlaceSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchMessage: String? = null,
    val isSaving: Boolean = false,
    /** 저장/삭제 완료 → 화면 닫기 신호. */
    val done: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * v2 STEP2 (2-2/2-3/2-4) 장소 등록·편집. 주소는 카카오 검색으로만 선택되고
 * 저장 후에는 잠긴다(주소는 어떤 경우에도 친구에게 비공개).
 */
@HiltViewModel
class PlaceEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val placeRepository: PlaceRepository,
    private val kakaoLocalRepository: KakaoLocalRepository,
) : ViewModel() {

    private val placeId: String = savedStateHandle.get<String>("placeId").orEmpty()
    private val homeArg: Boolean = savedStateHandle.get<String>("home") == "true"

    private val _uiState = MutableStateFlow(PlaceEditUiState())
    val uiState: StateFlow<PlaceEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val original = if (placeId.isNotBlank()) placeRepository.getPlace(placeId) else null
            val allPlaces = placeRepository.observePlaces().first()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    original = original,
                    isHome = original?.isHome ?: homeArg,
                    suggestedSharedName = PlaceRules.suggestSharedName(allPlaces.map { p -> p.sharedName }),
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchAddress() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank() || _uiState.value.isSearching) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchMessage = null) }
            val result = kakaoLocalRepository.searchKeyword(query)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { places ->
                        state.copy(
                            isSearching = false,
                            searchResults = places,
                            searchMessage = if (places.isEmpty()) "검색 결과가 없어요" else null,
                        )
                    },
                    onFailure = { error ->
                        state.copy(isSearching = false, searchMessage = UserFacingError.of(error))
                    },
                )
            }
        }
    }

    fun clearSearchResults() {
        _uiState.update { it.copy(searchResults = emptyList(), searchQuery = "") }
    }

    fun save(myName: String, isPublic: Boolean, sharedName: String, address: String, latitude: Double, longitude: Double) {
        val state = _uiState.value
        val place = Place(
            id = state.original?.id ?: UUID.randomUUID().toString(),
            myName = if (state.isHome) PlaceRules.HOME_NAME else myName.trim(),
            address = address,
            latitude = latitude,
            longitude = longitude,
            isPublic = isPublic,
            sharedName = if (isPublic) sharedName.trim() else null,
            isHome = state.isHome,
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = runCatching { placeRepository.savePlace(place) }
            _uiState.update {
                it.copy(
                    isSaving = false,
                    done = result.isSuccess,
                    errorMessage = result.exceptionOrNull()?.let { e -> UserFacingError.of(e) },
                )
            }
        }
    }

    fun delete() {
        val target = _uiState.value.original ?: return
        if (!PlaceRules.isDeletable(target)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = runCatching { placeRepository.deletePlace(target.id) }
            _uiState.update {
                it.copy(
                    isSaving = false,
                    done = result.isSuccess,
                    errorMessage = result.exceptionOrNull()?.let { e -> UserFacingError.of(e) },
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
