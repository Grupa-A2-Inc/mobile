package com.adaptive_tutor_mobile.data.remote.dto

/**
 * Body pentru POST /api/v1/questions/{questionId}/error-reports.
 * Backend-ul îl numește DescriptionRequestDto; păstrăm conceptul cu
 * un nume mai descriptiv pe partea de mobil.
 *
 * Validare backend: description între 10 și 2000 caractere (validat și client-side
 * în ReportQuestionErrorUseCase).
 */
data class ErrorReportRequestDto(
    val description: String
)