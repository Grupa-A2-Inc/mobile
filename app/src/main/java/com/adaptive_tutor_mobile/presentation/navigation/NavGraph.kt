package com.adaptive_tutor_mobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.presentation.auth.AuthNavigationTarget
import com.adaptive_tutor_mobile.presentation.auth.AuthViewModel
import com.adaptive_tutor_mobile.presentation.auth.ForgotPasswordScreen
import com.adaptive_tutor_mobile.presentation.auth.LoginScreen
import com.adaptive_tutor_mobile.presentation.auth.RegisterScreen
import com.adaptive_tutor_mobile.presentation.auth.RoleBlockedScreen
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
import com.adaptive_tutor_mobile.presentation.profile.ProfileScreen
import com.adaptive_tutor_mobile.presentation.test.TestScreen
import com.adaptive_tutor_mobile.presentation.stats.PersonalStatsScreen
import com.adaptive_tutor_mobile.presentation.test.TestAttemptsScreen

fun navigateAfterAuth(navController: NavController, target: AuthNavigationTarget) {
    val dest = when (target) {
        AuthNavigationTarget.STUDENT_HOME -> Screen.StudentHome.route
        AuthNavigationTarget.ROLE_BLOCKED -> Screen.RoleBlocked.route
    }
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
                onNavigateAfterAuth = { target -> navigateAfterAuth(navController, target) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateAfterAuth = { target -> navigateAfterAuth(navController, target) },
                onNavigateToLogin = { navController.navigateUp() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.RoleBlocked.route) {
            RoleBlockedScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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

        composable(Screen.AdaptiveResult.route) { entry ->
            val sessionEntry = remember(entry) {
                navController.getBackStackEntry(Screen.AdaptiveSession.route)
            }
            AdaptiveResultScreen(
                viewModel = hiltViewModel(sessionEntry),
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

        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType }
            )
        ) {
            CourseDetailScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToLesson = { lessonId ->
                    navController.navigate(Screen.Lesson.createRoute(lessonId))
                },
                onNavigateToStats = { courseId ->
                    navController.navigate(Screen.PersonalStats.createRoute(courseId))
                }
            )
        }

        composable(
            route = Screen.Lesson.route,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) {
            LessonScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTest = { testId ->
                    navController.navigate(Screen.TestAttempt.createRoute(testId))
                },
                onNavigateToHistory = { testId ->
                    navController.navigate(Screen.TestAttempts.createRoute(testId))
                }
            )
        }

        composable(
            route = Screen.TestAttempt.route,
            arguments = listOf(
                navArgument("testId") { type = NavType.StringType }
            )
        ) {
            TestScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.navigateUp() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PersonalStats.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) {
            PersonalStatsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.TestAttempts.route,
            arguments = listOf(navArgument("testId") { type = NavType.StringType })
        ) {
            TestAttemptsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
