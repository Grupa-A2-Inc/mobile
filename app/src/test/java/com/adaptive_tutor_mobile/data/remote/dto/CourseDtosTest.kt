package com.adaptive_tutor_mobile.data.remote.dto

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class CourseDtosTest {

    @Test
    fun `course dto request models store constructor values`() {
        val createLessonResource = CreateLessonResourceDto("Slides", "https://example.com/slides")
        val createLesson = CreateLessonDTO("Lesson", "Markdown", 1, listOf(createLessonResource))
        val createChapter = CreateChapterDTO("Chapter", 2, listOf(createLesson))
        val createCourse = CreateCourseDto("Course", "Desc", "Math", "DRAFT", listOf(createChapter))
        val updateCourse = UpdateCourseDto("Updated", "New desc", "Science", "PUBLISHED")

        assertEquals("Slides", createLessonResource.title)
        assertEquals("Lesson", createLesson.title)
        assertEquals(1, createLesson.orderIndex)
        assertEquals("Chapter", createChapter.title)
        assertEquals(2, createChapter.orderIndex)
        assertEquals("Course", createCourse.title)
        assertEquals("Math", createCourse.category)
        assertEquals("Updated", updateCourse.title)
        assertEquals("PUBLISHED", updateCourse.status)
    }

    @Test
    fun `course dto entity and metadata models store nullable fields`() {
        val chapter = ChapterDtoResponse(id = "c1", title = "Intro", orderIndex = 1)
        val lessonEntity = LessonDtoEntity(
            id = "l1",
            chapterID = "c1",
            title = "Lesson",
            contentMarkdown = null,
            orderIndex = 3,
            createdAt = null,
            updatedAt = null
        )
        val lessonPost = LessonDtoPost(title = "Post title", contentMarkdown = null)
        val metadata = LessonDtoMetadata(title = null, orderIndex = null)
        val enrollment = EnrollmentDto("e1", "course1", "student1", "2026-01-01", 42.5)

        assertEquals("Intro", chapter.title)
        assertEquals("l1", lessonEntity.id)
        assertNull(lessonEntity.contentMarkdown)
        assertEquals("Post title", lessonPost.title)
        assertNull(metadata.title)
        assertNull(metadata.orderIndex)
        assertEquals(42.5, enrollment.progressPercent)
    }
}
