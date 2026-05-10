package com.adaptive_tutor_mobile.presentation.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.Course
import com.adaptive_tutor_mobile.domain.usecase.EnrollInCourseUseCase
import com.adaptive_tutor_mobile.domain.usecase.GetPublicCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublicCoursesViewModel @Inject constructor(
    private val getPublicCoursesUseCase: GetPublicCoursesUseCase,
    private val enrollInCourseUseCase: EnrollInCourseUseCase
) : ViewModel() {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _enrollSuccess = MutableStateFlow<String?>(null)
    val enrollSuccess: StateFlow<String?> = _enrollSuccess
    private val _enrolledCourseIds = MutableStateFlow<Set<String>>(emptySet())
    val enrolledCourseIds: StateFlow<Set<String>> = _enrolledCourseIds

    init {
        loadCourses()
    }

    fun loadCourses(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = getPublicCoursesUseCase(page, size)
            result.onSuccess { _courses.value = it }
            result.onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun enroll(courseId: String) {
        viewModelScope.launch {
            val result = enrollInCourseUseCase(courseId)
            result.onSuccess {
                _enrollSuccess.value = "Înscris cu succes!"
                _enrolledCourseIds.value = _enrolledCourseIds.value + courseId
            }
            result.onFailure { _errorMessage.value = it.message }
        }
    }

    fun clearEnrollSuccess() {
        _enrollSuccess.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}