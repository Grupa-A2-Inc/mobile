package com.adaptive_tutor_mobile.domain.model.adaptive

import com.adaptive_tutor_mobile.data.remote.dto.AdaptiveQuestionForStudentDto

data class AdaptiveSession(
    val sessionId: String,
    val attemptId: String?,
    val expiresAt: String?,
    val questions: List<AdaptiveQuestionForStudentDto>
)
