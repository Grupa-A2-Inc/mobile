package com.adaptive_tutor_mobile.presentation.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.EnrolledCourse
import com.adaptive_tutor_mobile.domain.usecase.GetEnrolledCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EnrolledCoursesUiState {
    object Loading : EnrolledCoursesUiState()
    data class Success(val courses: List<EnrolledCourse>) : EnrolledCoursesUiState()
    data class Error(val message: String) : EnrolledCoursesUiState()
}

@HiltViewModel
class EnrolledCoursesViewModel @Inject constructor(
    private val getEnrolledCoursesUseCase: GetEnrolledCoursesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EnrolledCoursesUiState>(EnrolledCoursesUiState.Loading)
    val uiState: StateFlow<EnrolledCoursesUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
    }

    fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = EnrolledCoursesUiState.Loading
            getEnrolledCoursesUseCase()
                .onSuccess { courses ->
                    _uiState.value = EnrolledCoursesUiState.Success(courses)
                }
                .onFailure { e ->
                    _uiState.value = EnrolledCoursesUiState.Error(
                        e.message ?: "Nu am putut încărca cursurile"
                    )
                }
        }
    }
}