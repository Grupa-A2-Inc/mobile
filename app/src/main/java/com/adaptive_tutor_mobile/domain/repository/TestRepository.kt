package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.AttemptResult
import com.adaptive_tutor_mobile.domain.model.TestAttempt

interface TestRepository {
    suspend fun startAttempt(testId: String): Result<TestAttempt>
    suspend fun submitAttempt(attemptId: String, answers: Map<Int, List<Int>>): Result<Unit>
    suspend fun getAttemptResult(attemptId: String): Result<AttemptResult>
    // suspend fun getMyAttempts(testId: String): Result<List<TestAttempt>> // Opțional, dacă e cerut în UI
}
import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.StartAttemptResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto

interface TestRepository {
    suspend fun startTest(testId: String): Result<StartAttemptResponseDto>
    suspend fun submitAttempt(attemptId: String, request: SubmitRequestDto): Result<AttemptReportDTO>
}
