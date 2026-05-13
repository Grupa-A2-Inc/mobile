package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.AdaptiveAnswer
import com.adaptive_tutor_mobile.domain.model.AdaptiveResult
import com.adaptive_tutor_mobile.domain.repository.AdaptiveRepository
import javax.inject.Inject

class SubmitAdaptiveSessionUseCase @Inject constructor(
    private val repository: AdaptiveRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        answers: List<AdaptiveAnswer>
    ): Result<AdaptiveResult> {
        return repository.submitSession(
            sessionId = sessionId,
            answers = answers
        )
    }
}