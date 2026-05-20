package com.adaptive_tutor_mobile.domain.repository.test

import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.StartAttemptResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto

interface TestRepository {
    suspend fun startTest(testId: String): Result<StartAttemptResponseDto>
    suspend fun submitAttempt(attemptId: String, request: SubmitRequestDto): Result<AttemptReportDTO>
}
