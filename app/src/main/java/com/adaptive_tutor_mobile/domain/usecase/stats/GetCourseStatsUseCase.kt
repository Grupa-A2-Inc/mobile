package com.adaptive_tutor_mobile.domain.usecase.stats

import com.adaptive_tutor_mobile.domain.model.stats.CourseStats
import com.adaptive_tutor_mobile.domain.repository.stats.StatsRepository
import javax.inject.Inject

class GetCourseStatsUseCase @Inject constructor(
    private val repository: StatsRepository
) {
    suspend operator fun invoke(courseId: String): Result<CourseStats> {
        return repository.getCourseStats(courseId)
    }
}
