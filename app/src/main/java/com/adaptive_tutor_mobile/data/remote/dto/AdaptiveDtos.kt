package com.adaptive_tutor_mobile.data.remote.dto

data class AdaptiveStartRequestDto(
    val subjectId: Int,
    val topicId: Int,
    val count: Int
)

data class AdaptiveStartResponseDto(
    val sessionId: String,
    val expiresAt: String?,
    val exercises: List<AdaptiveExerciseDto>
)

data class AdaptiveExerciseDto(
    val exerciseId: String,
    val text: String,
    val type: String,
    val answers: List<String>,
    val difficulty: Double? = null
)

data class AdaptiveSubmitRequestDto(
    val answers: List<AdaptiveAnswerDto>
)

data class AdaptiveAnswerDto(
    val exerciseId: String,
    val givenAnswers: List<String>,
    val timeSpent: Long
)

data class AdaptiveSubmitResponseDto(
    val totalScore: Double,
    val results: List<AdaptiveExerciseResultDto>
)

data class AdaptiveExerciseResultDto(
    val exerciseId: String,
    val correct: Boolean,
    val score: Double,
    val correctAnswers: List<String>,
    val givenAnswers: List<String>
)