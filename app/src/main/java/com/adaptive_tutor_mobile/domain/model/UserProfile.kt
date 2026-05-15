package com.adaptive_tutor_mobile.domain.model

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val organizationName: String?,
    val country: String?,
    val city: String?
)