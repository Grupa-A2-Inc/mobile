package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.RatingApi
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRatingDto
import com.adaptive_tutor_mobile.domain.model.lesson.RatingSummary
import com.adaptive_tutor_mobile.domain.repository.lesson.RatingRepository
import javax.inject.Inject

class RatingRepositoryImpl @Inject constructor(
    private val ratingApi: RatingApi
) : RatingRepository {

    override suspend fun submitRating(lessonId: String, rating: Int, comment: String?): Result<Unit> {
        return try {
            val response = ratingApi.submitRating(lessonId, SubmitRatingDto(rating, comment))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRatingSummary(lessonId: String): Result<RatingSummary> {
        return try {
            val response = ratingApi.getRatingSummary(lessonId)
            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(RatingSummary(dto.avgRating, dto.totalRatings))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
