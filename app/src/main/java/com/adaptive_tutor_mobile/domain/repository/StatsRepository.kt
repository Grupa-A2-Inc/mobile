package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.CourseStats

interface StatsRepository {
    suspend fun getCourseStats(courseId: String): Result<CourseStats>
}
