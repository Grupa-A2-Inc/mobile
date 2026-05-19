package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.AttemptStatusDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AttemptHistoryApi {
    @GET("/api/v1/tests/{testId}/my-attempts")
    suspend fun getMyAttempts(
        @Path("testId") testId: String
    ): Response<List<AttemptStatusDto>>

    @GET("/api/v1/tests/{testId}/my-best")
    suspend fun getMyBestAttempt(
        @Path("testId") testId: String
    ): Response<AttemptStatusDto>
}
