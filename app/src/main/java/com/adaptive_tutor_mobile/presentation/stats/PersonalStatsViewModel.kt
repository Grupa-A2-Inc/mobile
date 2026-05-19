package com.adaptive_tutor_mobile.presentation.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.CourseStats
import com.adaptive_tutor_mobile.domain.usecase.GetCourseStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonalStatsUiState(
    val stats: CourseStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PersonalStatsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCourseStatsUseCase: GetCourseStatsUseCase
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle["courseId"])

    private val _uiState = MutableStateFlow(PersonalStatsUiState())
    val uiState: StateFlow<PersonalStatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getCourseStatsUseCase(courseId)
                .onSuccess { courseStats ->
                    _uiState.update { it.copy(stats = courseStats, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.localizedMessage ?: "Eroare la încărcarea statisticilor"
                        )
                    }
                }
        }
    }
}
