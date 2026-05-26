package com.adaptive_tutor_mobile.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.data.remote.dto.RegisterRequest
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.auth.User
import com.adaptive_tutor_mobile.domain.model.auth.UserRole
import com.adaptive_tutor_mobile.domain.usecase.auth.CheckUserRoleUseCase
import com.adaptive_tutor_mobile.domain.usecase.auth.ForgotPasswordUseCase
import com.adaptive_tutor_mobile.domain.usecase.auth.LoginUseCase
import com.adaptive_tutor_mobile.domain.usecase.auth.LogoutUseCase
import com.adaptive_tutor_mobile.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_ERROR_MESSAGE = "Eroare necunoscută"

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object ForgotPasswordSent : AuthUiState()
}

enum class AuthNavigationTarget {
    STUDENT_HOME,
    ROLE_BLOCKED
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val checkUserRoleUseCase: CheckUserRoleUseCase,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _navigationTarget = MutableStateFlow<AuthNavigationTarget?>(null)
    val navigationTarget: StateFlow<AuthNavigationTarget?> = _navigationTarget.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUser.value = sessionStore.getUser()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                    _navigationTarget.value = navigationTargetFor(checkUserRoleUseCase())
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: DEFAULT_ERROR_MESSAGE) }
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registerUseCase(request)
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                    _navigationTarget.value = navigationTargetFor(checkUserRoleUseCase())
                }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: DEFAULT_ERROR_MESSAGE) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            logoutUseCase()
            _currentUser.value = null
            _uiState.value = AuthUiState.Idle
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            forgotPasswordUseCase(email)
                .onSuccess { _uiState.value = AuthUiState.ForgotPasswordSent }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: DEFAULT_ERROR_MESSAGE) }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun consumeNavigation() {
        _navigationTarget.value = null
    }

    fun refreshUser() {
        viewModelScope.launch {
            _currentUser.value = sessionStore.getUser()
        }
    }

    private fun navigationTargetFor(role: UserRole): AuthNavigationTarget =
        if (role == UserRole.STUDENT) {
            AuthNavigationTarget.STUDENT_HOME
        } else {
            AuthNavigationTarget.ROLE_BLOCKED
        }
}
