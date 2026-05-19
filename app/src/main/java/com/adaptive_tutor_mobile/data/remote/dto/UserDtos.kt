package com.adaptive_tutor_mobile.data.remote.dto

data class UserProfileDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val organizationName: String?,
    val country: String?,
    val city: String?
)

data class UpdateUserDto(
    val email: String,
    val firstName: String,
    val lastName: String,
    val organizationId: String?
)

data class ChangePasswordDto(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirm: String
)