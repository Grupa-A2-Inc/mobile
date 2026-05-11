package com.adaptive_tutor_mobile.domain.usecase

import com.adaptive_tutor_mobile.domain.model.CourseDetail
import com.adaptive_tutor_mobile.domain.repository.CourseDetailRepository
import javax.inject.Inject

class GetCourseFullViewUseCase @Inject constructor(
    private val repository: CourseDetailRepository
) {
    suspend operator fun invoke(courseId: String): Result<CourseDetail> =
        repository.getCourseFullView(courseId)
}