package com.adaptive_tutor_mobile.di

import com.adaptive_tutor_mobile.data.remote.api.ProgressApi
import com.adaptive_tutor_mobile.data.remote.api.TestApi // Adăugat de Dev 6
import com.adaptive_tutor_mobile.data.repository.*
import com.adaptive_tutor_mobile.domain.repository.*
import com.adaptive_tutor_mobile.data.repository.TestRepositoryImpl
import com.adaptive_tutor_mobile.domain.repository.TestRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
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

    // ── Dev 3: ProgressRepository binding ────────────────────────────────────
    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    // ── Dev 6: TestRepository binding ────────────────────────────────────────
    @Binds
    @Singleton
    abstract fun bindTestRepository(impl: TestRepositoryImpl): TestRepository

    companion object {
        // ── Dev 3: ProgressApi provider ──────────────────────────────────────
        @Provides
        @Singleton
        fun provideProgressApi(retrofit: Retrofit): ProgressApi =
            retrofit.create(ProgressApi::class.java)

        // ── Dev 6: TestApi provider ──────────────────────────────────────────
        @Provides
        @Singleton
        fun provideTestApi(retrofit: Retrofit): TestApi =
            retrofit.create(TestApi::class.java)
    }
}