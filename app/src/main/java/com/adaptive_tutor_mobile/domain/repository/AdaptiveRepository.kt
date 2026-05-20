package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveSubmitRequestDto
import com.adaptive_tutor_mobile.domain.model.AdaptiveSession

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
