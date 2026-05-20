package com.adaptive_tutor_mobile.domain.usecase.test

import com.adaptive_tutor_mobile.domain.repository.test.ErrorReportRepository
import javax.inject.Inject

class ReportQuestionErrorUseCase @Inject constructor(
    private val repository: ErrorReportRepository
) {
    suspend operator fun invoke(
        questionId: Int,
        description: String
    ): Result<Unit> {
        val trimmed = description.trim()
        return when {
            trimmed.length < MIN_LENGTH -> Result.failure(
                IllegalArgumentException("Descrierea trebuie să aibă cel puțin $MIN_LENGTH caractere")
            )
            trimmed.length > MAX_LENGTH -> Result.failure(
                IllegalArgumentException("Descrierea poate avea cel mult $MAX_LENGTH caractere")
            )
            else -> repository.reportError(questionId, trimmed)
        }
    }

    companion object {
        const val MIN_LENGTH = 10
        const val MAX_LENGTH = 2000
    }
}
