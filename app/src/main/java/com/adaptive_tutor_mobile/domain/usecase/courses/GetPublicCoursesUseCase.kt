package com.adaptive_tutor_mobile.domain.usecase.courses

import com.adaptive_tutor_mobile.domain.model.courses.PagedCourses
import com.adaptive_tutor_mobile.domain.repository.courses.CourseRepository
import javax.inject.Inject

class GetPublicCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(page: Int, size: Int): Result<PagedCourses> {
        return courseRepository.getPublicCourses(page, size)
    }
}
