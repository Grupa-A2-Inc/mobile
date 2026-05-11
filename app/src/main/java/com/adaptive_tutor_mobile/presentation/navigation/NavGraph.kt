package com.adaptive_tutor_mobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adaptive_tutor_mobile.di.SessionStore
import com.adaptive_tutor_mobile.domain.model.UserRole
import com.adaptive_tutor_mobile.presentation.auth.AuthViewModel
import com.adaptive_tutor_mobile.presentation.auth.ForgotPasswordScreen
import com.adaptive_tutor_mobile.presentation.auth.LoginScreen
import com.adaptive_tutor_mobile.presentation.auth.RegisterScreen
import com.adaptive_tutor_mobile.presentation.courses.PublicCoursesScreen
import com.adaptive_tutor_mobile.presentation.home.admin.AdminHomeScreen
import com.adaptive_tutor_mobile.presentation.home.orgadmin.OrgAdminHomeScreen
import com.adaptive_tutor_mobile.presentation.home.parent.ParentHomeScreen
import com.adaptive_tutor_mobile.presentation.home.student.StudentHomeScreen
import com.adaptive_tutor_mobile.presentation.home.teacher.TeacherHomeScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.material3.Text
import com.adaptive_tutor_mobile.presentation.courses.PublicCoursesScreen
fun navigateByRole(navController: NavController, role: UserRole) {
    val dest = routeForRole(role)
    navController.navigate(dest) {
        popUpTo(0) { inclusive = true }
    }
}

@Composable
fun AppNavGraph(startDestination: String, sessionStore: SessionStore) {
    val navController = rememberNavController()

    // Listen for forced logout events from the token authenticator
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
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navController = navController
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
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            // Dev 4 implementeaza ecranul - deocamdata placeholder
            Text("Course Detail: $courseId")
        }
    }
}
