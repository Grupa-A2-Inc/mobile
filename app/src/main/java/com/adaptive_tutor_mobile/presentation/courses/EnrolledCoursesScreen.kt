package com.adaptive_tutor_mobile.presentation.courses

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.domain.model.courses.EnrolledCourse
import com.adaptive_tutor_mobile.presentation.components.EmptyScreen
import com.adaptive_tutor_mobile.presentation.components.ErrorScreen
import com.adaptive_tutor_mobile.presentation.components.LoadingScreen
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrolledCoursesScreen(
    onNavigateBack: () -> Unit,
    onCourseClick: (courseId: String) -> Unit,
    viewModel: EnrolledCoursesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val unenrollSuccess by viewModel.unenrollSuccess.collectAsState()
    val certificateState by viewModel.certificateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingUnenrollCourse by remember { mutableStateOf<EnrolledCourse?>(null) }
    val context = LocalContext.current

    // ── Mesaje Snackbar ──────────────────────────────────────────────────────
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }
    LaunchedEffect(unenrollSuccess) {
        unenrollSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUnenrollSuccess()
        }
    }

    // ── Gestionare certificat descărcat ──────────────────────────────────────
    LaunchedEffect(certificateState) {
        when (val state = certificateState) {
            is CertificateDownloadState.Ready -> {
                openPdf(context, state.pdfBytes, state.courseTitle)
                viewModel.clearCertificateState()
            }
            is CertificateDownloadState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearCertificateState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cursurile mele") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = uiState) {
                is EnrolledCoursesUiState.Loading -> LoadingScreen()

                is EnrolledCoursesUiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.loadCourses() }
                )

                is EnrolledCoursesUiState.Success -> {
                    if (state.courses.isEmpty()) {
                        EmptyScreen(
                            message = "Nu ești înscris la niciun curs încă.\nExplorează cursurile disponibile!",
                            icon = Icons.AutoMirrored.Filled.MenuBook
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.courses,
                                key = { it.courseId }
                            ) { course ->
                                EnrolledCourseCard(
                                    course = course,
                                    isCertificateLoading = certificateState is CertificateDownloadState.Loading &&
                                            (certificateState as CertificateDownloadState.Loading).enrollmentId == course.enrollmentId,
                                    onClick = { onCourseClick(course.courseId) },
                                    onUnenrollClick = { pendingUnenrollCourse = course },
                                    onCertificateClick = { viewModel.downloadCertificate(course) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialog confirmare dezabonare ─────────────────────────────────────────
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
                        viewModel.unenroll(course.courseId)
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

// ── Card curs înscris ─────────────────────────────────────────────────────────

@Composable
private fun EnrolledCourseCard(
    course: EnrolledCourse,
    isCertificateLoading: Boolean,
    onClick: () -> Unit,
    onUnenrollClick: () -> Unit,
    onCertificateClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Title + completion badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = course.courseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (course.isCompleted) {
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Completat") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.height(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                } else if (course.canUnenroll) {
                    IconButton(onClick = onUnenrollClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Dezabonează-te",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Category
            course.courseCategory?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar + label
            LinearProgressIndicator(
                progress = { (course.progressPercent.toFloat() / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${course.progressPercent.toInt()}% completat",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Înscris: ${formatDate(course.enrolledAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // ── Buton certificat (doar dacă cursul e completat și public) ─────
            if (course.canDownloadCertificate) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCertificateClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCertificateLoading
                ) {
                    if (isCertificateLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(if (isCertificateLoading) "Se descarcă..." else "Vezi certificatul")
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("ro"))

private fun formatDate(iso: String): String = try {
    LocalDateTime.parse(iso).format(DATE_FORMATTER)
} catch (_: Exception) {
    iso.take(10)
}

/**
 * Salvează PDF-ul în cache și îl deschide cu aplicația implicită de PDF.
 */
private fun openPdf(context: Context, bytes: ByteArray, courseTitle: String) {
    try {
        val cacheDir = File(context.cacheDir, "certificates").apply { mkdirs() }
        val safeTitle = courseTitle.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(40)
        val file = File(cacheDir, "certificat_$safeTitle.pdf")
        file.writeBytes(bytes)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Deschide certificatul").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        // Dacă nu există nicio aplicație PDF, nu facem nimic — eroarea e deja afișată via state
    }
}
