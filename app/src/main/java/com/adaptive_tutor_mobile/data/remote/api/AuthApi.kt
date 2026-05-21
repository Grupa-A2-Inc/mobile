package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.AuthResponse
import com.adaptive_tutor_mobile.data.remote.dto.CsrfResponse
import com.adaptive_tutor_mobile.data.remote.dto.ForgotPasswordRequest
import com.adaptive_tutor_mobile.data.remote.dto.LoginRequest
import com.adaptive_tutor_mobile.data.remote.dto.RefreshResponse
import com.adaptive_tutor_mobile.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/v1/auth/csrf")
    suspend fun getCsrf(): Response<CsrfResponse>

    /** Sends the HttpOnly refresh_token cookie via CookieJar — nothing in the body. */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(): Response<RefreshResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshWithCsrf(@Header("X-XSRF-TOKEN") csrfToken: String): Response<RefreshResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/v1/auth/password-reset/request")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>
}
