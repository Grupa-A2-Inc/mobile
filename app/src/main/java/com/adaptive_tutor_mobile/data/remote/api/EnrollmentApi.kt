package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.EnrollmentDto
import com.adaptive_tutor_mobile.data.remote.dto.PageResponseCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.ResponseCourseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EnrollmentApi {

    @GET("api/v1/courses/public")
    suspend fun getPublicCourses(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageResponseCourseDto>

    @POST("api/v1/courses/{courseId}/enroll")
    suspend fun enrollInCourse(
        @Path("courseId") courseId: String
    ): Response<EnrollmentDto>
}