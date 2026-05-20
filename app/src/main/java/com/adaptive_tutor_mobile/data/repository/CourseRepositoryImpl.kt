package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.EnrollmentApi
import com.adaptive_tutor_mobile.domain.model.courses.Course
import com.adaptive_tutor_mobile.domain.model.courses.PagedCourses
import com.adaptive_tutor_mobile.domain.repository.courses.CourseRepository
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val enrollmentApi: EnrollmentApi
) : CourseRepository {

    override suspend fun getPublicCourses(page: Int, size: Int): Result<PagedCourses> {
        return try {
            val response = enrollmentApi.getPublicCourses(page, size)
            if (response.isSuccessful) {
                val body = response.body()
                val courses = body?.content?.map { dto ->
                    Course(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        category = dto.category,
                        status = dto.status,
                        visibility = dto.visibility
                    )
                } ?: emptyList()
                Result.success(PagedCourses(courses, body?.totalPages ?: 1, body?.number ?: 0))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enrollInCourse(courseId: String): Result<Unit> {
        return try {
            val response = enrollmentApi.enrollInCourse(courseId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unenrollFromCourse(courseId: String): Result<Unit> {
        return try {
            val response = enrollmentApi.unenrollFromCourse(courseId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
