package com.adaptive_tutor_mobile.domain.model.courses

data class PagedCourses(
    val courses: List<Course>,
    val totalPages: Int,
    val currentPage: Int
)
