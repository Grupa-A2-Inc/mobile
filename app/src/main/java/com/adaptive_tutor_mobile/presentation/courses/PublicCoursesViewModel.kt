package com.adaptive_tutor_mobile.presentation.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.domain.model.courses.Course
import com.adaptive_tutor_mobile.domain.usecase.courses.EnrollInCourseUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.GetPublicCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublicCoursesViewModel @Inject constructor(
    private val getPublicCoursesUseCase: GetPublicCoursesUseCase,
    private val enrollInCourseUseCase: EnrollInCourseUseCase,
    private val progressApi: ProgressApi
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

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    init {
        loadEnrolledIds()
        loadCourses()
    }

    /**
     * Încarcă lista cursurilor la care studentul e deja înscris,
     * ca să afișăm corect butonul „Înscris ✓" încă de la prima afișare.
     */
    private fun loadEnrolledIds() {
        viewModelScope.launch {
            try {
                val response = progressApi.getMyEnrolledCourses()
                if (response.isSuccessful) {
                    val ids = response.body()?.content?.map { it.courseId }?.toSet() ?: emptySet()
                    _enrolledCourseIds.value = ids
                }
            } catch (_: Exception) {
                // best-effort: dacă pică, UI-ul rămâne cu set gol
                // backend-ul oricum va răspunde cu 409 dacă userul apasă „Înscrie-te"
            }
        }
    }

    fun loadCourses(page: Int = 0, size: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = getPublicCoursesUseCase(page, size)
            result.onSuccess {
                _courses.value = it.courses
                _currentPage.value = it.currentPage
                _totalPages.value = it.totalPages
            }
            result.onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun nextPage() {
        if (_currentPage.value < _totalPages.value - 1) {
            loadCourses(_currentPage.value + 1)
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            loadCourses(_currentPage.value - 1)
        }
    }

    fun enroll(courseId: String) {
        viewModelScope.launch {
            val result = enrollInCourseUseCase(courseId)
            result.onSuccess {
                _enrollSuccess.value = "Înscris cu succes!"
                _enrolledCourseIds.value = _enrolledCourseIds.value + courseId
            }
            result.onFailure { e ->
                val msg = e.message.orEmpty()
                _errorMessage.value = when {
                    msg.contains("already enrolled", ignoreCase = true) || msg.contains("409") -> {
                        _enrolledCourseIds.value = _enrolledCourseIds.value + courseId
                        "Ești deja înscris la acest curs"
                    }
                    else -> msg.ifBlank { "Eroare la înscriere" }
                }
            }
        }
    }

    fun clearEnrollSuccess() {
        _enrollSuccess.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}