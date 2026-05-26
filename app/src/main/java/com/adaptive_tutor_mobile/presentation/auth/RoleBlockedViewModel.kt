package com.adaptive_tutor_mobile.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val DEFAULT_LOGOUT_ERROR = "Nu te-am putut deconecta. Încearcă din nou."

data class RoleBlockedUiState(
    val isLoggingOut: Boolean = false,
    val navigateToLogin: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RoleBlockedViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoleBlockedUiState())
    val uiState: StateFlow<RoleBlockedUiState> = _uiState.asStateFlow()

    fun logout() {
        if (_uiState.value.isLoggingOut) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true, errorMessage = null)

            logoutUseCase()
                .onSuccess {
                    _uiState.value = RoleBlockedUiState(navigateToLogin = true)
                }
                .onFailure { error ->
                    _uiState.value = RoleBlockedUiState(
                        isLoggingOut = false,
                        errorMessage = error.message ?: DEFAULT_LOGOUT_ERROR
                    )
                }
        }
    }
}
