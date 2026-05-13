package com.adaptive_tutor_mobile.domain.model

data class AdaptiveSession(
    val sessionId: String,
    val expiresAt: String?,
    val exercises: List<AdaptiveExercise>
)

data class AdaptiveExercise(
    val id: String,
    val text: String,
    val type: String,
    val answers: List<String>,
    val difficulty: Double?
)

data class AdaptiveAnswer(
    val exerciseId: String,
    val givenAnswers: List<String>,
    val timeSpent: Long
)

data class AdaptiveResult(
    val totalScore: Double,
    val results: List<ExerciseResult>
)

data class ExerciseResult(
    val exerciseId: String,
    val correct: Boolean,
    val score: Double,
    val correctAnswers: List<String>,
    val givenAnswers: List<String>
)