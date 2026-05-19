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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.adaptive_tutor_mobile.data.remote.dto.EnrolledCourseDto
import com.adaptive_tutor_mobile.presentation.auth.AuthViewModel
import com.adaptive_tutor_mobile.presentation.components.AdaptiveBottomBar
import com.adaptive_tutor_mobile.presentation.components.AdaptiveTopBar
import com.adaptive_tutor_mobile.presentation.components.BottomNavItem
import com.adaptive_tutor_mobile.presentation.components.CourseCard
import com.adaptive_tutor_mobile.presentation.courses.PublicCoursesScreen
import com.adaptive_tutor_mobile.presentation.navigation.Screen
import com.adaptive_tutor_mobile.ui.components.AppTopBar
import com.adaptive_tutor_mobile.ui.components.EmptyState
import com.adaptive_tutor_mobile.ui.components.ErrorCard
import com.adaptive_tutor_mobile.ui.components.LoadingShimmerList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Tab-uri bottom nav — 5 tab-uri originale
// ─────────────────────────────────────────────────────────────────────────────

private const val TAB_HOME      = "student_tab_home"
private const val TAB_MY_COURSES = "student_tab_my_courses"
private const val TAB_EXPLORE   = "student_tab_explore"
private const val TAB_ADAPTIVE  = "student_tab_adaptive"
private const val TAB_PROFILE   = "student_tab_profile"

private val bottomNavItems = listOf(
    BottomNavItem(TAB_HOME,       Icons.Filled.Home,      "Acasă"),
    BottomNavItem(TAB_MY_COURSES, Icons.AutoMirrored.Filled.MenuBook,  "Cursuri"),
    BottomNavItem(TAB_EXPLORE,    Icons.Filled.Explore,   "Explorează"),
    BottomNavItem(TAB_ADAPTIVE,   Icons.Filled.Psychology,"Adaptiv"),
    BottomNavItem(TAB_PROFILE,    Icons.Filled.Person,    "Profil")
)

