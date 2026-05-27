package com.adaptive_tutor_mobile.presentation.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitAnswerDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto
import com.adaptive_tutor_mobile.domain.repository.test.TestRepository
import com.adaptive_tutor_mobile.domain.usecase.test.ReportQuestionErrorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val testId: String? = null,
    val attemptId: String? = null,
    val questions: List<QuestionForStudentDto> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, List<Int>> = emptyMap(),
    val timeSpentSeconds: Map<Int, Double> = emptyMap(),
    val report: AttemptReportDTO? = null,
    val timeLimitSec: Int? = null,
    val remainingTimeSec: Int? = null,
    val isTimeUp: Boolean = false,

    // ── Dev 5: error reporting ──────────────────────────────────────────
    val showReportDialog: Boolean = false,
    val reportingQuestionId: Int? = null,
    val isSubmittingReport: Boolean = false,
    val reportError: String? = null,
    val reportSuccess: String? = null
)

@HiltViewModel
class TestViewModel @Inject constructor(
    private val repository: TestRepository,
    private val reportQuestionErrorUseCase: ReportQuestionErrorUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(TestUiState(isLoading = true))
    val state: StateFlow<TestUiState> = _state.asStateFlow()

    private var questionStartTime = System.currentTimeMillis()

    private var timerJob: Job? = null

    init {
        val testId: String? = savedStateHandle["testId"]
        _state.update { it.copy(testId = testId) }
        testId?.let { startTest(it) }
    }

    private fun startTest(testId: String) {
        viewModelScope.launch {
            repository.startTest(testId).fold(
                onSuccess = { attempt ->
                    // Sync with server time
                    val serverStart = try {
                        Instant.parse(attempt.startedAt)
                    } catch (e: Exception) {
                        Instant.now()
                    }
                    val elapsed = Duration.between(serverStart, Instant.now()).seconds
                    val adjustedRemaining = (attempt.timeLimitSec ?: 0) - elapsed.toInt()

                    questionStartTime = System.currentTimeMillis()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            attemptId = attempt.attemptId,
                            questions = attempt.questions.orEmpty(),
                            timeLimitSec = attempt.timeLimitSec,
                            remainingTimeSec = adjustedRemaining.coerceAtLeast(0)
                        )
                    }
                    if (adjustedRemaining > 0) {
                        startTimer()
                    } else if (attempt.timeLimitSec != null && attempt.timeLimitSec > 0) {
                        // Already expired
                        _state.update { it.copy(isTimeUp = true) }
                        submitTest()
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Eroare la pornirea testului") }
                }
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Safety buffer: submit when 1 second is remaining instead of 0
            while (_state.value.remainingTimeSec != null && _state.value.remainingTimeSec!! > 1) {
                delay(1000)
                _state.update { it.copy(remainingTimeSec = it.remainingTimeSec!! - 1) }
            }
            if (_state.value.remainingTimeSec == 1) {
                _state.update { it.copy(remainingTimeSec = 0, isTimeUp = true) }
                submitTest()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun selectOption(questionId: Int, optionId: Int, singleChoice: Boolean) {
        val current = _state.value.selectedAnswers[questionId].orEmpty()
        val updated = if (singleChoice) listOf(optionId)
        else if (optionId in current) current - optionId
        else current + optionId
        _state.update { it.copy(selectedAnswers = it.selectedAnswers + (questionId to updated)) }
    }

    fun goToQuestion(index: Int) {
        saveTime()
        val size = _state.value.questions.size
        if (index in 0 until size) {
            questionStartTime = System.currentTimeMillis()
            _state.update { it.copy(currentIndex = index) }
        }
    }

    fun nextQuestion() = goToQuestion(_state.value.currentIndex + 1)
    fun prevQuestion() = goToQuestion(_state.value.currentIndex - 1)

    fun submitTest() {
        timerJob?.cancel()
        saveTime()
        val s = _state.value
        val attemptId = s.attemptId ?: return
        val answers = s.questions.map { q ->
            SubmitAnswerDto(
                questionId = q.questionId,
                selectedOptionIds = s.selectedAnswers[q.questionId].orEmpty(),
                timeSpent = s.timeSpentSeconds[q.questionId] ?: 0.0
            )
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.submitAttempt(attemptId, SubmitRequestDto(answers)).fold(
                onSuccess = { report -> _state.update { it.copy(isLoading = false, report = report) } },
                onFailure = { e ->
                    // Handle 410 Gone - attempt already auto-submitted by server
                    if (e is retrofit2.HttpException && e.code() == 410) {
                        repository.getAttemptReport(attemptId).fold(
                            onSuccess = { report -> _state.update { it.copy(isLoading = false, report = report) } },
                            onFailure = { err -> _state.update { it.copy(isLoading = false, error = err.message ?: "Timpul a expirat și nu am putut prelua rezultatele.") } }
                        )
                    } else {
                        _state.update { it.copy(isLoading = false, error = e.message ?: "Eroare la trimitere") }
                    }
                }
            )
        }
    }

    private fun saveTime() {
        val q = _state.value.questions.getOrNull(_state.value.currentIndex) ?: return
        val elapsed = (System.currentTimeMillis() - questionStartTime) / 1000.0
        val prev = _state.value.timeSpentSeconds[q.questionId] ?: 0.0
        _state.update { it.copy(timeSpentSeconds = it.timeSpentSeconds + (q.questionId to prev + elapsed)) }
        questionStartTime = System.currentTimeMillis()
    }

    fun showReportDialog(questionId: Int) {
        _state.update {
            it.copy(
                showReportDialog = true,
                reportingQuestionId = questionId,
                reportError = null
            )
        }
    }

    fun dismissReportDialog() {
        _state.update {
            it.copy(
                showReportDialog = false,
                reportingQuestionId = null,
                reportError = null,
                isSubmittingReport = false
            )
        }
    }

    fun submitReport(description: String) {
        val questionId = _state.value.reportingQuestionId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingReport = true, reportError = null) }
            reportQuestionErrorUseCase(questionId, description).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isSubmittingReport = false,
                            showReportDialog = false,
                            reportingQuestionId = null,
                            reportSuccess = "Raportul a fost trimis. Mulțumim!"
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isSubmittingReport = false,
                            reportError = e.message ?: "Eroare la trimiterea raportului"
                        )
                    }
                }
            )
        }
    }

    fun clearReportSuccess() {
        _state.update { it.copy(reportSuccess = null) }
    }
}