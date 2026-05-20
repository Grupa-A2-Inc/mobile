package com.adaptive_tutor_mobile.domain.usecase.adaptive

import com.adaptive_tutor_mobile.domain.model.adaptive.AdaptiveSession
import com.adaptive_tutor_mobile.domain.repository.adaptive.AdaptiveRepository
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
