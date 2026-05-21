package com.adaptive_tutor_mobile

import com.adaptive_tutor_mobile.data.remote.api.RatingApi
import com.adaptive_tutor_mobile.data.remote.dto.RatingSummaryDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRatingDto
import com.adaptive_tutor_mobile.data.repository.RatingRepositoryImpl
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class RatingRepositoryImplTest {

    private val ratingApi: RatingApi = mock()
    private val repository = RatingRepositoryImpl(ratingApi)

    @Test
    fun `submitRating returns success`() = runTest {
        whenever(ratingApi.submitRating("l1", SubmitRatingDto(5, null)))
            .thenReturn(Response.success(null))

        val result = repository.submitRating("l1", 5, null)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `submitRating returns failure on error response`() = runTest {
        whenever(ratingApi.submitRating("l1", SubmitRatingDto(5, null)))
            .thenReturn(Response.error(400, ResponseBody.create(null, "")))

        val result = repository.submitRating("l1", 5, null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `submitRating returns failure on exception`() = runTest {
        whenever(ratingApi.submitRating("l1", SubmitRatingDto(5, null)))
            .thenThrow(RuntimeException("Network error"))

        val result = repository.submitRating("l1", 5, null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getRatingSummary returns success with mapped data`() = runTest {
        val dto = RatingSummaryDto(avgRating = 4.5f, totalRatings = 10)
        whenever(ratingApi.getRatingSummary("l1")).thenReturn(Response.success(dto))

        val result = repository.getRatingSummary("l1")

        assertTrue(result.isSuccess)
        assertEquals(4.5f, result.getOrNull()?.avgRating)
        assertEquals(10, result.getOrNull()?.totalRatings)
    }

    @Test
    fun `getRatingSummary returns failure on error response`() = runTest {
        whenever(ratingApi.getRatingSummary("l1"))
            .thenReturn(Response.error(404, ResponseBody.create(null, "")))

        val result = repository.getRatingSummary("l1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getRatingSummary returns failure on exception`() = runTest {
        whenever(ratingApi.getRatingSummary("l1")).thenThrow(RuntimeException("Network error"))

        val result = repository.getRatingSummary("l1")

        assertTrue(result.isFailure)
    }
}