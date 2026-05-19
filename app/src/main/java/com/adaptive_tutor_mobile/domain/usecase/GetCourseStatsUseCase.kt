package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.CourseStats
import com.adaptive_tutor_mobile.domain.repository.StatsRepository
import javax.inject.Inject

class GetCourseStatsUseCase @Inject constructor(
    private val repository: StatsRepository
) {
    suspend operator fun invoke(courseId: String): Result<CourseStats> {
        return repository.getCourseStats(courseId)
    }
}
