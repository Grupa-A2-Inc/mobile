package com.adaptive_tutor_mobile.presentation.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.test.AttemptHistory
import com.adaptive_tutor_mobile.domain.model.test.BestAttempt
import com.adaptive_tutor_mobile.domain.usecase.test.GetTestAttemptsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestAttemptsUiState(
    val attempts: List<AttemptHistory> = emptyList(),
    val bestAttempt: BestAttempt? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TestAttemptsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestAttemptsUseCase: GetTestAttemptsUseCase
) : ViewModel() {

    private val testId: String = checkNotNull(savedStateHandle["testId"])

    private val _uiState = MutableStateFlow(TestAttemptsUiState())
    val uiState: StateFlow<TestAttemptsUiState> = _uiState.asStateFlow()

    init {
        loadAttempts()
    }

    fun loadAttempts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTestAttemptsUseCase(testId)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            attempts = result.attempts.sortedByDescending { a -> a.date },
                            bestAttempt = result.bestAttempt,
                            isLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.localizedMessage ?: "Eroare la încărcarea încercărilor"
                        )
                    }
                }
        }
    }
}
