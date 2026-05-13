package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.AttemptResultDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitAnswersRequest
import com.adaptive_tutor_mobile.data.remote.dto.TestAttemptDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TestApi {
    @POST("/api/v1/tests/{testId}/start")
    suspend fun startAttempt(@Path("testId") testId: String): TestAttemptDto

    @POST("/api/v1/attempts/{attemptId}/submit")
    suspend fun submitAttempt(
        @Path("attemptId") attemptId: String,
        @Body request: SubmitAnswersRequest
    )

    @GET("/api/v1/attempts/{attemptId}/result")
    suspend fun getAttemptResult(@Path("attemptId") attemptId: String): AttemptResultDto

    @GET("/api/v1/tests/{testId}/my-attempts")
    suspend fun getMyAttempts(@Path("testId") testId: String): List<TestAttemptDto>
}