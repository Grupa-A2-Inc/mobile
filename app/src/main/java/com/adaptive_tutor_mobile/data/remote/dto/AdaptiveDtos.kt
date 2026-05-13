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
