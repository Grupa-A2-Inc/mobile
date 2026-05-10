package com.adaptive_tutor_mobile.presentation.home.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto
import com.adaptive_tutor_mobile.presentation.auth.AuthViewModel
import com.adaptive_tutor_mobile.presentation.components.AdaptiveBottomBar
import com.adaptive_tutor_mobile.presentation.components.AdaptiveTopBar
import com.adaptive_tutor_mobile.presentation.components.BottomNavItem
import com.adaptive_tutor_mobile.presentation.components.CourseCard
import com.adaptive_tutor_mobile.presentation.components.EmptyScreen
import com.adaptive_tutor_mobile.presentation.components.ErrorScreen
import com.adaptive_tutor_mobile.presentation.components.LoadingScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavController
import com.adaptive_tutor_mobile.presentation.courses.PublicCoursesScreen

private const val TAB_HOME = "student_tab_home"
private const val TAB_MY_COURSES = "student_tab_my_courses"
private const val TAB_EXPLORE = "student_tab_explore"
private const val TAB_PROFILE = "student_tab_profile"

private val bottomNavItems = listOf(
    BottomNavItem(TAB_HOME, Icons.Filled.Home, "Acasă"),
    BottomNavItem(TAB_MY_COURSES, Icons.Filled.MenuBook, "Cursuri"),
    BottomNavItem(TAB_EXPLORE, Icons.Filled.Explore, "Explorează"),
    BottomNavItem(TAB_PROFILE, Icons.Filled.Person, "Profil")
)

@Composable

fun StudentHomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit,
    navController: NavController
) {
    val studentViewModel: StudentViewModel = hiltViewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val firstName = currentUser?.firstName ?: "Student"

    var currentTab by remember { mutableStateOf(TAB_HOME) }

    Scaffold(
        bottomBar = {
            AdaptiveBottomBar(
                items = bottomNavItems,
                currentRoute = currentTab,
                onItemClick = { currentTab = it }
            )
        }
    ) { innerPadding ->
        when (currentTab) {
            TAB_HOME -> DashboardTab(
                firstName = firstName,
                studentViewModel = studentViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            TAB_MY_COURSES -> MyCoursesTab(
                studentViewModel = studentViewModel,
                onExploreClick = { currentTab = TAB_EXPLORE },
                modifier = Modifier.padding(innerPadding)
            )
            TAB_EXPLORE -> PublicCoursesScreen(
                navController = navController
            )
            TAB_PROFILE -> ProfileTab(
                firstName = firstName,
                lastName = currentUser?.lastName ?: "",
                email = currentUser?.email ?: "",
                onLogout = { viewModel.logout(); onLogout() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// ── Tab 1: Dashboard ──────────────────────────────────────────────────────────

@Composable
private fun DashboardTab(
    firstName: String,
    studentViewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val coursesState by studentViewModel.coursesState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale("ro")) }
    val today = remember { dateFormat.format(Date()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Bună, $firstName! 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = today,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        when (val state = coursesState) {
            is CoursesUiState.Loading -> item { LoadingScreen() }
            is CoursesUiState.Error -> item {
                ErrorScreen(
                    message = state.message,
                    onRetry = { studentViewModel.loadEnrolledCourses() }
                )
            }
            is CoursesUiState.Success -> {
                val courses = state.courses

                // "Continuă să înveți" — course with highest progress < 100
                val inProgressCourse = courses
                    .filter { (it.progressPercent ?: 0.0) < 100.0 }
                    .maxByOrNull { it.progressPercent ?: 0.0 }

                if (inProgressCourse != null) {
                    item {
                        Text(
                            text = "Continuă să înveți",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CourseCard(
                                title = inProgressCourse.courseTitle,
                                description = null,
                                category = inProgressCourse.courseCategory,
                                status = "PUBLISHED",
                                progressPercent = inProgressCourse.progressPercent,
                                onClick = {}
                            )
                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continuă")
                            }
                        }
                    }
                }

                // "Cursurile mele" — first 3
                if (courses.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cursurile mele",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = {}) {
                                Text("Vezi toate →")
                            }
                        }
                    }
                    items(
                        items = courses.take(3),
                        key = { course -> course.courseId }
                    ) { course ->
                        CourseCard(
                            title = course.courseTitle,
                            description = null,
                            category = course.courseCategory,
                            status = "PUBLISHED",
                            progressPercent = course.progressPercent,
                            onClick = {}
                        )
                    }
                }

                // "Statistici rapide"
                item {
                    Text(
                        text = "Statistici rapide",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Cursuri înscrise", courses.size.toString(), Modifier.weight(1f))
                        StatCard("Lecții citite", "—", Modifier.weight(1f))
                        StatCard("Teste promovate", "0", Modifier.weight(1f))
                    }
                }

                if (courses.isEmpty()) {
                    item {
                        EmptyScreen(
                            message = "Nu ești înscris la niciun curs încă.",
                            icon = Icons.Filled.MenuBook
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Tab 2: Cursurile mele ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyCoursesTab(
    studentViewModel: StudentViewModel,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coursesState by studentViewModel.coursesState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        AdaptiveTopBar(
            title = "Cursurile mele",
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Search, contentDescription = "Caută")
                }
                TextButton(onClick = onExploreClick) {
                    Text("Explorează")
                }
            }
        )

        when (val state = coursesState) {
            is CoursesUiState.Loading -> LoadingScreen()
            is CoursesUiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = { studentViewModel.loadEnrolledCourses() }
            )
            is CoursesUiState.Success -> {
                val courses = state.courses
                if (courses.isEmpty()) {
                    EmptyScreen(
                        message = "Nu ești înscris la niciun curs.\nExplorează cursuri disponibile!",
                        icon = Icons.Filled.MenuBook
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = courses,
                            key = { course -> course.courseId }
                        ) { course ->
                            CourseCard(
                                title = course.courseTitle,
                                description = null,
                                category = course.courseCategory,
                                status = "PUBLISHED",
                                progressPercent = course.progressPercent,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Tab 3: Explorează ─────────────────────────────────────────────────────────

@Composable
private fun ExploreTab(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        AdaptiveTopBar(title = "Explorează cursuri")
        EmptyScreen(
            message = "Cursuri publice — în curând",
            icon = Icons.Filled.Explore
        )
    }
}

// ── Tab 4: Profil ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileTab(
    firstName: String,
    lastName: String,
    email: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        AdaptiveTopBar(title = "Profil")
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "$firstName $lastName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Text(text = "Deconectare", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

