package com.adaptive_tutor_mobile

import com.adaptive_tutor_mobile.domain.model.Course
import com.adaptive_tutor_mobile.domain.repository.CourseRepository
import com.adaptive_tutor_mobile.domain.usecase.GetPublicCoursesUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetPublicCoursesUseCaseTest {

    private val repository: CourseRepository = mock()
    private val useCase = GetPublicCoursesUseCase(repository)

    @Test
    fun `invoke returns success with courses`() = runTest {
        val courses = listOf(
            Course("1", "Math", "Desc", "Science", "PUBLISHED", "PUBLIC")
        )
        whenever(repository.getPublicCourses(0, 10)).thenReturn(Result.success(courses))

        val result = useCase(0, 10)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        whenever(repository.getPublicCourses(0, 10)).thenReturn(Result.failure(Exception("Error")))

        val result = useCase(0, 10)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke delegates to repository with correct params`() = runTest {
        whenever(repository.getPublicCourses(1, 5)).thenReturn(Result.success(emptyList()))

        val result = useCase(1, 5)

        assertTrue(result.isSuccess)
    }
}