package com.adaptive_tutor_mobile.domain.model

import com.adaptive_tutor_mobile.data.remote.dto.QuestionForStudentDto

data class AdaptiveSession(
    val sessionId: String,
    val attemptId: String?,
    val expiresAt: String?,
    val questions: List<QuestionForStudentDto>
)
