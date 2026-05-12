package com.adaptive_tutor_mobile.data.remote.dto

// ── COURSE ───────────────────────────────────────────────────────────────────

data class ResponseCourseDto(
    val id: String,
    val title: String,
    val description: String?,
    val category: String?,
    val status: String,
    val visibility: String,
    val createdBy: String?
)

data class CreateCourseDto(
    val title: String,
    val description: String?,
    val category: String?,
    val status: String,
    val chapters: List<CreateChapterDTO>? = null
)

data class UpdateCourseDto(
    val title: String?,
    val description: String?,
    val category: String?,
    val status: String?
)

data class ResponseCourseFullViewDto(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val visibility: String,
    val createdAt: String?,
    val chapters: List<ChapterFullViewDTO>
)

data class PageResponseCourseDto(
    val content: List<ResponseCourseDto>,
    val totalPages: Int,
    val totalElements: Int,
    val number: Int,
    val size: Int
)

// ── CHAPTER ──────────────────────────────────────────────────────────────────

data class ChapterFullViewDTO(
    val id: String,
    val courseId: String,
    val title: String,
    val orderIndex: Int,
    val lessons: List<LessonFullViewDTO>
)

data class ChapterDtoResponse(
    val id: String,
    val title: String,
    val orderIndex: Int
)

data class CreateChapterDTO(
    val title: String,
    val orderIndex: Int,
    val lessons: List<CreateLessonDTO>? = null
)

// ── LESSON ───────────────────────────────────────────────────────────────────

data class LessonFullViewDTO(
    val id: String,
    val chapterId: String,
    val testId: String?,
    val title: String,
    val contentMarkdown: String?,
    val orderIndex: Int,
    val lessonResources: List<ResponseLessonResourceDto>
)

data class LessonDtoEntity(
    val id: String,
    val chapterID: String,
    val title: String,
    val contentMarkdown: String?,
    val orderIndex: Int,
    val createdAt: String?,
    val updatedAt: String?
)

data class LessonDtoPost(
    val title: String,
    val contentMarkdown: String?
)

data class LessonDtoMetadata(
    val title: String?,
    val orderIndex: Int?
)

data class CreateLessonDTO(
    val title: String,
    val contentMarkdown: String?,
    val orderIndex: Int,
    val lessonResources: List<CreateLessonResourceDto>? = null
)

// ── LESSON RESOURCE ──────────────────────────────────────────────────────────

data class ResponseLessonResourceDto(
    val id: String,
    val lessonId: String,
    val title: String,
    val url: String
)

data class CreateLessonResourceDto(
    val title: String,
    val url: String
)

// ── ENROLLMENT ───────────────────────────────────────────────────────────────

data class EnrollmentDto(
    val enrollmentId: String,
    val courseId: String,
    val studentId: String,
    val enrolledAt: String,
    val progressPercent: Double?
)

data class EnrolledCourseDto(
    val unrollmentId: String,
    val courseId: String,
    val courseTitle: String,
    val courseCategory: String?,
    val enrolledAt: String,
    val progressPercent: Double?,
    val completedAt: String?
)

// ── PROGRESS ─────────────────────────────────────────────────────────────────

//data class ProgressWithLessonListDto(
//    val totalLessons: Int,
//    val visitedLessons: Int,
//    val progressPercent: Double,
//    val completedAt: String?,
//    val lessons: List<LessonStatusDto>
//)
//
//data class LessonStatusDto(
//    val lessonId: String,
//    val title: String,
//    val visited: Boolean,
//    val visitedAt: String?
//)
