package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.Course
import com.adaptive_tutor_mobile.domain.model.PagedCourses

interface CourseRepository {
    suspend fun getPublicCourses(page: Int, size: Int): Result<PagedCourses>
    suspend fun enrollInCourse(courseId: String): Result<Unit>
    suspend fun unenrollFromCourse(courseId: String): Result<Unit>
}
