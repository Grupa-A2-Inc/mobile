package com.adaptive_tutor_mobile

import com.adaptive_tutor_mobile.domain.repository.RatingRepository
import com.adaptive_tutor_mobile.domain.usecase.SubmitLessonRatingUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SubmitLessonRatingUseCaseTest {

    private val ratingRepository: RatingRepository = mock()
    private val useCase = SubmitLessonRatingUseCase(ratingRepository)

    @Test
    fun `invoke returns success when rating is valid`() = runTest {
        whenever(ratingRepository.submitRating("l1", 5, null)).thenReturn(Result.success(Unit))

        val result = useCase("l1", 5, null)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when rating is 0`() = runTest {
        val result = useCase("l1", 0, null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns failure when rating is 6`() = runTest {
        val result = useCase("l1", 6, null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        whenever(ratingRepository.submitRating("l1", 3, "comment"))
            .thenReturn(Result.failure(Exception("Error")))

        val result = useCase("l1", 3, "comment")

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns success with comment`() = runTest {
        whenever(ratingRepository.submitRating("l1", 4, "Great lesson"))
            .thenReturn(Result.success(Unit))

        val result = useCase("l1", 4, "Great lesson")

        assertTrue(result.isSuccess)
    }
}