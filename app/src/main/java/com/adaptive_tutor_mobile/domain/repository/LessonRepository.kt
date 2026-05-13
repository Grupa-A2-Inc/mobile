package com.adaptive_tutor_mobile.domain.repository

import com.adaptive_tutor_mobile.domain.model.LessonDetail

interface LessonRepository {
    suspend fun getLessonDetail(lessonId: String): Result<LessonDetail>
    suspend fun checkLessonTest(lessonId: String): String?
}