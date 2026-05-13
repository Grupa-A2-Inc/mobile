package com.adaptive_tutor_mobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.UserRole
import com.adaptive_tutor_mobile.presentation.auth.AuthViewModel
import com.adaptive_tutor_mobile.presentation.auth.ForgotPasswordScreen
import com.adaptive_tutor_mobile.presentation.auth.LoginScreen
import com.adaptive_tutor_mobile.presentation.auth.RegisterScreen
import com.adaptive_tutor_mobile.presentation.courses.CourseDetailScreen
import com.adaptive_tutor_mobile.presentation.courses.PublicCoursesScreen
import com.adaptive_tutor_mobile.presentation.home.admin.AdminHomeScreen
import com.adaptive_tutor_mobile.presentation.home.orgadmin.OrgAdminHomeScreen
import com.adaptive_tutor_mobile.presentation.home.parent.ParentHomeScreen
import com.adaptive_tutor_mobile.presentation.home.student.StudentHomeScreen
import com.adaptive_tutor_mobile.presentation.home.teacher.TeacherHomeScreen
import com.adaptive_tutor_mobile.presentation.adaptive.AdaptiveResultScreen
import com.adaptive_tutor_mobile.presentation.adaptive.AdaptiveSessionScreen
import com.adaptive_tutor_mobile.presentation.lesson.LessonScreen
// ── Dev 6: Importuri noi ──────────────────────────────────────────────────
import com.adaptive_tutor_mobile.presentation.test.TestScreen
import com.adaptive_tutor_mobile.presentation.test.TestResultScreen

fun navigateByRole(navController: NavController, role: UserRole) {
    val dest = routeForRole(role)
    navController.navigate(dest) {
        popUpTo(0) { inclusive = true }
    }
}

@Composable
fun AppNavGraph(startDestination: String, sessionStore: SessionStore) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        sessionStore.forceLogoutEvent.collect {
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { user -> navigateByRole(navController, user.role) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { user -> navigateByRole(navController, user.role) },
                onNavigateToLogin = { navController.navigateUp() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AdminHome.route) {
            AdminHomeScreen(
                viewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.OrgAdminHome.route) {
            OrgAdminHomeScreen(
                viewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(
                viewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.StudentHome.route) {
            StudentHomeScreen(
                viewModel = authViewModel,
                onAdaptiveClick = {
                    navController.navigate(Screen.AdaptiveSession.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        composable(Screen.AdaptiveSession.route) {
            AdaptiveSessionScreen(
                onBackToHome = {
                    navController.navigate(Screen.StudentHome.route) {
                        popUpTo(Screen.StudentHome.route) { inclusive = true }
                    }
                },
                onShowResult = {
                    navController.navigate(Screen.AdaptiveResult.route)
                }
            )
        }

        composable(Screen.AdaptiveResult.route) {
            AdaptiveResultScreen(
                onBackToHome = {
                    navController.navigate(Screen.StudentHome.route) {
                        popUpTo(Screen.StudentHome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ParentHome.route) {
            ParentHomeScreen(
                viewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PublicCourses.route) {
            PublicCoursesScreen(navController = navController)
        }

        // ── Course Detail ────────────────────────────────────────────────────
        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType }
            )
        ) {
            CourseDetailScreen(
                onNavigateBack     = { navController.navigateUp() },
                onNavigateToLesson = { lessonId ->
                    navController.navigate(Screen.Lesson.createRoute(lessonId))
                }
            )
        }

        // ── Lesson Screen ────────────────────────────────────────────────────
        composable(
            route = Screen.Lesson.route,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) {
            LessonScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTest = { testId ->
                    // Dev 6: Acum navigăm real către ecranul de test
                    navController.navigate(Screen.Test.createRoute(testId))
                }
            )
        }

        // ── Dev 6: Test & Test Result ────────────────────────────────────────
        composable(
            route = Screen.Test.route,
            arguments = listOf(
                navArgument("testId") { type = NavType.StringType }
            )
        ) {
            TestScreen(
                onNavigateToResult = { attemptId ->
                    navController.navigate(Screen.TestResult.createRoute(attemptId)) {
                        // Ștergem testul din backstack ca studentul să nu revină la el cu butonul Back
                        popUpTo(Screen.Test.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.TestResult.route,
            arguments = listOf(
                navArgument("attemptId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString("attemptId") ?: ""
            TestResultScreen(
                attemptId = attemptId,
                onBackToLesson = {
                    navController.popBackStack()
                },
                onRetryTest = {
                    // Ne întoarcem la lecție sau la test
                    navController.popBackStack()
                }
            )
        }
    }
}