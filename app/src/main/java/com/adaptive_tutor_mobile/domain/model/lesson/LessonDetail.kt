package com.adaptive_tutor_mobile.domain.model.lesson

data class LessonDetail(
    val id: String,
    val title: String,
    val contentMarkdown: String,
    val resources: List<LessonResource>
)

data class LessonResource(
    val id: String,
    val title: String,
    val url: String
)
