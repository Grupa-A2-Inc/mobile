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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForAttemptReportDTO
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForStudentDto
import com.adaptive_tutor_mobile.presentation.components.AdaptiveTopBar
import com.adaptive_tutor_mobile.presentation.components.ErrorScreen
import com.adaptive_tutor_mobile.presentation.components.LoadingScreen
import com.adaptive_tutor_mobile.presentation.components.ScoreCircle

@Composable
fun TestScreen(
    viewModel: TestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> Scaffold(
            topBar = { AdaptiveTopBar("Test", onBack = onNavigateBack) }
        ) { LoadingScreen() }

        state.error != null -> Scaffold(
            topBar = { AdaptiveTopBar("Test", onBack = onNavigateBack) }
        ) { ErrorScreen(state.error!!, onRetry = onNavigateBack) }

        state.report != null -> TestResultScreen(state = state, onBack = onNavigateBack)

        state.questions.isNotEmpty() -> TestQuestionScreen(
            state = state,
            onSelectOption = viewModel::selectOption,
            onGoToQuestion = viewModel::goToQuestion,
            onNext = viewModel::nextQuestion,
            onPrev = viewModel::prevQuestion,
            onSubmit = viewModel::submitTest,
            onBack = onNavigateBack
        )

        else -> Scaffold(
            topBar = { AdaptiveTopBar("Test", onBack = onNavigateBack) }
        ) { LoadingScreen() }
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
    onBack: () -> Unit
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
    val accent = if (correct) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

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
                        color = Color(0xFF4CAF50)
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
