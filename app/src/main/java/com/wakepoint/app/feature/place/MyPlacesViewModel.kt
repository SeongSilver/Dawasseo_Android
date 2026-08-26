package com.wakepoint.app.feature.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.core.place.PlaceRules
import com.wakepoint.app.data.place.PlaceRepository
import com.wakepoint.app.domain.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MyPlacesUiState(
    /** 저장소 첫 방출 전 = true(빈 화면 깜빡임 방지). */
    val isLoading: Boolean = true,
    val places: List<Place> = emptyList(),
) {
    val homePlace: Place? = places.firstOrNull { it.isHome }
    val otherPlaces: List<Place> = places.filter { !it.isHome }
    val canAdd: Boolean = PlaceRules.canAddPlace(places.size)
    val slotText: String = "${places.size} / ${PlaceRules.MAX_PLACES}"
}

/** v2 STEP2 (2-1) 내 장소 목록. DAO 가 집 우선 정렬을 보장한다. */
@HiltViewModel
class MyPlacesViewModel @Inject constructor(
    placeRepository: PlaceRepository,
) : ViewModel() {

    val uiState: StateFlow<MyPlacesUiState> = placeRepository.observePlaces()
        .map { MyPlacesUiState(isLoading = false, places = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MyPlacesUiState(),
        )
}
