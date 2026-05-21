package com.adaptive_tutor_mobile.domain.usecase.courses

import com.adaptive_tutor_mobile.domain.model.courses.EnrolledCourse
import com.adaptive_tutor_mobile.domain.repository.stats.ProgressRepository
import javax.inject.Inject

class GetEnrolledCoursesUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(): Result<List<EnrolledCourse>> =
        repository.getEnrolledCourses()
}
