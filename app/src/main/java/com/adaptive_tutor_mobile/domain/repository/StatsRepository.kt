package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.CourseStats

fun interface StatsRepository {
    suspend fun getCourseStats(courseId: String): Result<CourseStats>
}
