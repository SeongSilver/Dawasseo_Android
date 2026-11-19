package com.wakepoint.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val isEditingNickname: Boolean = false,
    val nicknameDraft: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.fetchCurrentProfile()
            _uiState.update { current ->
                result.fold(
                    onSuccess = { profile ->
                        current.copy(profile = profile, isLoading = false)
                    },
                    onFailure = { error ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "프로필을 불러오지 못했습니다."
                        )
                    }
                )
            }
        }
    }

    fun startEditingNickname() {
        _uiState.update {
            it.copy(
                isEditingNickname = true,
                nicknameDraft = it.profile?.nickname.orEmpty(),
                errorMessage = null
            )
        }
    }

    fun updateNicknameDraft(value: String) {
        _uiState.update { it.copy(nicknameDraft = value, errorMessage = null) }
    }

    fun cancelEditingNickname() {
        _uiState.update { it.copy(isEditingNickname = false, nicknameDraft = "") }
    }

    fun saveNickname() {
        val draft = _uiState.value.nicknameDraft.trim()
        if (draft.isBlank()) {
            _uiState.update { it.copy(errorMessage = "닉네임을 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = authRepository.updateNickname(draft)
            _uiState.update { current ->
                result.fold(
                    onSuccess = { profile ->
                        current.copy(
                            profile = profile,
                            isSaving = false,
                            isEditingNickname = false,
                            nicknameDraft = ""
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "닉네임을 저장하지 못했습니다."
                        )
                    }
                )
            }
        }
    }
}
