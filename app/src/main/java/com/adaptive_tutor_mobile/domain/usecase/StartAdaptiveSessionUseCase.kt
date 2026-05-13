package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.AdaptiveSession
import com.adaptive_tutor_mobile.domain.repository.AdaptiveRepository
import javax.inject.Inject

class StartAdaptiveSessionUseCase @Inject constructor(
    private val repository: AdaptiveRepository
) {
    suspend operator fun invoke(
        subjectId: Int,
        topicId: Int,
        count: Int
    ): Result<AdaptiveSession> {
        return repository.startSession(
            subjectId = subjectId,
            topicId = topicId,
            count = count
        )
    }
}