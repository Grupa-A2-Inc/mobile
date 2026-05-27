package com.adaptive_tutor_mobile.domain.model.courses

data class CourseDetail(
    val id: String,
    val title: String,
    val description: String,
    val visibility: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val id: String,
    val title: String,
    val lessons: List<LessonSummary>
)

data class LessonSummary(
    val id: String,
    val title: String,
    val hasTest: Boolean
)
