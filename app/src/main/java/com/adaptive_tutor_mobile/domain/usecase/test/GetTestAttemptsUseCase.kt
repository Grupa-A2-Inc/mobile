package com.adaptive_tutor_mobile.domain.usecase.test

import com.adaptive_tutor_mobile.domain.model.test.AttemptHistory
import com.adaptive_tutor_mobile.domain.model.test.BestAttempt
import com.adaptive_tutor_mobile.domain.repository.test.AttemptHistoryRepository
import javax.inject.Inject

data class TestAttemptsResult(
    val attempts: List<AttemptHistory>,
    val bestAttempt: BestAttempt?
)

class GetTestAttemptsUseCase @Inject constructor(
    private val repository: AttemptHistoryRepository
) {
    suspend operator fun invoke(testId: String): Result<TestAttemptsResult> {
        return try {
            val attemptsResult = repository.getAttempts(testId)
            val bestAttemptResult = repository.getBestAttempt(testId)

            if (attemptsResult.isSuccess) {
                Result.success(
                    TestAttemptsResult(
                        attempts = attemptsResult.getOrThrow(),
                        bestAttempt = bestAttemptResult.getOrNull()
                    )
                )
            } else {
                Result.failure(attemptsResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
