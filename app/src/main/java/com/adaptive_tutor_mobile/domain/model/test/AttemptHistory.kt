package com.adaptive_tutor_mobile.domain.model.test

data class AttemptHistory(
    val attemptId: String,
    val attemptNumber: Int,
    val score: Double,
    val scorePercent: Double,
    val passed: Boolean,
    val date: String,
    val status: String
)

data class BestAttempt(
    val attemptId: String,
    val score: Double,
    val scorePercent: Double,
    val date: String
)
