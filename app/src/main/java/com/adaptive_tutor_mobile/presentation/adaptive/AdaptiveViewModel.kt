package com.adaptive_tutor_mobile.presentation.adaptive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveSubmitAnswerDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveSubmitRequestDto
import com.adaptive_tutor_mobile.domain.model.adaptive.AdaptiveSession
import com.adaptive_tutor_mobile.domain.repository.adaptive.AdaptiveRepository
import com.adaptive_tutor_mobile.domain.usecase.adaptive.StartAdaptiveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdaptiveUiState(
    val session: AdaptiveSession? = null,
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, List<Int>> = emptyMap(),
    val timeSpentSeconds: Map<String, Double> = emptyMap(),
    val result: AdaptiveAttemptReportDTO? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AdaptiveViewModel @Inject constructor(
    private val startAdaptiveSessionUseCase: StartAdaptiveSessionUseCase,
    private val adaptiveRepository: AdaptiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdaptiveUiState())
    val uiState: StateFlow<AdaptiveUiState> = _uiState.asStateFlow()

    private var questionStartedAtMillis: Long = System.currentTimeMillis()

    fun startSession(subjectId: Int, topicId: Int, count: Int) {
        viewModelScope.launch {
            _uiState.value = AdaptiveUiState(isLoading = true)
            startAdaptiveSessionUseCase(subjectId = subjectId, topicId = topicId, count = count)
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

    fun selectAnswer(questionId: String, optionId: Int, singleChoice: Boolean) {
        val current = _uiState.value.selectedAnswers[questionId].orEmpty()
        val newSelection = if (singleChoice) listOf(optionId)
        else if (optionId in current) current - optionId
        else current + optionId
        _uiState.value = _uiState.value.copy(
            selectedAnswers = _uiState.value.selectedAnswers + (questionId to newSelection)
        )
    }

    fun goToQuestion(index: Int) {
        saveCurrentQuestionTime()
        val session = _uiState.value.session ?: return
        if (index in 0 until session.questions.size) {
            questionStartedAtMillis = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(currentIndex = index)
        }
    }

    fun nextQuestion() = goToQuestion(_uiState.value.currentIndex + 1)
    fun prevQuestion() = goToQuestion(_uiState.value.currentIndex - 1)

    fun submitSession() {
        saveCurrentQuestionTime()
        val session = _uiState.value.session ?: return
        val answers = session.questions.map { q ->
            val selectedTexts = _uiState.value.selectedAnswers[q.questionId].orEmpty().mapNotNull { optionId ->
                q.options?.find { it.optionId == optionId }?.text
            }
            AdaptiveSubmitAnswerDto(
                questionId = q.questionId,
                selectedOptionIds = selectedTexts,
                timeSpent = (_uiState.value.timeSpentSeconds[q.questionId] ?: 0.0).toInt()
            )
        }
        val sessionId = session.sessionId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            adaptiveRepository.submitSession(sessionId, AdaptiveSubmitRequestDto(answers))
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(result = result, isLoading = false)
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
        val q = session.questions.getOrNull(_uiState.value.currentIndex) ?: return
        val elapsed = (System.currentTimeMillis() - questionStartedAtMillis) / 1000.0
        val prev = _uiState.value.timeSpentSeconds[q.questionId] ?: 0.0
        _uiState.value = _uiState.value.copy(
            timeSpentSeconds = _uiState.value.timeSpentSeconds + (q.questionId to prev + elapsed)
        )
    }
}
