package com.adaptive_tutor_mobile.data.remote.api

import com.adaptive_tutor_mobile.data.remote.dto.UpdateUserDto
import com.adaptive_tutor_mobile.data.remote.dto.ChangePasswordDto
import com.adaptive_tutor_mobile.data.remote.dto.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {

    @GET("api/v1/users/{id}")
    suspend fun getUserById(
        @Path("id") id: String
    ): Response<UserProfileDto>

    @PUT("api/v1/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body dto: UpdateUserDto
    ): Response<UserProfileDto>

    @PATCH("api/v1/users/{id}/change-password")
    suspend fun changePassword(
        @Path("id") id: String,
        @Body dto: ChangePasswordDto
    ): Response<Unit>
}