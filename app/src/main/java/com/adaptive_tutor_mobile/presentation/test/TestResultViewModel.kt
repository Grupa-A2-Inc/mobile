package com.adaptive_tutor_mobile.presentation.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adaptive_tutor_mobile.domain.model.AttemptResult
import com.adaptive_tutor_mobile.domain.usecase.GetAttemptResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestResultViewModel @Inject constructor(
    private val getAttemptResultUseCase: GetAttemptResultUseCase
) : ViewModel() {

    private val _result = MutableStateFlow<AttemptResult?>(null)
    val result: StateFlow<AttemptResult?> = _result.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadResult(attemptId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = getAttemptResultUseCase(attemptId)
            response.onSuccess {
                _result.value = it
            }
            _isLoading.value = false
        }
    }
}