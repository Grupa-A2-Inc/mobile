package com.adaptive_tutor_mobile.domain.model

enum class QuestionType {
    SINGLE_CHOICE,
    MULTI_CHOICE,
    TRUE_FALSE
}

data class Option(
    val id: Int,
    val text: String
)

data class Question(
    val id: Int,
    val type: QuestionType,
    val content: String,
    val options: List<Option>
)

data class TestAttempt(
    val attemptId: String,
    val timeLimitSec: Int,
    val questions: List<Question>
)

data class QuestionResult(
    val question: Question,
    val correctOptionIds: List<Int>,
    val userOptionIds: List<Int>
)

data class AttemptResult(
    val score: Int,
    val scorePercent: Int,
    val passed: Boolean,
    val questions: List<QuestionResult>
)