package com.adaptive_tutor_mobile.domain.repository.stats

import com.adaptive_tutor_mobile.data.remote.dto.ProgressWithLessonListDto
import com.adaptive_tutor_mobile.domain.model.courses.EnrolledCourse

interface ProgressRepository {
    suspend fun getEnrolledCourses(): Result<List<EnrolledCourse>>
    suspend fun getCompletedCourses(): Result<List<EnrolledCourse>>
    suspend fun getMyProgress(courseId: String): Result<ProgressWithLessonListDto>
}
