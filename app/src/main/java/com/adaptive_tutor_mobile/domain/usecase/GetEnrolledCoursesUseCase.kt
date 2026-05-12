package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.EnrolledCourse
import com.adaptive_tutor_mobile.domain.repository.ProgressRepository
import javax.inject.Inject

class GetEnrolledCoursesUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(): Result<List<EnrolledCourse>> =
        repository.getEnrolledCourses()
}