package com.adaptive_tutor_mobile.di

import com.adaptive_tutor_mobile.data.repository.*
import com.adaptive_tutor_mobile.domain.repository.auth.AuthRepository
import com.adaptive_tutor_mobile.domain.repository.courses.CourseRepository
import com.adaptive_tutor_mobile.domain.repository.courses.CourseDetailRepository
import com.adaptive_tutor_mobile.domain.repository.lesson.LessonRepository
import com.adaptive_tutor_mobile.domain.repository.lesson.RatingRepository
import com.adaptive_tutor_mobile.domain.repository.test.TestRepository
import com.adaptive_tutor_mobile.domain.repository.test.AttemptHistoryRepository
import com.adaptive_tutor_mobile.domain.repository.test.ErrorReportRepository
import com.adaptive_tutor_mobile.domain.repository.adaptive.AdaptiveRepository
import com.adaptive_tutor_mobile.domain.repository.profile.UserRepository
import com.adaptive_tutor_mobile.domain.repository.stats.ProgressRepository
import com.adaptive_tutor_mobile.domain.repository.stats.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    @Singleton
    abstract fun bindCourseDetailRepository(impl: CourseDetailRepositoryImpl): CourseDetailRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindAdaptiveRepository(impl: AdaptiveRepositoryImpl): AdaptiveRepository

    @Binds
    @Singleton
    abstract fun bindLessonRepository(impl: LessonRepositoryImpl): LessonRepository

    @Binds
    @Singleton
    abstract fun bindTestRepository(impl: TestRepositoryImpl): TestRepository

    @Binds
    @Singleton
    abstract fun bindRatingRepository(impl: RatingRepositoryImpl): RatingRepository

    @Binds
    @Singleton
    abstract fun bindErrorReportRepository(impl: ErrorReportRepositoryImpl): ErrorReportRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    @Singleton
    abstract fun bindAttemptHistoryRepository(impl: AttemptHistoryRepositoryImpl): AttemptHistoryRepository
}
