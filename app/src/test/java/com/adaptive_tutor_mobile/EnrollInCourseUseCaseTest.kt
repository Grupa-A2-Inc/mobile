package com.adaptive_tutor_mobile

import com.adaptive_tutor_mobile.domain.repository.courses.CourseRepository
import com.adaptive_tutor_mobile.domain.usecase.courses.EnrollInCourseUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class EnrollInCourseUseCaseTest {

    private val repository: CourseRepository = mock()
    private val useCase = EnrollInCourseUseCase(repository)

    @Test
    fun `invoke returns success when enroll succeeds`() = runTest {
        whenever(repository.enrollInCourse("course1")).thenReturn(Result.success(Unit))

        val result = useCase("course1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when enroll fails`() = runTest {
        whenever(repository.enrollInCourse("course1")).thenReturn(Result.failure(Exception("Error")))

        val result = useCase("course1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke delegates to repository with different courseId`() = runTest {
        whenever(repository.enrollInCourse("course2")).thenReturn(Result.success(Unit))

        val result = useCase("course2")

        assertTrue(result.isSuccess)
    }
}