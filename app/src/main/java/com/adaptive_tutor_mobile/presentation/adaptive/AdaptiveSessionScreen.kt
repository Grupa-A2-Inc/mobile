package com.adaptive_tutor_mobile.presentation.adaptive

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.presentation.components.AdaptiveTopBar
import com.adaptive_tutor_mobile.presentation.components.ErrorScreen
import com.adaptive_tutor_mobile.presentation.components.LoadingScreen
import com.adaptive_tutor_mobile.presentation.test.QuestionChip
import kotlin.math.roundToInt

@Composable
fun AdaptiveSessionScreen(
    onBackToHome: () -> Unit,
    onShowResult: () -> Unit,
    viewModel: AdaptiveViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.result) {
        if (state.result != null) onShowResult()
    }

    Scaffold(
        topBar = {
            AdaptiveTopBar(title = "Sesiune adaptivă", onBack = onBackToHome)
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingScreen()

            state.errorMessage != null -> ErrorScreen(
                message = state.errorMessage ?: "A apărut o eroare",
                onRetry = { viewModel.clearError() }
            )

            state.session == null -> AdaptiveStartContent(
                modifier = Modifier.padding(innerPadding),
                onStart = { subjectId, topicId, count ->
                    viewModel.startSession(subjectId = subjectId, topicId = topicId, count = count)
                }
            )

            state.session!!.questions.isEmpty() -> ErrorScreen(
                message = "Sesiunea nu conține exerciții. Încearcă din nou.",
                onRetry = { viewModel.clearError() }
            )

            else -> AdaptiveQuestionContent(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onSelectAnswer = viewModel::selectAnswer,
                onNext = viewModel::nextQuestion,
                onPrev = viewModel::prevQuestion,
                onGoToQuestion = viewModel::goToQuestion,
                onSubmit = viewModel::submitSession
            )
        }
    }
}

// ── Start form ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdaptiveStartContent(
    onStart: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubject by remember { mutableStateOf(SUBJECT_LIST.first()) }
    var selectedTopic by remember {
        mutableStateOf(TOPIC_LIST.filter { it.subjectId == SUBJECT_LIST.first().id }.first())
    }
    var exerciseCount by remember { mutableFloatStateOf(5f) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var topicExpanded by remember { mutableStateOf(false) }

    val topicsForSubject = remember(selectedSubject.id) {
        TOPIC_LIST.filter { it.subjectId == selectedSubject.id }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Sesiune adaptivă",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Alege materia și tema pentru exerciții adaptate nivelului tău.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        item { HorizontalDivider() }

        item {
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSubject.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Materie") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    SUBJECT_LIST.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = {
                                selectedSubject = subject
                                selectedTopic = TOPIC_LIST.filter { it.subjectId == subject.id }.first()
                                subjectExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        item {
            ExposedDropdownMenuBox(
                expanded = topicExpanded,
                onExpandedChange = { topicExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${selectedTopic.name}  ·  cls. ${selectedTopic.grade}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Temă") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = topicExpanded,
                    onDismissRequest = { topicExpanded = false }
                ) {
                    topicsForSubject.forEach { topic ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(topic.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "Clasa ${topic.grade}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            onClick = { selectedTopic = topic; topicExpanded = false },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Număr exerciții", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${exerciseCount.roundToInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = exerciseCount,
                    onValueChange = { exerciseCount = it },
                    valueRange = 3f..10f,
                    steps = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Button(
                onClick = {
                    onStart(selectedSubject.id, selectedTopic.id, exerciseCount.roundToInt())
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Pornește sesiunea", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ── Question content ──────────────────────────────────────────────────────────

@Composable
private fun AdaptiveQuestionContent(
    state: AdaptiveUiState,
    onSelectAnswer: (String, Int, Boolean) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = state.session ?: return
    val question = session.questions[state.currentIndex]
    val selectedAnswers = state.selectedAnswers[question.questionId].orEmpty()
    val isLast = state.currentIndex == session.questions.lastIndex
    val progress = (state.currentIndex + 1).toFloat() / session.questions.size.toFloat()
    val isSingle = question.questionType == "SINGLE_CHOICE" || question.questionType == "TRUE_FALSE"

    Column(modifier = modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Întrebarea ${state.currentIndex + 1} / ${session.questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question.content ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isSingle) "Alege un singur răspuns"
                            else "Alege unul sau mai multe răspunsuri",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.orEmpty().forEach { option ->
                        val isSelected = option.optionId in selectedAnswers
                        Surface(
                            onClick = { onSelectAnswer(question.questionId, option.optionId, isSingle) },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSingle) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelectAnswer(question.questionId, option.optionId, true) }
                                    )
                                } else {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onSelectAnswer(question.questionId, option.optionId, false) }
                                    )
                                }
                                Text(
                                    text = option.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider()
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(session.questions) { index, q ->
                        val hasAnswer = state.selectedAnswers[q.questionId]?.isNotEmpty() == true
                        QuestionChip(
                            number = index + 1,
                            isCurrent = index == state.currentIndex,
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
                        enabled = state.currentIndex > 0
                    ) { Text("← Înapoi") }

                    Button(
                        onClick = if (isLast) onSubmit else onNext,
                        modifier = Modifier.weight(1f),
                        enabled = selectedAnswers.isNotEmpty()
                    ) {
                        Text(if (isLast) "Trimite" else "Înainte →")
                    }
                }
            }
        }
    }
}
