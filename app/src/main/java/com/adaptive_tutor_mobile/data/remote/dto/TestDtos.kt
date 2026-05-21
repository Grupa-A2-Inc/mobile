package com.adaptive_tutor_mobile.data.remote.dto

// ── TEST ─────────────────────────────────────────────────────────────────────

data class TestEditDto(
    val title: String?,
    val description: String?,
    val timeLimitSec: Int?,
    val aiEnabled: Boolean?
)

data class TestEntityDto(
    val id: String,
    val lessonId: String,
    val createdBy: String,
    val title: String,
    val description: String?,
    val timeLimitSec: Int?,
    val status: String,
    val aiEnabled: Boolean?,
    val createdAt: String?,
    val updatedAt: String?
)

// ── QUESTION (teacher/admin) ──────────────────────────────────────────────────

data class QuestionRequestDto(
    val questionType: String, // "SINGLE_CHOICE" | "MULTI_CHOICE" | "TRUE_FALSE"
    val content: String,
    val difficulty: Double?,
    val options: List<OptionRequestDto>
)

data class OptionRequestDto(
    val text: String,
    val displayOrder: Int,
    val isCorrect: Boolean
)

data class QuestionResponseDto(
    val questionId: Long,
    val questionType: String,
    val content: String,
    val difficulty: Double?,
    val options: List<OptionResponseDto>
)

data class OptionResponseDto(
    val optionId: Long,
    val text: String,
    val displayOrder: Int,
    val isCorrect: Boolean?
)

// ── QUESTION (student view) ───────────────────────────────────────────────────

data class QuestionForStudentDto(
    val questionId: Int,
    @com.google.gson.annotations.SerializedName("questionType", alternate = ["type"])
    val questionType: String? = null,
    @com.google.gson.annotations.SerializedName("content", alternate = ["text"])
    val content: String? = null,
    val difficulty: Double?,
    val options: List<OptionForStudentDto>? = null
)

data class OptionForStudentDto(
    val optionId: Int,
    val text: String,
    val displayOrder: Int
)

// ── ATTEMPT ───────────────────────────────────────────────────────────────────

data class StartAttemptResponseDto(
    val attemptId: String,
    val attemptNumber: Int,
    val startedAt: String,
    val timeLimitSec: Int?,
    val test: TestInfoForAttemptDto,
    val questions: List<QuestionForStudentDto>? = null
)

data class TestInfoForAttemptDto(
    val id: String,
    val title: String
)

data class SubmitRequestDto(
    val answers: List<SubmitAnswerDto>
)

data class SubmitAnswerDto(
    val questionId: Int,
    val selectedOptionIds: List<Int>,
    val timeSpent: Double?
)

data class AttemptReportDTO(
    val attemptId: String,
    val score: Double?,
    val scorePercent: Double?,
    val passed: Boolean?,
    val completedAt: String?,
    val questions: List<QuestionForAttemptReportDTO>? = null
)

data class QuestionForAttemptReportDTO(
    val questionId: Int,
    val questionType: String? = null,
    @com.google.gson.annotations.SerializedName("content", alternate = ["text"])
    val content: String? = null,
    val selectedOptionIds: List<Int>? = null,
    val correctOptionIds: List<Int>? = null,
    val correct: Boolean = false
)

