package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.CourseStatsDto
import com.adaptive_tutor_mobile.data.remote.dto.MyTestStatsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface StatsApi {
    @GET("/api/v1/students/me/courses/{courseId}/stats")
    suspend fun getCourseStats(
        @Path("courseId") courseId: String
    ): Response<CourseStatsDto>

    @GET("/api/v1/students/me/tests/{testId}/stats")
    suspend fun getTestStats(
        @Path("testId") testId: String
    ): Response<MyTestStatsDto>
}
