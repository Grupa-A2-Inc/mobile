package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveStartRequestDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveStartResponseDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveSubmitRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AdaptiveApi {

    @POST("api/v1/adaptive/start")
    suspend fun startSession(
        @Body request: AdaptiveStartRequestDto
    ): Response<AdaptiveStartResponseDto>

    @POST("api/v1/adaptive/sessions/{sessionId}/submit")
    suspend fun submitSession(
        @Path("sessionId") sessionId: String,
        @Body request: AdaptiveSubmitRequestDto
    ): Response<AdaptiveAttemptReportDTO>
}
