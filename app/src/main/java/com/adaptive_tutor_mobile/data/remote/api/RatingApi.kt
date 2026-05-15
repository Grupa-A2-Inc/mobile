package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.RatingSummaryDto
import com.adaptive_tutor_mobile.data.remote.dto.SubmitRatingDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RatingApi {

    @POST("api/v1/lessons/{lessonId}/ratings")
    suspend fun submitRating(
        @Path("lessonId") lessonId: String,
        @Body dto: SubmitRatingDto
    ): Response<Unit>

    @GET("api/v1/lessons/{lessonId}/ratings/summary")
    suspend fun getRatingSummary(
        @Path("lessonId") lessonId: String
    ): Response<RatingSummaryDto>
}