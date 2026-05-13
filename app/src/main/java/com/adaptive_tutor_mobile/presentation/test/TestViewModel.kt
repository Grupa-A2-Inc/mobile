package com.adaptive_tutor_mobile.presentation.test

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.TestAttempt
import com.adaptive_tutor_mobile.domain.usecase.StartAttemptUseCase
import com.adaptive_tutor_mobile.domain.usecase.SubmitAttemptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val startAttemptUseCase: StartAttemptUseCase,
    private val submitAttemptUseCase: SubmitAttemptUseCase
) : ViewModel() {

    // 1. Verificăm ID-ul din start
    private val testId: String = savedStateHandle["testId"] ?: ""

    private val _attempt = MutableStateFlow<TestAttempt?>(null)
    val attempt = _attempt.asStateFlow()

    // 2. Adăugăm o stare explicită pentru erori sau loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, List<Int>>>(emptyMap())
    val selectedAnswers = _selectedAnswers.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds = _remainingSeconds.asStateFlow()

    private val _isSubmitted = MutableStateFlow<String?>(null)
    val isSubmitted = _isSubmitted.asStateFlow()

    private var timerJob: Job? = null

    init {
        if (testId.isNotEmpty() && testId != "null") {
            loadAttempt()
        } else {
            _errorMessage.value = "ID-ul testului lipsește sau este invalid."
        }
    }

    private fun loadAttempt() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // 3. Gestionăm succesul și eșecul
            startAttemptUseCase(testId)
                .onSuccess { data ->
                    _attempt.value = data
                    _remainingSeconds.value = data.timeLimitSec
                    startTimer()
                }
                .onFailure { exception ->
                    Log.e("TestViewModel", "Eroare la încărcare test: ${exception.message}")
                    _errorMessage.value = "Nu am putut porni testul. Verifică conexiunea."
                }

            _isLoading.value = false
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            // 4. Atenție: submit() este o funcție suspend, dar aici e apelată din corutină
            submit()
        }
    }

    fun selectAnswer(questionId: Int, optionIds: List<Int>) {
        val currentAnswers = _selectedAnswers.value.toMutableMap()
        currentAnswers[questionId] = optionIds
        _selectedAnswers.value = currentAnswers
    }

    fun nextQuestion() {
        _attempt.value?.questions?.let { questions ->
            if (_currentQuestionIndex.value < questions.size - 1) {
                _currentQuestionIndex.value += 1
            }
        }
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun submit() {
        val attemptId = _attempt.value?.attemptId ?: return
        timerJob?.cancel()

        viewModelScope.launch {
            _isLoading.value = true
            submitAttemptUseCase(attemptId, _selectedAnswers.value)
                .onSuccess {
                    _isSubmitted.value = attemptId
                }
                .onFailure {
                    _errorMessage.value = "Eroare la trimiterea răspunsurilor."
                }
            _isLoading.value = false
        }
    }
}
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForStudentDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitAnswerDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto
import com.adaptive_tutor_mobile.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val attemptId: String? = null,
    val questions: List<QuestionForStudentDto> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, List<Int>> = emptyMap(),
    val timeSpentSeconds: Map<Int, Double> = emptyMap(),
    val report: AttemptReportDTO? = null
)

@HiltViewModel
class TestViewModel @Inject constructor(
    private val repository: TestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(TestUiState(isLoading = true))
    val state: StateFlow<TestUiState> = _state.asStateFlow()

    private var questionStartTime = System.currentTimeMillis()

    init {
        savedStateHandle.get<String>("testId")?.let { startTest(it) }
    }

    private fun startTest(testId: String) {
        viewModelScope.launch {
            repository.startTest(testId).fold(
                onSuccess = { attempt ->
                    questionStartTime = System.currentTimeMillis()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            attemptId = attempt.attemptId,
                            questions = attempt.questions.orEmpty()
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Eroare la pornirea testului") }
                }
            )
        }
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
        saveTime()
        val s = _state.value
        val attemptId = s.attemptId ?: return
        val answers = s.questions.map { q ->
            SubmitAnswerDto(
                questionId = q.questionId,
                selectedOptionIds = s.selectedAnswers[q.questionId].orEmpty(),
                timeSpent = s.timeSpentSeconds[q.questionId]
            )
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.submitAttempt(attemptId, SubmitRequestDto(answers)).fold(
                onSuccess = { report -> _state.update { it.copy(isLoading = false, report = report) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Eroare la trimitere") } }
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
}
