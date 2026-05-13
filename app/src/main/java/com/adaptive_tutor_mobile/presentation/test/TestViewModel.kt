package com.adaptive_tutor_mobile.presentation.test

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

    private val testId: String = savedStateHandle["testId"] ?: ""

    private val _attempt = MutableStateFlow<TestAttempt?>(null)
    val attempt: StateFlow<TestAttempt?> = _attempt.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, List<Int>>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Int, List<Int>>> = _selectedAnswers.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isSubmitted = MutableStateFlow<String?>(null) // Păstrează attemptId după submit
    val isSubmitted: StateFlow<String?> = _isSubmitted.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadAttempt()
    }

    private fun loadAttempt() {
        viewModelScope.launch {
            val result = startAttemptUseCase(testId)
            result.onSuccess { data ->
                _attempt.value = data
                _remainingSeconds.value = data.timeLimitSec
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            submit() // Auto-submit când ajunge la 0
        }
    }

    fun selectAnswer(questionId: Int, optionIds: List<Int>) {
        val currentAnswers = _selectedAnswers.value.toMutableMap()
        currentAnswers[questionId] = optionIds
        _selectedAnswers.value = currentAnswers
    }

    fun nextQuestion() {
        val maxIndex = (_attempt.value?.questions?.size ?: 1) - 1
        if (_currentQuestionIndex.value < maxIndex) {
            _currentQuestionIndex.value += 1
        }
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun submit() {
        timerJob?.cancel()
        val attemptId = _attempt.value?.attemptId ?: return
        viewModelScope.launch {
            submitAttemptUseCase(attemptId, _selectedAnswers.value)
            _isSubmitted.value = attemptId
        }
    }
}