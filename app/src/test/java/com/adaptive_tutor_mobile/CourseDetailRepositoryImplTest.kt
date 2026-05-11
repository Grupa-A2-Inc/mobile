package com.adaptive_tutor_mobile

import com.adaptive_tutor_mobile.data.remote.api.CourseDetailApi
import com.adaptive_tutor_mobile.data.remote.dto.ChapterFullViewDTO
import com.adaptive_tutor_mobile.data.remote.dto.LessonFullViewDTO
import com.adaptive_tutor_mobile.data.remote.dto.ResponseCourseFullViewDto
import com.adaptive_tutor_mobile.data.repository.CourseDetailRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CourseDetailRepositoryImplTest {

    private val api: CourseDetailApi = mock()
    private val repository = CourseDetailRepositoryImpl(api)

    // Helper builders

    private fun lessonDto(id: String, title: String, orderIndex: Int, testId: String? = null) =
        LessonFullViewDTO(
            id              = id,
            chapterId       = "ch_parent",
            testId          = testId,
            title           = title,
            contentMarkdown = null,
            orderIndex      = orderIndex,
            lessonResources = emptyList()
        )

    private fun chapterDto(
        id: String,
        title: String,
        orderIndex: Int,
        lessons: List<LessonFullViewDTO> = emptyList()
    ) = ChapterFullViewDTO(
        id         = id,
        courseId   = "c1",
        title      = title,
        orderIndex = orderIndex,
        lessons    = lessons
    )

    private fun courseDto(
        id: String = "c1",
        title: String = "Course Title",
        description: String? = "Description",
        chapters: List<ChapterFullViewDTO> = emptyList()
    ) = ResponseCourseFullViewDto(
        id          = id,
        title       = title,
        description = description,
        status      = "PUBLISHED",
        visibility  = "PUBLIC",
        createdAt   = null,
        chapters    = chapters
    )

    // Tests

    @Test
    fun `getCourseFullView returns success with mapped domain model`() = runTest {
        val dto = courseDto(
            chapters = listOf(
                chapterDto(
                    id = "ch1", title = "Chapter 1", orderIndex = 0,
                    lessons = listOf(lessonDto("l1", "Lesson 1", 0))
                )
            )
        )
        whenever(api.getCourseFullView("c1")).thenReturn(dto)

        val result = repository.getCourseFullView("c1")

        assertTrue(result.isSuccess)
        val detail = result.getOrThrow()
        assertEquals("c1", detail.id)
        assertEquals("Course Title", detail.title)
        assertEquals(1, detail.chapters.size)
        assertEquals("Chapter 1", detail.chapters.first().title)
        assertEquals(1, detail.chapters.first().lessons.size)
        assertEquals("Lesson 1", detail.chapters.first().lessons.first().title)
    }

    @Test
    fun `getCourseFullView returns failure on exception`() = runTest {
        whenever(api.getCourseFullView("c1")).thenThrow(RuntimeException("Network error"))

        val result = repository.getCourseFullView("c1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getCourseFullView maps hasTest correctly when testId is present`() = runTest {
        val dto = courseDto(
            chapters = listOf(
                chapterDto(
                    id = "ch1", title = "Chapter 1", orderIndex = 0,
                    lessons = listOf(
                        lessonDto("l1", "With test", 0, testId = "t1"),
                        lessonDto("l2", "No test",   1, testId = null)
                    )
                )
            )
        )
        whenever(api.getCourseFullView("c1")).thenReturn(dto)

        val lessons = repository.getCourseFullView("c1").getOrThrow().chapters.first().lessons

        assertTrue(lessons[0].hasTest)
        assertTrue(!lessons[1].hasTest)
    }

    @Test
    fun `getCourseFullView sorts lessons by orderIndex`() = runTest {
        val dto = courseDto(
            chapters = listOf(
                chapterDto(
                    id = "ch1", title = "Chapter 1", orderIndex = 0,
                    lessons = listOf(
                        lessonDto("l3", "Third",  2),
                        lessonDto("l1", "First",  0),
                        lessonDto("l2", "Second", 1)
                    )
                )
            )
        )
        whenever(api.getCourseFullView("c1")).thenReturn(dto)

        val lessons = repository.getCourseFullView("c1").getOrThrow().chapters.first().lessons

        assertEquals("First",  lessons[0].title)
        assertEquals("Second", lessons[1].title)
        assertEquals("Third",  lessons[2].title)
    }

    @Test
    fun `getCourseFullView sorts chapters by orderIndex`() = runTest {
        val dto = courseDto(
            chapters = listOf(
                chapterDto(id = "ch2", title = "Second", orderIndex = 1),
                chapterDto(id = "ch1", title = "First",  orderIndex = 0)
            )
        )
        whenever(api.getCourseFullView("c1")).thenReturn(dto)

        val chapters = repository.getCourseFullView("c1").getOrThrow().chapters

        assertEquals("First",  chapters[0].title)
        assertEquals("Second", chapters[1].title)
    }

    @Test
    fun `getCourseFullView maps course with no chapters`() = runTest {
        whenever(api.getCourseFullView("c1")).thenReturn(courseDto())

        val result = repository.getCourseFullView("c1")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().chapters.isEmpty())
    }

    @Test
    fun `getCourseFullView maps null description to empty string`() = runTest {
        whenever(api.getCourseFullView("c1")).thenReturn(courseDto(description = null))

        val result = repository.getCourseFullView("c1")

        assertEquals("", result.getOrThrow().description)
    }
}