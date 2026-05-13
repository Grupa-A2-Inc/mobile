package com.adaptive_tutor_mobile.presentation.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.LessonDetail
import com.adaptive_tutor_mobile.domain.repository.LessonRepository
import com.adaptive_tutor_mobile.domain.usecase.GetLessonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonState(
    val lesson: LessonDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val testId: String? = null,
    val isCheckingTest: Boolean = false
)

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(LessonState())
    val state: StateFlow<LessonState> = _state.asStateFlow()

    init {
        val lessonId: String? = savedStateHandle["lessonId"]
        lessonId?.let { loadLesson(it) }
    }

    private fun loadLesson(lessonId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getLessonDetailUseCase(lessonId).fold(
                onSuccess = { lessonDetail ->
                    _state.update { it.copy(isLoading = false, lesson = lessonDetail, isCheckingTest = true) }
                    checkTest(lessonId)
                    launch { lessonRepository.markVisited(lessonId) }
                },
                onFailure = { exception ->
                    _state.update { it.copy(isLoading = false, error = exception.message ?: "Eroare necunoscută") }
                }
            )
        }
    }

    private fun checkTest(lessonId: String) {
        viewModelScope.launch {
            val testId = lessonRepository.checkLessonTest(lessonId)
            _state.update { it.copy(isCheckingTest = false, testId = testId) }
        }
    }
}
