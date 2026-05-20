package com.adaptive_tutor_mobile.domain.repository.test

interface ErrorReportRepository {
    suspend fun reportError(questionId: Int, description: String): Result<Unit>
}
