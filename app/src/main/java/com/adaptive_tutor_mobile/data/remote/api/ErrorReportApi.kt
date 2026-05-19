package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.ErrorReportRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ErrorReportApi {

    /**
     * POST /api/v1/questions/{questionId}/error-reports
     * Trimite un raport de eroare pentru o întrebare dintr-un test.
     * Backend-ul răspunde 201 Created cu body-ul ErrorReportDto, dar
     * frontend-ul ignoră body-ul (nu avem nevoie de ID-ul raportului).
     */
    @POST("api/v1/questions/{questionId}/error-reports")
    suspend fun reportError(
        @Path("questionId") questionId: Int,
        @Body request: ErrorReportRequestDto
    ): Response<Unit>
}