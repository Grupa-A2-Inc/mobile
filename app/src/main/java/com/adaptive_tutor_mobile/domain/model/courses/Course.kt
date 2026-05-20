package com.adaptive_tutor_mobile.domain.model.courses

data class Course(
    val id: String,
    val title: String,
    val description: String?,
    val category: String?,
    val status: String,
    val visibility: String
)
