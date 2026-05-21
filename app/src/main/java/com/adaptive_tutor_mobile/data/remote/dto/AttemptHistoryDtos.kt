package com.adaptive_tutor_mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AttemptStatusDto(
    @SerializedName("attemptID")
    val attemptId: String,
    val attemptNumber: Int,
    val score: Double,
    val scorePercent: Double,
    val passed: Boolean,
    val startedAt: String,
    val status: String
)
