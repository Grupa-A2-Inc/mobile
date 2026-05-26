package com.adaptive_tutor_mobile.presentation.test

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForStudentDto
import com.adaptive_tutor_mobile.domain.usecase.test.ReportQuestionErrorUseCase
import com.adaptive_tutor_mobile.presentation.components.AdaptiveTopBar
import com.adaptive_tutor_mobile.presentation.components.ErrorScreen
import com.adaptive_tutor_mobile.presentation.components.LoadingScreen
import com.adaptive_tutor_mobile.ui.theme.SuccessColor
import com.adaptive_tutor_mobile.presentation.components.ScoreCircle

@Composable
fun TestScreen(
    viewModel: TestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar pe succesul raportării
    LaunchedEffect(state.reportSuccess) {
        state.reportSuccess?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearReportSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { outerPadding ->
        Box(modifier = Modifier.padding(outerPadding)) {
            when {
                state.isLoading -> Scaffold(
                    topBar = { AdaptiveTopBar("Test", onBack = onNavigateBack) }
                ) { padding -> Box(Modifier.padding(padding)) { LoadingScreen() } }

                state.error != null -> Scaffold(
                    topBar = { AdaptiveTopBar("Test", onBack = onNavigateBack) }
                ) { padding -> Box(Modifier.padding(padding)) { ErrorScreen(state.error!!, onRetry = onNavigateBack) } }

                state.report != null -> TestResultScreen(
                    state = state,
                    onBack = onNavigateBack
                )

                state.questions.isNotEmpty() -> TestQuestionScreen(
                    state = state,
                    onSelectOption = viewModel::selectOption,
                    onGoToQuestion = viewModel::goToQuestion,
                    onNext = viewModel::nextQuestion,
                    onPrev = viewModel::prevQuestion,
                    onSubmit = viewModel::submitTest,
                    onBack = onNavigateBack,
                    onReportQuestion = viewModel::showReportDialog
                )

                else -> Scaffold(
                    topBar = { AdaptiveTopBar("Test", onBack = onNavigateBack) }
                ) { padding -> Box(Modifier.padding(padding)) { LoadingScreen() } }
            }
        }
    }

    if (state.showReportDialog) {
        ReportQuestionDialog(
            isSubmitting = state.isSubmittingReport,
            errorMessage = state.reportError,
            onDismiss = viewModel::dismissReportDialog,
            onSubmit = viewModel::submitReport
        )
    }
}

// ── Question screen ───────────────────────────────────────────────────────────

@Composable
private fun TestQuestionScreen(
    state: TestUiState,
    onSelectOption: (Int, Int, Boolean) -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onReportQuestion: (Int) -> Unit
) {
    val question = state.questions.getOrNull(state.currentIndex) ?: return
    val total = state.questions.size
    val isLast = state.currentIndex == state.questions.lastIndex
    val selected = state.selectedAnswers[question.questionId].orEmpty()
    val isSingle = question.questionType == "SINGLE_CHOICE" || question.questionType == "TRUE_FALSE"
    val progress = (state.currentIndex + 1).toFloat() / total

    Scaffold(
        topBar = {
            AdaptiveTopBar(
                title = "Întrebarea ${state.currentIndex + 1} / $total",
                onBack = onBack
            )
        },
        bottomBar = {
            QuestionNavigationBar(
                questions = state.questions,
                selectedAnswers = state.selectedAnswers,
                currentIndex = state.currentIndex,
                isLast = isLast,
                onGoToQuestion = onGoToQuestion,
                onPrev = onPrev,
                onNext = onNext,
                onSubmit = onSubmit
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: text intrebare + buton raporteaza în dreapta sus
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = question.content ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = if (isSingle) "Alege un singur răspuns"
                                        else "Alege unul sau mai multe răspunsuri",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                TextButton(
                                    onClick = { onReportQuestion(question.questionId) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = "Raportează",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
                items(question.options.orEmpty()) { option ->
                    FilterChip(
                        selected = option.optionId in selected,
                        onClick = { onSelectOption(question.questionId, option.optionId, isSingle) },
                        label = { Text(option.text, style = MaterialTheme.typography.bodyLarge) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionNavigationBar(
    questions: List<QuestionForStudentDto>,
    selectedAnswers: Map<Int, List<Int>>,
    currentIndex: Int,
    isLast: Boolean,
    onGoToQuestion: (Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(questions) { index, q ->
                    val hasAnswer = selectedAnswers[q.questionId]?.isNotEmpty() == true
                    QuestionChip(
                        number = index + 1,
                        isCurrent = index == currentIndex,
                        isAnswered = hasAnswer,
                        onClick = { onGoToQuestion(index) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrev,
                    modifier = Modifier.weight(1f),
                    enabled = currentIndex > 0
                ) { Text("← Înapoi") }
                Button(
                    onClick = if (isLast) onSubmit else onNext,
                    modifier = Modifier.weight(1f)
                ) { Text(if (isLast) "Trimite" else "Înainte →") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionChip(
    number: Int,
    isCurrent: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isAnswered -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimary
        isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bg,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$number",
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ── Report dialog ─────────────────────────────────────────────────────────────

@Composable
private fun ReportQuestionDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var description by rememberSaveable { mutableStateOf("") }
    val minLength = ReportQuestionErrorUseCase.MIN_LENGTH
    val maxLength = ReportQuestionErrorUseCase.MAX_LENGTH
    val length = description.trim().length
    val isValid = length in minLength..maxLength

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Raportează eroare") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Descrie problema pe care ai întâlnit-o la această întrebare.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= maxLength) description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = { Text("Ex: răspunsul corect e marcat greșit…") },
                    isError = length > 0 && !isValid,
                    enabled = !isSubmitting,
                    maxLines = 6
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val helper = when {
                        length == 0 -> "Minim $minLength caractere"
                        length < minLength -> "Mai trebuie ${minLength - length} caractere"
                        else -> ""
                    }
                    Text(
                        text = helper,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$length / $maxLength",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (length > maxLength) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(description) },
                enabled = isValid && !isSubmitting
            ) {
                Text(if (isSubmitting) "Se trimite..." else "Trimite")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Anulează")
            }
        }
    )
}

// ── Result screen ─────────────────────────────────────────────────────────────

@Composable
private fun TestResultScreen(
    state: TestUiState,
    onBack: () -> Unit
) {
    val report = state.report!!
    val passed = report.passed == true
    val scorePercent = report.scorePercent ?: 0.0

    Scaffold(
        topBar = { AdaptiveTopBar(title = "Rezultatele testului", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreCircle(scorePercent = scorePercent, passed = passed)
                        if (report.score != null) {
                            Text(
                                text = "Scor: ${String.format("%.1f", report.score)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Detalii răspunsuri",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            itemsIndexed(report.questions.orEmpty()) { index, rq ->
                ResultQuestionCard(
                    index = index + 1,
                    reportQuestion = rq,
                    original = state.questions.firstOrNull { it.questionId == rq.questionId }
                )
            }

            item {
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Înapoi la lecție")
                }
            }
        }
    }
}

@Composable
private fun ResultQuestionCard(
    index: Int,
    reportQuestion: QuestionForAttemptReportDTO,
    original: QuestionForStudentDto?
) {
    val correct = reportQuestion.correct
    val accent = if (correct) SuccessColor else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (correct) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$index. ${reportQuestion.content ?: ""}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            if (original != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                val selectedTexts = reportQuestion.selectedOptionIds.orEmpty().mapNotNull { id ->
                    original.options.orEmpty().firstOrNull { it.optionId == id }?.text
                }
                val correctTexts = reportQuestion.correctOptionIds.orEmpty().mapNotNull { id ->
                    original.options.orEmpty().firstOrNull { it.optionId == id }?.text
                }

                if (selectedTexts.isNotEmpty()) {
                    AnswerRow(
                        label = "Răspunsul tău:",
                        texts = selectedTexts,
                        color = accent
                    )
                }
                if (!correct && correctTexts.isNotEmpty()) {
                    AnswerRow(
                        label = "Răspuns corect:",
                        texts = correctTexts,
                        color = SuccessColor
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerRow(label: String, texts: List<String>, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = texts.joinToString(", "),
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}