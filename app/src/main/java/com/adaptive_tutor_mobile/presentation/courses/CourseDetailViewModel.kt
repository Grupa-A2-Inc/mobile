package com.adaptive_tutor_mobile.presentation.courses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.CourseDetail
import com.adaptive_tutor_mobile.domain.usecase.GetCourseFullViewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val error: String?                 = null
)

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCourseFullViewUseCase: GetCourseFullViewUseCase
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
            getCourseFullViewUseCase(courseId)
                .onSuccess { detail ->
                    _uiState.update { it.copy(courseDetail = detail, isLoading = false) }
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
}