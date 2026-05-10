package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.repository.CourseRepository
import javax.inject.Inject

class EnrollInCourseUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(courseId: String): Result<Unit> {
        return courseRepository.enrollInCourse(courseId)
    }
}