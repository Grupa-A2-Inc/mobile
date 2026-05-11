package com.adaptive_tutor_mobile.di

import com.adaptive_tutor_mobile.data.repository.AuthRepositoryImpl
import com.adaptive_tutor_mobile.data.repository.CourseDetailRepositoryImpl
import com.adaptive_tutor_mobile.data.repository.CourseRepositoryImpl
import com.adaptive_tutor_mobile.domain.repository.AuthRepository
import com.adaptive_tutor_mobile.domain.repository.CourseDetailRepository
import com.adaptive_tutor_mobile.domain.repository.CourseRepository
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
}