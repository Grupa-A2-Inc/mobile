package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.CourseDetail
import com.adaptive_tutor_mobile.domain.model.LessonWithContent

interface CourseDetailRepository {
    suspend fun getCourseFullView(courseId: String): Result<CourseDetail>

    //------ Dev 5 ------
    fun getCachedLessonWithContent(lessonId: String): LessonWithContent?
}