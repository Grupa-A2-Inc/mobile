package com.adaptive_tutor_mobile.presentation.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicCoursesScreen(
    navController: NavController,
    viewModel: PublicCoursesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val enrollSuccess by viewModel.enrollSuccess.collectAsState()
    val unenrollSuccess by viewModel.unenrollSuccess.collectAsState()
    val enrolledCourseIds by viewModel.enrolledCourseIds.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadEnrolledIds()
    }

    LaunchedEffect(enrollSuccess) {
        enrollSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearEnrollSuccess()
        }
    }
    LaunchedEffect(unenrollSuccess) {
        unenrollSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUnenrollSuccess()
        }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Cursuri disponibile") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                courses.isEmpty() && !isLoading -> Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Nu există cursuri disponibile.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(courses, key = { it.id }) { course ->
                            val isEnrolled = enrolledCourseIds.contains(course.id)
                            Card(
                                onClick = {
                                    if (isEnrolled) navController.navigate("course_detail/${course.id}")
                                },
                                enabled = isEnrolled,
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = course.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    course.category?.let {
                                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                                    }
                                    course.description?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (!isEnrolled) {
                                            Button(onClick = { viewModel.enroll(course.id) }) {
                                                Text("Înscrie-te")
                                            }
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "Înscris ✓",
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                IconButton(onClick = { viewModel.unenroll(course.id) }) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                                        contentDescription = "Dezabonează-te",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.previousPage() },
                            enabled = currentPage > 0
                        ) { Text("← Înapoi") }
                        Text("${currentPage + 1} / $totalPages")
                        Button(
                            onClick = { viewModel.nextPage() },
                            enabled = currentPage < totalPages - 1
                        ) { Text("Înainte →") }
                    }
                }
            }
        }
    }
}
