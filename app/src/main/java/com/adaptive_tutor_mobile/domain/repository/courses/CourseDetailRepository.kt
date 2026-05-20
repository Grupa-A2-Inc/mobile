package com.adaptive_tutor_mobile.domain.repository.courses

import com.adaptive_tutor_mobile.domain.model.courses.CourseDetail
import com.adaptive_tutor_mobile.domain.model.lesson.LessonWithContent

interface CourseDetailRepository {
    suspend fun getCourseFullView(courseId: String): Result<CourseDetail>
    fun getCachedLessonWithContent(lessonId: String): LessonWithContent?
}
