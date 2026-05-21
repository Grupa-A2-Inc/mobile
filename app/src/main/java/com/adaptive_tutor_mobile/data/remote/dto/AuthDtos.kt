package com.adaptive_tutor_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── REQUEST ──────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val organizationName: String,
    val country: String,
    val city: String,
    val organizationType: String,
    val address: String? = null,
    val phoneNumber: String? = null
)

data class ForgotPasswordRequest(
    val email: String
)

// ── RESPONSE ─────────────────────────────────────────────────────────────────

data class AuthResponse(
    val message: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val user: UserDataResponse?
)

data class UserDataResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val status: String,
    val organizationId: String?,
    val organizationName: String?,
    val organizationType: String?,
    val country: String?,
    val city: String?,
    val organizationPhoneNumber: String?,
    val organizationAddress: String?
)

data class RefreshResponse(
    val accessToken: String?
)

data class CsrfResponse(
    val csrfToken: String?,
    val headerName: String?
)
