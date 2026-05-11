package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.AuthApi
import com.adaptive_tutor_mobile.data.remote.dto.ForgotPasswordRequest
import com.adaptive_tutor_mobile.data.remote.dto.LoginRequest
import com.adaptive_tutor_mobile.data.remote.dto.RegisterRequest
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.User
import com.adaptive_tutor_mobile.domain.model.toDomain
import com.adaptive_tutor_mobile.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val response = api.login(LoginRequest(email, password))
        if (response.isSuccessful) {
            val body = response.body() ?: error("Empty response body")
            body.accessToken?.let { sessionStore.saveAccessToken(it) }
            val user = body.user?.toDomain() ?: error("No user in response")
            sessionStore.saveUser(user)
            user
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<User> = runCatching {
        val response = api.register(request)
        if (response.isSuccessful) {
            val body = response.body() ?: error("Empty response body")
            body.accessToken?.let { sessionStore.saveAccessToken(it) }
            val user = body.user?.toDomain() ?: error("No user in response")
            sessionStore.saveUser(user)
            user
        } else {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        try { api.logout() } catch (_: Exception) { /* best-effort */ }
        sessionStore.clearAll()
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        val response = api.forgotPassword(ForgotPasswordRequest(email))
        if (!response.isSuccessful) {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    private fun parseError(code: Int, body: String?): String {
        if (!body.isNullOrBlank()) {
            return try {
                val json = com.google.gson.JsonParser.parseString(body).asJsonObject
                json["message"]?.asString
                    ?: json["error"]?.asString
                    ?: "Eroare $code"
            } catch (_: Exception) { "Eroare $code" }
        }
        return when (code) {
            400 -> "Date invalide"
            401 -> "Email sau parolă incorecte"
            409 -> "Cont deja existent"
            else -> "Eroare $code"
        }
    }
}