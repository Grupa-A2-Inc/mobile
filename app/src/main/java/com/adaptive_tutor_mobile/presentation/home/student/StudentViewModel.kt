package com.adaptive_tutor_mobile.presentation.home.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.data.remote.api.EnrollmentApi
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto
import com.adaptive_tutor_mobile.domain.usecase.courses.UnenrollFromCourseUseCase
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
    private val enrollmentApi: EnrollmentApi,
    private val unenrollFromCourseUseCase: UnenrollFromCourseUseCase
) : ViewModel() {

    private val _coursesState = MutableStateFlow<CoursesUiState>(CoursesUiState.Loading)
    val coursesState: StateFlow<CoursesUiState> = _coursesState.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    init {
        loadEnrolledCourses()
    }

    fun loadEnrolledCourses(page: Int = 0) {
        viewModelScope.launch {
            _coursesState.value = CoursesUiState.Loading
            try {
                val response = progressApi.getMyEnrolledCourses(page = page)
                if (response.isSuccessful) {
                    val body = response.body()
                    _currentPage.value = body?.number ?: page
                    _totalPages.value = body?.totalPages ?: 1

                    // Fetch public course IDs to know which courses allow unenroll
                    val publicIds: Set<String> = try {
                        val pub = enrollmentApi.getPublicCourses(page = 0, size = 500)
                        if (pub.isSuccessful) pub.body()?.content?.map { it.id }?.toSet() ?: emptySet()
                        else emptySet()
                    } catch (_: Exception) { emptySet() }

                    // Mark courseVisibility so the UI card knows whether to show the button
                    val courses = (body?.content ?: emptyList()).map { dto ->
                        dto.copy(courseVisibility = if (dto.courseId in publicIds) "PUBLIC" else "PRIVATE")
                    }

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

    fun nextPage() {
        val next = _currentPage.value + 1
        if (next < _totalPages.value) loadEnrolledCourses(next)
    }

    fun previousPage() {
        val prev = _currentPage.value - 1
        if (prev >= 0) loadEnrolledCourses(prev)
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
