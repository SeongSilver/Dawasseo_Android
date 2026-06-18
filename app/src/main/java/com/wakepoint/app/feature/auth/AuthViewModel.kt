package com.wakepoint.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakepoint.app.data.auth.AuthRepository
import com.wakepoint.app.data.auth.AuthSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val session: AuthSession? = null,
    val loginEmail: String = "",
    val loginPassword: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val signUpPasswordConfirm: String = "",
    val signUpName: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
) {
    val isAuthenticated: Boolean = session != null
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authSession.collect { session ->
                val validSession = if (session != null) {
                    authRepository.refreshSessionIfNeeded().getOrNull()
                } else {
                    null
                }
                _uiState.update {
                    it.copy(
                        isCheckingSession = false,
                        session = validSession
                    )
                }
            }
        }
    }

    fun updateLoginEmail(value: String) {
        _uiState.update { it.copy(loginEmail = value, errorMessage = null, infoMessage = null) }
    }

    fun updateLoginPassword(value: String) {
        _uiState.update { it.copy(loginPassword = value, errorMessage = null, infoMessage = null) }
    }

    fun updateSignUpEmail(value: String) {
        _uiState.update { it.copy(signUpEmail = value, errorMessage = null, infoMessage = null) }
    }

    fun updateSignUpPassword(value: String) {
        _uiState.update { it.copy(signUpPassword = value, errorMessage = null, infoMessage = null) }
    }

    fun updateSignUpPasswordConfirm(value: String) {
        _uiState.update { it.copy(signUpPasswordConfirm = value, errorMessage = null, infoMessage = null) }
    }

    fun updateSignUpName(value: String) {
        _uiState.update { it.copy(signUpName = value, errorMessage = null, infoMessage = null) }
    }

    fun signIn() {
        val state = _uiState.value
        if (state.loginEmail.isBlank() || state.loginPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "이메일과 비밀번호를 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            val result = authRepository.signInWithEmail(
                email = state.loginEmail.trim(),
                password = state.loginPassword
            )
            _uiState.update { current ->
                result.fold(
                    onSuccess = { outcome ->
                        current.copy(
                            isSubmitting = false,
                            infoMessage = outcome.message
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "로그인에 실패했습니다."
                        )
                    }
                )
            }
        }
    }

    fun signUp() {
        val state = _uiState.value
        when {
            state.signUpEmail.isBlank() || state.signUpPassword.isBlank() || state.signUpName.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "이름, 이메일, 비밀번호를 입력해주세요.") }
                return
            }
            state.signUpPassword != state.signUpPasswordConfirm -> {
                _uiState.update { it.copy(errorMessage = "비밀번호가 서로 일치하지 않습니다.") }
                return
            }
            state.signUpPassword.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "비밀번호는 6자 이상이어야 합니다.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            val result = authRepository.signUpWithEmail(
                email = state.signUpEmail.trim(),
                password = state.signUpPassword,
                nickname = state.signUpName.trim()
            )
            _uiState.update { current ->
                result.fold(
                    onSuccess = { outcome ->
                        current.copy(
                            isSubmitting = false,
                            infoMessage = outcome.message
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "회원가입에 실패했습니다."
                        )
                    }
                )
            }
        }
    }

    fun kakaoOAuthUrl(): String? {
        return runCatching {
            authRepository.kakaoOAuthUrl()
        }.onFailure { error ->
            _uiState.update {
                it.copy(errorMessage = error.message ?: "카카오 로그인을 시작하지 못했습니다.")
            }
        }.getOrNull()
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
