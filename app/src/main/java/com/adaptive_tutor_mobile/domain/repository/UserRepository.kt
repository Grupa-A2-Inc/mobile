package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.UserProfile

interface UserRepository {
    suspend fun getProfile(userId: String): Result<UserProfile>
    suspend fun updateProfile(userId: String, firstName: String, lastName: String, city: String?): Result<UserProfile>
    suspend fun changePassword(userId: String, currentPassword: String, newPassword: String, confirmPassword: String): Result<Unit>
}