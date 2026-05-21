package com.adaptive_tutor_mobile.domain.repository.lesson

import com.adaptive_tutor_mobile.domain.model.lesson.LessonDetail

interface LessonRepository {
    suspend fun getLessonDetail(lessonId: String): Result<LessonDetail>
    suspend fun checkLessonTest(lessonId: String): String?
    suspend fun markVisited(lessonId: String)
}
