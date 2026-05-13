package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.TestAttempt
import com.adaptive_tutor_mobile.domain.repository.TestRepository
import javax.inject.Inject

class StartAttemptUseCase @Inject constructor(
    private val repository: TestRepository
) {
    suspend operator fun invoke(testId: String): Result<TestAttempt> {
        return repository.startAttempt(testId)
    }
}