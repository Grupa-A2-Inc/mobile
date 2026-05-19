package com.adaptive_tutor_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AdaptiveStartRequestDto(
    val subjectId: Int,
    val topicId: Int,
    val count: Int
)

data class AdaptiveStartResponseDto(
    val sessionId: String,
    val attemptId: String? = null,
    val expiresAt: String?,
    @SerializedName("exercises", alternate = ["questions"])
    val exercises: List<AdaptiveExerciseStudentDto>? = null
)

// Dedicated DTO for adaptive questions to preserve String IDs
data class AdaptiveQuestionForStudentDto(
    val questionId: String,
    @SerializedName("questionType", alternate = ["type"])
    val questionType: String? = null,
    @SerializedName("content", alternate = ["text"])
    val content: String? = null,
    val difficulty: Double?,
    val options: List<OptionForStudentDto>? = null
)

// Dedicated DTOs for adaptive submission to match OpenAPI spec
data class AdaptiveSubmitRequestDto(
    val answers: List<AdaptiveSubmitAnswerDto>
)

data class AdaptiveSubmitAnswerDto(
    @SerializedName("exerciseId")
    val questionId: String,
    @SerializedName("givenAnswers")
    val selectedOptionIds: List<String>,
    @SerializedName("timeSpent")
    val timeSpent: Int?
)

data class AdaptiveAttemptReportDTO(
    @SerializedName("sessionId")
    val attemptId: String,
    @SerializedName("totalScore")
    val score: Double?,
    val scorePercent: Double?,
    val passed: Boolean?,
    val completedAt: String?,
    @SerializedName("clientResults")
    val questions: List<AdaptiveQuestionForAttemptReportDTO>? = null
)

data class AdaptiveQuestionForAttemptReportDTO(
    @SerializedName("mlExerciseId")
    val questionId: String,
    val questionType: String? = null,
    val content: String? = null,
    @SerializedName("givenAnswers")
    val selectedOptionIds: List<String>? = null,
    @SerializedName("correctAnswers")
    val correctOptionIds: List<String>? = null,
    val correct: Boolean = false
)

// Handles both old format (exerciseId + answers: List<String>)
// and new format (questionId + options: List<OptionForStudentDto>)
data class AdaptiveExerciseStudentDto(
    @SerializedName("questionId", alternate = ["exerciseId"])
    val questionId: String? = null,
    @SerializedName("content", alternate = ["text"])
    val content: String? = null,
    @SerializedName("questionType", alternate = ["type"])
    val questionType: String? = null,
    val difficulty: Double? = null,
    val options: List<OptionForStudentDto>? = null,
    val answers: List<String>? = null
)
