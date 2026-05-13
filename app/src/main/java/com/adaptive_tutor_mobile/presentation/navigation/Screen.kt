package com.adaptive_tutor_mobile.presentation.navigation

import com.adaptive_tutor_mobile.domain.model.UserRole

sealed class Screen(val route: String) {
    object Splash         : Screen("splash")
    object Login          : Screen("login")
    object Register       : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object AdminHome      : Screen("admin_home")
    object OrgAdminHome   : Screen("org_admin_home")
    object TeacherHome    : Screen("teacher_home")
    object StudentHome    : Screen("student_home")
    object ParentHome     : Screen("parent_home")
    object AdaptiveSession : Screen("adaptive_session")
    object AdaptiveResult  : Screen("adaptive_result")

    object PublicCourses : Screen("public_courses")
    object CourseDetail : Screen("course_detail/{courseId}") {
        fun createRoute(courseId: String) = "course_detail/$courseId"
    }

    object EnrolledCourses : Screen("enrolled_courses")

    object Lesson : Screen("lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }

    // ── Dev 6: Rute pentru Testare ──────────────────────────────────────
    object Test : Screen("test/{testId}") {
        fun createRoute(testId: String) = "test/$testId"
    }

    object TestResult : Screen("test_result/{attemptId}") {
        fun createRoute(attemptId: String) = "test_result/$attemptId"
    }
}

fun routeForRole(role: UserRole): String = when (role) {
    UserRole.ADMIN              -> Screen.AdminHome.route
    UserRole.ORGANIZATION_ADMIN -> Screen.OrgAdminHome.route
    UserRole.TEACHER            -> Screen.TeacherHome.route
    UserRole.STUDENT            -> Screen.StudentHome.route
    UserRole.PARENT             -> Screen.ParentHome.route
    UserRole.UNKNOWN            -> Screen.Login.route
}