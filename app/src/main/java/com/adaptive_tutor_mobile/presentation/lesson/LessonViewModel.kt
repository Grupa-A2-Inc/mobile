package com.adaptive_tutor_mobile.presentation.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.lesson.LessonDetail
import com.adaptive_tutor_mobile.domain.repository.lesson.LessonRepository
import com.adaptive_tutor_mobile.domain.usecase.lesson.GetLessonDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.adaptive_tutor_mobile.domain.model.lesson.RatingSummary
import com.adaptive_tutor_mobile.domain.usecase.lesson.SubmitLessonRatingUseCase

data class LessonState(
    val lesson: LessonDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val testId: String? = null,
    val isCheckingTest: Boolean = false,
    val ratingSummary: RatingSummary? = null,
    val hasRated: Boolean = false,
    val showRatingDialog: Boolean = false
)

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val lessonRepository: LessonRepository,
    private val submitLessonRatingUseCase: SubmitLessonRatingUseCase,
    private val ratingRepository: com.adaptive_tutor_mobile.domain.repository.lesson.RatingRepository,
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
                    loadRatingSummary(lessonId)
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

    private fun loadRatingSummary(lessonId: String) {
        viewModelScope.launch {
            ratingRepository.getRatingSummary(lessonId).onSuccess { summary ->
                _state.update { it.copy(ratingSummary = summary) }
            }
        }
    }

    fun showRatingDialog() {
        _state.update { it.copy(showRatingDialog = true) }
    }

    fun dismissRatingDialog() {
        _state.update { it.copy(showRatingDialog = false) }
    }

    fun submitRating(lessonId: String, rating: Int, comment: String?) {
        viewModelScope.launch {
            submitLessonRatingUseCase(lessonId, rating, comment).onSuccess {
                _state.update { it.copy(hasRated = true, showRatingDialog = false) }
                loadRatingSummary(lessonId)
            }
        }
    }
}
