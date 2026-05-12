package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.CompletedCourseDto
import com.adaptive_tutor_mobile.data.remote.dto.ProgressWithLessonListDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProgressApi {

    /**
     * GET /api/v1/courses/{courseId}/my-progress
     * Returnează progresul detaliat al studentului curent pentru un curs.
     */
    @GET("api/v1/courses/{courseId}/my-progress")
    suspend fun getMyProgress(@Path("courseId") courseId: String): Response<ProgressWithLessonListDto>

    /**
     * GET /api/v1/students/me/completed-courses
     * Returnează cursurile finalizate de studentul curent.
     */
    @GET("api/v1/students/me/completed-courses")
    suspend fun getCompletedCourses(): Response<List<CompletedCourseDto>>
}