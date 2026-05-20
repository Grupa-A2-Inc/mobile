package com.adaptive_tutor_mobile.domain.repository.profile

import com.adaptive_tutor_mobile.domain.model.profile.UserProfile

interface UserRepository {
    suspend fun getProfile(userId: String): Result<UserProfile>

    suspend fun updateProfile(
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        organizationId: String?
    ): Result<UserProfile>

    suspend fun changePassword(
        userId: String,
        currentPassword: String,
        newPassword: String,
        newPasswordConfirm: String
    ): Result<Unit>
}
