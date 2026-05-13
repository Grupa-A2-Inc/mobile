package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.repository.TestRepository
import javax.inject.Inject

class SubmitAttemptUseCase @Inject constructor(
    private val repository: TestRepository
) {
    suspend operator fun invoke(attemptId: String, answers: Map<Int, List<Int>>): Result<Unit> {
        return repository.submitAttempt(attemptId, answers)
    }
}