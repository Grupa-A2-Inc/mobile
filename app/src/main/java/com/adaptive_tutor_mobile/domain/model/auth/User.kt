package com.adaptive_tutor_mobile.domain.model.auth

import com.adaptive_tutor_mobile.data.remote.dto.UserDataResponse

enum class UserRole {
    ADMIN, ORGANIZATION_ADMIN, TEACHER, STUDENT, PARENT, UNKNOWN
}

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: UserRole,
    val status: String,
    val organizationId: String?,
    val organizationName: String?
)

fun UserDataResponse.toDomain() = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    role = UserRole.entries.find { it.name == role } ?: UserRole.UNKNOWN,
    status = status,
    organizationId = organizationId,
    organizationName = organizationName
)
