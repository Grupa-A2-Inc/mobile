package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.AttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.StartAttemptResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface TestApi {
    @POST("/api/v1/tests/{testId}/start")
    suspend fun startTest(@Path("testId") testId: String): StartAttemptResponseDto

    @POST("/api/v1/attempts/{attemptId}/submit")
    suspend fun submitAttempt(
        @Path("attemptId") attemptId: String,
        @Body request: SubmitRequestDto
    ): AttemptReportDTO

    @retrofit2.http.GET("/api/v1/attempts/{attemptId}/report")
    suspend fun getAttemptReport(@Path("attemptId") attemptId: String): AttemptReportDTO
}
