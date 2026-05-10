package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.Course

interface CourseRepository {
    suspend fun getPublicCourses(page: Int, size: Int): Result<List<Course>>
    suspend fun enrollInCourse(courseId: String): Result<Unit>
}