package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.AttemptResult
import com.adaptive_tutor_mobile.domain.repository.TestRepository
import javax.inject.Inject

class GetAttemptResultUseCase @Inject constructor(
    private val repository: TestRepository
) {
    suspend operator fun invoke(attemptId: String): Result<AttemptResult> {
        return repository.getAttemptResult(attemptId)
    }
}