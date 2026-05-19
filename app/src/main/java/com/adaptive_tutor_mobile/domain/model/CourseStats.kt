package com.adaptive_tutor_mobile.domain.model

data class CourseStats(
    val courseTitle: String,
    val totalTests: Int,
    val totalTestDone: Int,
    val passedTests: Int,
    val bestScore: Float,
    val lowestScore: Float,
    val avgScore: Float,
    val hardestLessons: List<String>,
    val lastAttempts: List<AttemptSummary>
)

data class AttemptSummary(
    val attemptId: String,
    val testId: String,
    val testTitle: String,
    val score: Float,
    val scorePercent: Float,
    val passed: Boolean,
    val attemptedAt: String
)
