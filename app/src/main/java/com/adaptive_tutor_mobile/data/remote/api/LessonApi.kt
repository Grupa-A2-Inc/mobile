package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.LessonDtoEntity
import com.adaptive_tutor_mobile.data.remote.dto.ResponseLessonResourceDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface LessonApi {
    @GET("/api/v1/lessons/{id}")
    suspend fun getLessonById(@Path("id") id: String): LessonDtoEntity

    @GET("/api/v1/lessons/{id}/content")
    suspend fun getLessonContent(@Path("id") id: String): ResponseBody

    @GET("/api/v1/lessons/{id}/resources")
    suspend fun getResources(@Path("id") id: String): List<ResponseLessonResourceDto>
}