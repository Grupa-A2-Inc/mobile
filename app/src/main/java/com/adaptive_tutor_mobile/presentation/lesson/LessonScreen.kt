package com.adaptive_tutor_mobile.presentation.lesson

import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.noties.markwon.Markwon
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    viewModel: LessonViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTest: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.lesson?.title ?: "Lecție",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (state.showRatingDialog) {
            RatingDialog(
                lessonId = state.lesson?.id ?: "",
                onDismiss = { viewModel.dismissRatingDialog() },
                onSubmit = { rating, comment -> viewModel.submitRating(state.lesson?.id ?: "", rating, comment) }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(onClick = onNavigateBack) {
                            Text("Înapoi")
                        }
                    }
                }
                state.lesson != null -> {
                    val lesson = state.lesson!!
                    val context = LocalContext.current
                    val markwon = remember { Markwon.create(context) }
                    val contentColor = MaterialTheme.colorScheme.onBackground.toArgb()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )

                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            AndroidView(
                                modifier = Modifier.fillMaxWidth(),
                                factory = { ctx ->
                                    TextView(ctx).apply {
                                        layoutParams = android.view.ViewGroup.LayoutParams(
                                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        textSize = 16f
                                        setLineSpacing(0f, 1.5f)
                                    }
                                },
                                update = { textView ->
                                    textView.setTextColor(contentColor)
                                    markwon.setMarkdown(textView, lesson.contentMarkdown)
                                }
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            when {
                                state.isCheckingTest -> Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                                state.testId != null -> Button(
                                    onClick = { onNavigateToTest(state.testId!!) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        text = "Dă testul",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                else -> {}
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            RatingSection(
                                ratingSummary = state.ratingSummary,
                                hasRated = state.hasRated,
                                onRateClick = { viewModel.showRatingDialog() }
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSection(
    ratingSummary: com.adaptive_tutor_mobile.domain.model.RatingSummary?,
    hasRated: Boolean,
    onRateClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (ratingSummary != null) {
            Text(
                text = "Rating mediu: ${"%.1f".format(ratingSummary.avgRating)} ⭐ (${ratingSummary.totalRatings} recenzii)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (!hasRated) {
            OutlinedButton(onClick = onRateClick) {
                Text("Evaluează lecția")
            }
        } else {
            Text(
                text = "Ai evaluat această lecție ✓",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RatingDialog(
    lessonId: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String?) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Evaluează lecția") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { selectedRating = star }) {
                            Icon(
                                imageVector = if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$star stele",
                                tint = if (star <= selectedRating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentariu (opțional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedRating, comment.ifBlank { null }) },
                enabled = selectedRating > 0
            ) {
                Text("Trimite")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Anulează")
            }
        }
    )
}
