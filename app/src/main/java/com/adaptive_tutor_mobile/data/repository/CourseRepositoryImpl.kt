package com.adaptive_tutor_mobile.data.repository

import com.adaptive_tutor_mobile.data.remote.api.EnrollmentApi
import com.adaptive_tutor_mobile.domain.model.Course
import com.adaptive_tutor_mobile.domain.repository.CourseRepository
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val enrollmentApi: EnrollmentApi
) : CourseRepository {

    override suspend fun getPublicCourses(page: Int, size: Int): Result<List<Course>> {
        return try {
            val response = enrollmentApi.getPublicCourses(page, size)
            if (response.isSuccessful) {
                val courses = response.body()?.content?.map { dto ->
                    Course(
                        id = dto.id,
                        title = dto.title,
                        description = dto.description,
                        category = dto.category,
                        status = dto.status,
                        visibility = dto.visibility
                    )
                } ?: emptyList()
                Result.success(courses)
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
}