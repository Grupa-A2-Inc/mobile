package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.CourseDetail

fun interface CourseDetailRepository {
    suspend fun getCourseFullView(courseId: String): Result<CourseDetail>
}