package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.TestApi
import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.StartAttemptResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto
import com.adaptive_tutor_mobile.domain.repository.TestRepository
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    private val api: TestApi
) : TestRepository {

    override suspend fun startTest(testId: String): Result<StartAttemptResponseDto> = try {
        Result.success(api.startTest(testId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun submitAttempt(
        attemptId: String,
        request: SubmitRequestDto
    ): Result<AttemptReportDTO> = try {
        Result.success(api.submitAttempt(attemptId, request))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
