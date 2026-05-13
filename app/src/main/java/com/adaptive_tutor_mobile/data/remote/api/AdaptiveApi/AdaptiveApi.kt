package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveStartRequestDto
import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveStartResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

fun interface AdaptiveApi {

    @POST("api/v1/adaptive/start")
    suspend fun startSession(
        @Body request: AdaptiveStartRequestDto
    ): Response<AdaptiveStartResponseDto>
}
