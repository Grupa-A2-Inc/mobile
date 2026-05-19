package com.adaptive_tutor_mobile.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.UserProfile
import com.adaptive_tutor_mobile.domain.usecase.ChangePasswordUseCase
import com.adaptive_tutor_mobile.domain.usecase.GetUserProfileUseCase
import com.adaptive_tutor_mobile.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    // organizationName vine din SessionStore (stocat la login),
    // nu din răspunsul GET /users/{id} care poate să nu îl returneze
    val organizationName: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateUserProfileUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val user = sessionStore.getUser() ?: return@launch
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                // Populăm organizationName imediat din SessionStore —
                // e disponibil chiar înainte ca request-ul API să termine
                organizationName = user.organizationName
            )
            getProfileUseCase(user.id)
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        // Dacă API-ul returnează și el organizationName, îl preferăm;
                        // altfel rămâne cel din SessionStore
                        organizationName = profile.organizationName
                            ?: user.organizationName,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Eroare la încărcarea profilului"
                    )
                }
        }
    }

    fun updateProfile(email: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            val currentUser = sessionStore.getUser() ?: return@launch
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )
            updateProfileUseCase(currentUser.id, email, firstName, lastName, organizationId = null)
                .onSuccess { updatedProfile ->
                    sessionStore.saveUser(
                        currentUser.copy(
                            firstName        = updatedProfile.firstName,
                            lastName         = updatedProfile.lastName,
                            email            = updatedProfile.email,
                            organizationName = updatedProfile.organizationName
                                ?: currentUser.organizationName
                        )
                    )
                    _uiState.value = _uiState.value.copy(
                        profile = updatedProfile,
                        // Păstrăm organizationName existent dacă API-ul nu îl returnează
                        organizationName = updatedProfile.organizationName
                            ?: _uiState.value.organizationName,
                        isSaving = false,
                        successMessage = "Profil actualizat cu succes"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Eroare la salvarea profilului"
                    )
                }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String
    ) {
        if (newPassword.length < 8) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Parola nouă trebuie să aibă minim 8 caractere"
            )
            return
        }
        if (newPassword != newPasswordConfirm) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Parolele nu coincid"
            )
            return
        }
        viewModelScope.launch {
            val userId = sessionStore.getUser()?.id ?: return@launch
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )
            changePasswordUseCase(userId, currentPassword, newPassword, newPasswordConfirm)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        successMessage = "Parola a fost schimbată cu succes"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Eroare la schimbarea parolei"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}