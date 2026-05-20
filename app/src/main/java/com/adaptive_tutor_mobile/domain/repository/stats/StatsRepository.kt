package com.adaptive_tutor_mobile.domain.repository.stats

import com.adaptive_tutor_mobile.domain.model.stats.CourseStats

fun interface StatsRepository {
    suspend fun getCourseStats(courseId: String): Result<CourseStats>
}
