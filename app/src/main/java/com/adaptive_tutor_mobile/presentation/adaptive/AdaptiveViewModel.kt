package com.adaptive_tutor_mobile.presentation.adaptive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.AdaptiveAnswer
import com.adaptive_tutor_mobile.domain.model.AdaptiveResult
import com.adaptive_tutor_mobile.domain.model.AdaptiveSession
import com.adaptive_tutor_mobile.domain.usecase.StartAdaptiveSessionUseCase
import com.adaptive_tutor_mobile.domain.usecase.SubmitAdaptiveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdaptiveUiState(
    val session: AdaptiveSession? = null,
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, List<String>> = emptyMap(),
    val timeSpentByExercise: Map<String, Long> = emptyMap(),
    val result: AdaptiveResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdaptiveViewModel @Inject constructor(
    private val startAdaptiveSessionUseCase: StartAdaptiveSessionUseCase,
    private val submitAdaptiveSessionUseCase: SubmitAdaptiveSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdaptiveUiState())
    val uiState: StateFlow<AdaptiveUiState> = _uiState.asStateFlow()

    private var questionStartedAtMillis: Long = System.currentTimeMillis()

    fun startSession(subjectId: Int, topicId: Int, count: Int) {
        viewModelScope.launch {
            _uiState.value = AdaptiveUiState(isLoading = true)

            startAdaptiveSessionUseCase(
                subjectId = subjectId,
                topicId = topicId,
                count = count
            )
                .onSuccess { session ->
                    questionStartedAtMillis = System.currentTimeMillis()
                    _uiState.value = AdaptiveUiState(session = session)
                }
                .onFailure { error ->
                    _uiState.value = AdaptiveUiState(
                        errorMessage = error.message ?: "Nu am putut porni sesiunea adaptivă"
                    )
                }
        }
    }

    fun selectAnswer(exerciseId: String, answer: String) {
        val session = _uiState.value.session ?: return
        val exercise = session.exercises.firstOrNull { it.id == exerciseId } ?: return

        val currentSelection = _uiState.value.selectedAnswers[exerciseId].orEmpty()

        val newSelection = if (exercise.type == "MULTIPLE_CHOICE") {
            if (answer in currentSelection) {
                currentSelection - answer
            } else {
                currentSelection + answer
            }
        } else {
            listOf(answer)
        }

        _uiState.value = _uiState.value.copy(
            selectedAnswers = _uiState.value.selectedAnswers + (exerciseId to newSelection)
        )
    }

    fun goToQuestion(index: Int) {
        saveCurrentQuestionTime()
        val session = _uiState.value.session ?: return
        if (index in 0 until session.exercises.size) {
            questionStartedAtMillis = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(currentIndex = index)
        }
    }

    fun nextQuestion() = goToQuestion(_uiState.value.currentIndex + 1)

    fun prevQuestion() = goToQuestion(_uiState.value.currentIndex - 1)

    fun submitSession() {
        saveCurrentQuestionTime()

        val session = _uiState.value.session ?: return

        val answers = session.exercises.map { exercise ->
            AdaptiveAnswer(
                exerciseId = exercise.id,
                givenAnswers = _uiState.value.selectedAnswers[exercise.id].orEmpty(),
                timeSpent = _uiState.value.timeSpentByExercise[exercise.id] ?: 0L
            )
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            submitAdaptiveSessionUseCase(
                sessionId = session.sessionId,
                answers = answers
            )
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        result = result,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Nu am putut trimite răspunsurile"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun saveCurrentQuestionTime() {
        val session = _uiState.value.session ?: return
        val exercise = session.exercises.getOrNull(_uiState.value.currentIndex) ?: return

        val elapsedSeconds = ((System.currentTimeMillis() - questionStartedAtMillis) / 1000)
            .coerceAtLeast(0)

        val previousValue = _uiState.value.timeSpentByExercise[exercise.id] ?: 0L

        _uiState.value = _uiState.value.copy(
            timeSpentByExercise = _uiState.value.timeSpentByExercise +
                    (exercise.id to previousValue + elapsedSeconds)
        )
    }
}