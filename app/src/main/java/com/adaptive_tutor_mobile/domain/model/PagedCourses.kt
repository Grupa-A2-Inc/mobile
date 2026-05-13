package com.adaptive_tutor_mobile.domain.model

data class PagedCourses(
    val courses: List<Course>,
    val totalPages: Int,
    val currentPage: Int
)