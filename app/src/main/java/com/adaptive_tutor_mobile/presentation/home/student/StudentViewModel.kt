package com.adaptive_tutor_mobile.presentation.home.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto
import com.adaptive_tutor_mobile.domain.usecase.UnenrollFromCourseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CoursesUiState {
    object Loading : CoursesUiState()
    data class Success(val courses: List<EnrolledCourseDto>) : CoursesUiState()
    data class Error(val message: String) : CoursesUiState()
}

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val progressApi: ProgressApi,
    private val unenrollFromCourseUseCase: UnenrollFromCourseUseCase
) : ViewModel() {

    private val _coursesState = MutableStateFlow<CoursesUiState>(CoursesUiState.Loading)
    val coursesState: StateFlow<CoursesUiState> = _coursesState.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadEnrolledCourses()
    }

    fun loadEnrolledCourses() {
        viewModelScope.launch {
            _coursesState.value = CoursesUiState.Loading
            try {
                val response = progressApi.getMyEnrolledCourses()
                if (response.isSuccessful) {
                    val courses = response.body()?.content ?: emptyList()
                    _coursesState.value = CoursesUiState.Success(courses)
                } else {
                    _coursesState.value = CoursesUiState.Error(
                        "Nu s-au putut încărca cursurile (cod ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _coursesState.value = CoursesUiState.Error(e.message ?: "Eroare necunoscută")
            }
        }
    }

    fun unenroll(courseId: String) {
        val currentState = _coursesState.value as? CoursesUiState.Success ?: return

        viewModelScope.launch {
            val result = try {
                unenrollFromCourseUseCase(courseId)
            } catch (error: Exception) {
                _message.value = error.message ?: "Nu am putut procesa dezabonarea"
                return@launch
            }

            result
                .onSuccess {
                    _coursesState.value = CoursesUiState.Success(
                        currentState.courses.filterNot { it.courseId == courseId }
                    )
                    _message.value = "Te-ai dezabonat"
                }
                .onFailure { error ->
                    _message.value = error.message ?: "Nu am putut procesa dezabonarea"
                }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
