package com.adaptive_tutor_mobile.presentation.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.EnrolledCourse
import com.adaptive_tutor_mobile.domain.usecase.GetEnrolledCoursesUseCase
import com.adaptive_tutor_mobile.domain.usecase.UnenrollFromCourseUseCase
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
    private val getEnrolledCoursesUseCase: GetEnrolledCoursesUseCase,
    private val unenrollFromCourseUseCase: UnenrollFromCourseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EnrolledCoursesUiState>(EnrolledCoursesUiState.Loading)
    val uiState: StateFlow<EnrolledCoursesUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _unenrollSuccess = MutableStateFlow<String?>(null)
    val unenrollSuccess: StateFlow<String?> = _unenrollSuccess.asStateFlow()

    init {
        loadCourses()
    }

    fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = EnrolledCoursesUiState.Loading
            val result = getEnrolledCoursesUseCase()
            if (result.isSuccess) {
                _uiState.value = EnrolledCoursesUiState.Success(result.getOrThrow())
            } else {
                val error = result.exceptionOrNull()!!
                _uiState.value = EnrolledCoursesUiState.Error(
                    error.message ?: "Nu am putut încărca cursurile"
                )
            }
        }
    }

    fun unenroll(courseId: String) {
        val currentState = _uiState.value as? EnrolledCoursesUiState.Success ?: return

        viewModelScope.launch {
            val result = unenrollFromCourseUseCase(courseId)
            if (result.isSuccess) {
                _uiState.value = EnrolledCoursesUiState.Success(
                    currentState.courses.filterNot { it.courseId == courseId }
                )
                _unenrollSuccess.value = "Te-ai dezabonat"
            } else {
                val error = result.exceptionOrNull()!!
                _errorMessage.value = error.message ?: "Nu am putut procesa dezabonarea"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearUnenrollSuccess() {
        _unenrollSuccess.value = null
    }
}
