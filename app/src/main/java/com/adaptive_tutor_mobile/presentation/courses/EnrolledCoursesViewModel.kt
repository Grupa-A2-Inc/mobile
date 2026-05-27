package com.adaptive_tutor_mobile.presentation.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.courses.EnrolledCourse
import com.adaptive_tutor_mobile.domain.usecase.courses.DownloadCertificateUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.GetEnrolledCoursesUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.UnenrollFromCourseUseCase
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

/** Starea descărcării unui certificat */
sealed class CertificateDownloadState {
    object Idle : CertificateDownloadState()
    data class Loading(val enrollmentId: String) : CertificateDownloadState()
    data class Ready(val pdfBytes: ByteArray, val courseTitle: String) : CertificateDownloadState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Ready) return false
            return pdfBytes.contentEquals(other.pdfBytes) && courseTitle == other.courseTitle
        }
        override fun hashCode(): Int = 31 * pdfBytes.contentHashCode() + courseTitle.hashCode()
    }
    data class Error(val message: String) : CertificateDownloadState()
}

@HiltViewModel
class EnrolledCoursesViewModel @Inject constructor(
    private val getEnrolledCoursesUseCase: GetEnrolledCoursesUseCase,
    private val unenrollFromCourseUseCase: UnenrollFromCourseUseCase,
    private val downloadCertificateUseCase: DownloadCertificateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EnrolledCoursesUiState>(EnrolledCoursesUiState.Loading)
    val uiState: StateFlow<EnrolledCoursesUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _unenrollSuccess = MutableStateFlow<String?>(null)
    val unenrollSuccess: StateFlow<String?> = _unenrollSuccess.asStateFlow()

    private val _certificateState = MutableStateFlow<CertificateDownloadState>(CertificateDownloadState.Idle)
    val certificateState: StateFlow<CertificateDownloadState> = _certificateState.asStateFlow()

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

    fun downloadCertificate(course: EnrolledCourse) {
        if (course.enrollmentId.isBlank()) {
            _certificateState.value = CertificateDownloadState.Error("ID-ul de enrollment lipsește")
            return
        }
        viewModelScope.launch {
            _certificateState.value = CertificateDownloadState.Loading(course.enrollmentId)
            val result = downloadCertificateUseCase(course.enrollmentId)
            _certificateState.value = if (result.isSuccess) {
                CertificateDownloadState.Ready(result.getOrThrow(), course.courseTitle)
            } else {
                CertificateDownloadState.Error(
                    result.exceptionOrNull()?.message ?: "Eroare la descărcarea certificatului"
                )
            }
        }
    }

    fun clearCertificateState() {
        _certificateState.value = CertificateDownloadState.Idle
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearUnenrollSuccess() {
        _unenrollSuccess.value = null
    }
}
