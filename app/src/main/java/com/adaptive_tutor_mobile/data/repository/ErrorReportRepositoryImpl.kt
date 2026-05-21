package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.ErrorReportApi
import com.adaptive_tutor_mobile.data.remote.dto.ErrorReportRequestDto
import com.adaptive_tutor_mobile.domain.repository.test.ErrorReportRepository
import com.google.gson.JsonParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorReportRepositoryImpl @Inject constructor(
    private val api: ErrorReportApi
) : ErrorReportRepository {

    override suspend fun reportError(
        questionId: Int,
        description: String
    ): Result<Unit> = runCatching {
        val response = api.reportError(questionId, ErrorReportRequestDto(description))
        if (!response.isSuccessful) {
            error(parseError(response.code(), response.errorBody()?.string()))
        }
    }

    private fun parseError(code: Int, body: String?): String {
        if (!body.isNullOrBlank()) {
            return try {
                val json = JsonParser.parseString(body).asJsonObject
                json["message"]?.asString
                    ?: json["error"]?.asString
                    ?: "Eroare $code"
            } catch (_: Exception) { "Eroare $code" }
        }
        return when (code) {
            400 -> "Descrierea trebuie să aibă între 10 și 2000 de caractere"
            401 -> "Sesiune expirată"
            403 -> "Nu ai acces la această întrebare"
            404 -> "Întrebarea nu a fost găsită"
            else -> "Eroare $code"
        }
    }
}