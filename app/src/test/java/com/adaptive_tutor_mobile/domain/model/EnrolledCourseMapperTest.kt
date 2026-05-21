package com.adaptive_tutor_mobile.domain.model

import com.adaptive_tutor_mobile.domain.model.courses.EnrolledCourse
import com.adaptive_tutor_mobile.domain.model.courses.toDomain
import com.adaptive_tutor_mobile.ProgressTestFixtures.completedDto
import com.adaptive_tutor_mobile.ProgressTestFixtures.domainCourse
import com.adaptive_tutor_mobile.ProgressTestFixtures.enrolledDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnrolledCourseMapperTest {

    @Test
    fun `EnrolledCourseDto maps all fields when complete`() {
        val dto = enrolledDto(
            courseId = "c1", title = "Math", category = "STEM",
            progress = 60.0, completedAt = null
        )

        val result = dto.toDomain()

        assertEquals("c1", result.courseId)
        assertEquals("Math", result.courseTitle)
        assertEquals("STEM", result.courseCategory)
        assertEquals(60.0, result.progressPercent, 0.001)
        assertEquals("2025-01-10T09:00:00", result.enrolledAt)
        assertNull(result.completedAt)
        assertFalse(result.isCompleted)
    }

    @Test
    fun `EnrolledCourseDto with null progress defaults to zero`() {
        val dto = enrolledDto(progress = null)

        val result = dto.toDomain()

        assertEquals(0.0, result.progressPercent, 0.001)
    }

    @Test
    fun `EnrolledCourseDto with null category is preserved`() {
        val dto = enrolledDto(category = null)

        val result = dto.toDomain()

        assertNull(result.courseCategory)
    }

    @Test
    fun `EnrolledCourseDto with completedAt yields isCompleted true`() {
        val dto = enrolledDto(completedAt = "2025-02-01T12:00:00")

        val result = dto.toDomain()

        assertTrue(result.isCompleted)
        assertEquals("2025-02-01T12:00:00", result.completedAt)
    }

    @Test
    fun `EnrolledCourseDto with blank completedAt maps to null and is not completed`() {
        val dto = enrolledDto(completedAt = " ")

        val result = dto.toDomain()

        assertNull(result.completedAt)
        assertFalse(result.isCompleted)
    }

    @Test
    fun `EnrolledCourseDto with empty completedAt maps to null and is not completed`() {
        val dto = enrolledDto(completedAt = "")

        val result = dto.toDomain()

        assertNull(result.completedAt)
        assertFalse(result.isCompleted)
    }

    @Test
    fun `CompletedCourseDto maps to domain with 100 percent`() {
        val dto = completedDto(courseId = "c5", title = "History")

        val result = dto.toDomain()

        assertEquals("c5", result.courseId)
        assertEquals("History", result.courseTitle)
        assertEquals(100.0, result.progressPercent, 0.001)
        assertNull(result.courseCategory)
        assertTrue(result.isCompleted)
    }

    @Test
    fun `CompletedCourseDto preserves enrolledAt and completedAt`() {
        val dto = completedDto(
            enrolledAt = "2024-09-01T10:00:00",
            completedAt = "2025-01-15T18:30:00"
        )

        val result = dto.toDomain()

        assertEquals("2024-09-01T10:00:00", result.enrolledAt)
        assertEquals("2025-01-15T18:30:00", result.completedAt)
    }

    @Test
    fun `EnrolledCourse isCompleted is false for null empty and blank values`() {
        assertFalse(domainCourse(completedAt = null).isCompleted)
        assertFalse(domainCourse(completedAt = "").isCompleted)
        assertFalse(domainCourse(completedAt = " ").isCompleted)
    }

    @Test
    fun `EnrolledCourse isCompleted is true for non blank value`() {
        assertTrue(domainCourse(completedAt = "2025-01-15T18:30:00").isCompleted)
    }
}
