package com.adaptive_tutor_mobile.presentation.courses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.courses.CourseDetail
import com.adaptive_tutor_mobile.domain.usecase.courses.DownloadCertificateUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.GetCourseFullViewUseCase
import com.adaptive_tutor_mobile.domain.usecase.courses.GetEnrolledCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseDetailUiState(
    val courseDetail: CourseDetail?    = null,
    val expandedChapters: Set<String> = emptySet(),
    val isLoading: Boolean             = false,
    val error: String?                 = null,
    // Certificate
    val enrollmentId: String?          = null,
    val progressPercent: Double        = 0.0,
    val canDownloadCertificate: Boolean = false,
    val certificateState: CertificateDownloadState = CertificateDownloadState.Idle
)

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCourseFullViewUseCase: GetCourseFullViewUseCase,
    private val getEnrolledCoursesUseCase: GetEnrolledCoursesUseCase,
    private val downloadCertificateUseCase: DownloadCertificateUseCase
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle["courseId"])

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadCourseDetail()
    }

    fun loadCourseDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Fetch course detail and enrollment info in parallel
            val detailDeferred   = async { getCourseFullViewUseCase(courseId) }
            val enrolledDeferred = async { getEnrolledCoursesUseCase() }

            val detailResult   = detailDeferred.await()
            val enrolledResult = enrolledDeferred.await()

            detailResult
                .onSuccess { detail ->
                    val enrollment = enrolledResult.getOrNull()
                        ?.firstOrNull { it.courseId == courseId }

                    val isPublic   = detail.visibility.uppercase() == "PUBLIC"
                    val progress   = enrollment?.progressPercent ?: 0.0
                    val isComplete = progress >= 100.0

                    _uiState.update {
                        it.copy(
                            courseDetail            = detail,
                            isLoading               = false,
                            enrollmentId            = enrollment?.enrollmentId,
                            progressPercent         = progress,
                            canDownloadCertificate  = isPublic && isComplete
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.localizedMessage ?: "Eroare necunoscuta"
                        )
                    }
                }
        }
    }

    fun toggleChapter(chapterId: String) {
        _uiState.update { state ->
            val updated = if (chapterId in state.expandedChapters)
                state.expandedChapters - chapterId
            else
                state.expandedChapters + chapterId
            state.copy(expandedChapters = updated)
        }
    }

    fun downloadCertificate() {
        val state = _uiState.value
        val enrollmentId = state.enrollmentId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(certificateState = CertificateDownloadState.Loading(enrollmentId)) }
            val result = downloadCertificateUseCase(enrollmentId)
            val courseTitle = state.courseDetail?.title ?: ""
            _uiState.update {
                it.copy(
                    certificateState = if (result.isSuccess)
                        CertificateDownloadState.Ready(result.getOrThrow(), courseTitle)
                    else
                        CertificateDownloadState.Error(
                            result.exceptionOrNull()?.message ?: "Eroare la descărcarea certificatului"
                        )
                )
            }
        }
    }

    fun clearCertificateState() {
        _uiState.update { it.copy(certificateState = CertificateDownloadState.Idle) }
    }
}
