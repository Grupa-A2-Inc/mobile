package com.adaptive_tutor_mobile.domain.repository.adaptive

import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveSubmitRequestDto
import com.adaptive_tutor_mobile.domain.model.adaptive.AdaptiveSession

interface AdaptiveRepository {
    suspend fun startSession(
        subjectId: Int,
        topicId: Int,
        count: Int
    ): Result<AdaptiveSession>

    suspend fun submitSession(
        sessionId: String,
        request: AdaptiveSubmitRequestDto
    ): Result<AdaptiveAttemptReportDTO>
}
