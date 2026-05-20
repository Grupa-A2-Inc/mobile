package com.adaptive_tutor_mobile.domain.repository.lesson

import com.adaptive_tutor_mobile.domain.model.lesson.RatingSummary

interface RatingRepository {
    suspend fun submitRating(lessonId: String, rating: Int, comment: String?): Result<Unit>
    suspend fun getRatingSummary(lessonId: String): Result<RatingSummary>
}
