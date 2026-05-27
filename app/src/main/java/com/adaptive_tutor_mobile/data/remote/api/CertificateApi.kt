package com.adaptive_tutor_mobile.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface CertificateApi {

    /**
     * GET /api/v1/enrollments/{enrollmentId}/certificat
     * Generează un certificat PDF pentru un curs completat și public.
     * 200 – PDF generat cu succes
     * 403 – Cursul nu este completat, nu este public, sau studentul nu face parte din enrollment
     * 404 – Enrollment-ul nu există
     */
    @Streaming
    @GET("api/v1/enrollments/{enrollmentId}/certificat")
    suspend fun getCertificate(
        @Path("enrollmentId") enrollmentId: String
    ): Response<ResponseBody>
}
