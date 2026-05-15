package com.adaptive_tutor_mobile.data.remote.dto

data class SubmitRatingDto(
    val rating: Int,
    val comment: String?
)

data class RatingSummaryDto(
    val avgRating: Float,
    val totalRatings: Int
)