package com.adaptive_tutor_mobile.domain.usecase.courses

import com.adaptive_tutor_mobile.domain.repository.courses.CourseRepository
import javax.inject.Inject

class UnenrollFromCourseUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(courseId: String): Result<Unit> {
        return courseRepository.unenrollFromCourse(courseId)
    }
}