// ─────────────────────────────────────────────────────────────────────────────
// StudentHomeScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StudentHomeScreen(
    viewModel: AuthViewModel,
    onAdaptiveClick: () -> Unit,
    onLogout: () -> Unit,
    navController: NavController,
    onNavigateToEnrolledCourses: () -> Unit = {}
) {
    val studentViewModel: StudentViewModel = hiltViewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val firstName = currentUser?.firstName ?: "Student"

    // rememberSaveable → supraviețuiește rotației / process death
    var currentTab by rememberSaveable { mutableStateOf(TAB_HOME) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                studentViewModel.loadEnrolledCourses()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        bottomBar = {
            AdaptiveBottomBar(
                items = bottomNavItems,
                currentRoute = currentTab,
                onItemClick = { tab ->
                    if (tab == TAB_ADAPTIVE) onAdaptiveClick()
                    else currentTab = tab
                }
            )
        }
    ) { innerPadding ->
        when (currentTab) {
            TAB_HOME -> DashboardTab(
                firstName = firstName,
                studentViewModel = studentViewModel,
                onAdaptiveClick = onAdaptiveClick,
                navController = navController,
                onSeeAllCourses = { currentTab = TAB_MY_COURSES },
                modifier = Modifier.padding(innerPadding)
            )
            TAB_MY_COURSES -> MyCoursesTab(
                studentViewModel = studentViewModel,
                navController = navController,
                onExploreClick = { currentTab = TAB_EXPLORE },
                modifier = Modifier.padding(innerPadding)
            )
            TAB_EXPLORE -> PublicCoursesScreen(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
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

// ─────────────────────────────────────────────────────────────────────────────
// Tab 1: Dashboard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardTab(
    firstName: String,
    studentViewModel: StudentViewModel,
    navController: NavController,
    onSeeAllCourses: () -> Unit,
    onAdaptiveClick: () -> Unit,
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
        // ── Banner salut ──────────────────────────────────────────────────
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Text(
                        text = today.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bună, $firstName!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Continuă să înveți azi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // ── Card sesiune adaptivă ──────────────────────────────────────────
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAdaptiveClick,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sesiune adaptivă",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Primește exerciții adaptate nivelului tău și vezi rezultatul la final.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // ── Conținut dinamic pe baza state-ului ───────────────────────────
        when (val state = coursesState) {
            is CoursesUiState.Loading -> item {
                LoadingShimmerList(itemCount = 3)
            }

            is CoursesUiState.Error -> item {
                ErrorCard(
                    message = state.message,
                    onRetry = { studentViewModel.loadEnrolledCourses() }
                )
            }

            is CoursesUiState.Success -> {
                val courses = state.courses

                // "Continuă să înveți"
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
                                progressPercent = inProgressCourse.progressPercent,
                                onClick = {
                                    navController.navigate(
                                        Screen.CourseDetail.createRoute(inProgressCourse.courseId)
                                    )
                                }
                            )
                            Button(
                                onClick = {
                                    navController.navigate(
                                        Screen.CourseDetail.createRoute(inProgressCourse.courseId)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continuă")
                            }
                        }
                    }
                }

                // "Cursurile mele" — primele 3
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
                            TextButton(onClick = onSeeAllCourses) {
                                Text("Vezi toate →")
                            }
                        }
                    }
                    items(
                        items = courses.take(3),
                        key = { it.courseId }
                    ) { course ->
                        CourseCard(
                            title = course.courseTitle,
                            description = null,
                            category = course.courseCategory,
                            progressPercent = course.progressPercent,
                            onClick = {
                                navController.navigate(
                                    Screen.CourseDetail.createRoute(course.courseId)
                                )
                            }
                        )
                    }
                }

                // Statistici rapide
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
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Niciun curs",
                            subtitle = "Nu ești înscris la niciun curs încă.",
                            actionText = "Explorează cursuri",
                            onAction = { onSeeAllCourses() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
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

// ─────────────────────────────────────────────────────────────────────────────
// Tab 2: Cursurile mele
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyCoursesTab(
    studentViewModel: StudentViewModel,
    navController: NavController,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coursesState by studentViewModel.coursesState.collectAsState()
    val message by studentViewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingUnenrollCourse by remember { mutableStateOf<EnrolledCourseDto?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            studentViewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        AppTopBar(
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
            is CoursesUiState.Loading -> LoadingShimmerList(itemCount = 5)

            is CoursesUiState.Error -> ErrorCard(
                message = state.message,
                onRetry = { studentViewModel.loadEnrolledCourses() }
            )

            is CoursesUiState.Success -> {
                val courses = state.courses
                if (courses.isEmpty()) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "Niciun curs",
                        subtitle = "Nu ești înscris la niciun curs încă.\nExplorează cursuri disponibile!",
                        actionText = "Explorează",
                        onAction = onExploreClick
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = courses,
                            key = { it.courseId }
                        ) { course ->
                            MyCourseCard(
                                course = course,
                                onClick = {
                                    navController.navigate(
                                        Screen.CourseDetail.createRoute(course.courseId)
                                    )
                                },
                                onUnenrollClick = { pendingUnenrollCourse = course }
                            )
                        }
                    }
                }
            }
        }
    }

        pendingUnenrollCourse?.let { course ->
            AlertDialog(
                onDismissRequest = { pendingUnenrollCourse = null },
                title = { Text("Confirmă dezabonarea") },
                text = {
                    Text(
                        "Ești sigur că vrei să te dezabonezi din ${course.courseTitle}? " +
                            "Progresul tău va fi pierdut."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            studentViewModel.unenroll(course.courseId)
                            pendingUnenrollCourse = null
                        }
                    ) {
                        Text("Dezabonează")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingUnenrollCourse = null }) {
                        Text("Anulează")
                    }
                }
            )
        }
    }
}

@Composable
private fun MyCourseCard(
    course: EnrolledCourseDto,
    onClick: () -> Unit,
    onUnenrollClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.courseTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    if (!course.courseCategory.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = course.courseCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (course.completedAt.isNullOrBlank()) {
                    IconButton(onClick = onUnenrollClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Dezabonează-te",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                course.progressPercent?.let { progress ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progres",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${progress.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { (progress / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 3: Explorează → refolosește PublicCoursesScreen
// (randat direct în when block din StudentHomeScreen de mai sus)
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Tab 4: Profil
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileTab(
    firstName: String,
    lastName: String,
    email: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(title = "Profil")
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
