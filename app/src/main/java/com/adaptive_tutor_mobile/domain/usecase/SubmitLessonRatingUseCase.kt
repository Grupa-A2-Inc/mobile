package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.repository.RatingRepository
import javax.inject.Inject

class SubmitLessonRatingUseCase @Inject constructor(
    private val ratingRepository: RatingRepository
) {
    suspend operator fun invoke(lessonId: String, rating: Int, comment: String?): Result<Unit> {
        if (rating < 1 || rating > 5) {
            return Result.failure(Exception("Rating-ul trebuie să fie între 1 și 5"))
        }
        return ratingRepository.submitRating(lessonId, rating, comment)
    }
}
