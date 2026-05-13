package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.ResponseCourseFullViewDto
import retrofit2.http.GET
import retrofit2.http.Path

fun interface CourseDetailApi {

    @GET("api/v1/courses/{courseId}/full-view")
    suspend fun getCourseFullView(
        @Path("courseId") courseId: String
    ): ResponseCourseFullViewDto
}