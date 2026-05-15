package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.UserApi
import com.adaptive_tutor_mobile.data.remote.dto.ChangePasswordDto
import com.adaptive_tutor_mobile.data.remote.dto.UpdateUserDto
import com.adaptive_tutor_mobile.domain.model.UserProfile
import com.adaptive_tutor_mobile.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    override suspend fun getProfile(userId: String): Result<UserProfile> {
        return try {
            val response = userApi.getUserById(userId)
            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(
                    UserProfile(
                        id = dto.id,
                        firstName = dto.firstName,
                        lastName = dto.lastName,
                        email = dto.email,
                        organizationName = dto.organizationName,
                        country = dto.country,
                        city = dto.city
                    )
                )
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(userId: String, firstName: String, lastName: String, city: String?): Result<UserProfile> {
        return try {
            val response = userApi.updateUser(userId, UpdateUserDto(firstName, lastName, city))
            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(
                    UserProfile(
                        id = dto.id,
                        firstName = dto.firstName,
                        lastName = dto.lastName,
                        email = dto.email,
                        organizationName = dto.organizationName,
                        country = dto.country,
                        city = dto.city
                    )
                )
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(userId: String, currentPassword: String, newPassword: String, confirmPassword: String): Result<Unit> {
        return try {
            val response = userApi.changePassword(userId, ChangePasswordDto(currentPassword, newPassword, confirmPassword))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}