package com.adaptive_tutor_mobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun AppNavGraph(sessionStore: SessionStore) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    // Force logout (din TokenRefreshAuthenticator)
    LaunchedEffect(Unit) {
        sessionStore.forceLogoutEvent.collect {
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashRoute(
                sessionStore = sessionStore,
                onDecided = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

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
                onLogout = { popToLogin(navController) }
            )
        }
        composable(Screen.OrgAdminHome.route) {
            OrgAdminHomeScreen(
                viewModel = authViewModel,
                onLogout = { popToLogin(navController) }
            )
        }
        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(
                viewModel = authViewModel,
                onLogout = { popToLogin(navController) }
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
                onLogout = { popToLogin(navController) }
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

private fun popToLogin(navController: NavController) {
    navController.navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}

@Composable
private fun SplashRoute(
    sessionStore: SessionStore,
    onDecided: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        val token = sessionStore.getAccessToken()
        val user = if (token != null) sessionStore.getUser() else null
        val destination = when {
            token == null     -> Screen.Login.route
            user == null      -> Screen.Login.route
            else              -> routeForRole(user.role)
        }
        onDecided(destination)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}