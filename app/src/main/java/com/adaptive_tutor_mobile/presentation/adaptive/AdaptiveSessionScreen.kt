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
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.presentation.components.AdaptiveTopBar
import com.adaptive_tutor_mobile.presentation.components.ErrorScreen
import com.adaptive_tutor_mobile.presentation.components.LoadingScreen

@Composable
fun AdaptiveSessionScreen(
    onBackToHome: () -> Unit,
    onShowResult: () -> Unit,
    viewModel: AdaptiveViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.result) {
        if (state.result != null) {
            onShowResult()
        }
    }

    Scaffold(
        topBar = {
            AdaptiveTopBar(title = "Sesiune adaptivă")
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingScreen()

            state.errorMessage != null -> {
                val errorMessage = state.errorMessage ?: "A apărut o eroare"

                ErrorScreen(
                    message = errorMessage,
                    onRetry = { viewModel.clearError() }
                )
            }

            state.session == null -> AdaptiveStartContent(
                modifier = Modifier.padding(innerPadding),
                onStart = { subjectId, topicId, count ->
                    viewModel.startSession(
                        subjectId = subjectId,
                        topicId = topicId,
                        count = count
                    )
                },
                onBackToHome = onBackToHome
            )

            else -> AdaptiveQuestionContent(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onSelectAnswer = viewModel::selectAnswer,
                onNext = viewModel::nextQuestion,
                onSubmit = viewModel::submitSession
            )
        }
    }
}

@Composable
private fun AdaptiveStartContent(
    onStart: (Int, Int, Int) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectId by remember { mutableStateOf("1") }
    var topicId by remember { mutableStateOf("1") }
    var count by remember { mutableStateOf("5") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Pornește o sesiune adaptivă",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = subjectId,
                onValueChange = { subjectId = it.filter(Char::isDigit) },
                label = { Text("Subject ID") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = topicId,
                onValueChange = { topicId = it.filter(Char::isDigit) },
                label = { Text("Topic ID") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = count,
                onValueChange = { count = it.filter(Char::isDigit) },
                label = { Text("Număr exerciții") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    onStart(
                        subjectId.toIntOrNull() ?: 1,
                        topicId.toIntOrNull() ?: 1,
                        count.toIntOrNull() ?: 5
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start")
            }
        }

        item {
            Button(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Înapoi")
            }
        }
    }
}

@Composable
private fun AdaptiveQuestionContent(
    state: AdaptiveUiState,
    onSelectAnswer: (String, String) -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = state.session ?: return
    val exercise = session.exercises[state.currentIndex]
    val selectedAnswers = state.selectedAnswers[exercise.id].orEmpty()
    val isLastQuestion = state.currentIndex == session.exercises.lastIndex
    val progress = (state.currentIndex + 1).toFloat() / session.exercises.size.toFloat()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Întrebarea ${state.currentIndex + 1}/${session.exercises.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = exercise.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (exercise.type == "MULTIPLE_CHOICE") {
                            "Alege unul sau mai multe răspunsuri"
                        } else {
                            "Alege un singur răspuns"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                exercise.answers.forEach { answer ->
                    FilterChip(
                        selected = answer in selectedAnswers,
                        onClick = { onSelectAnswer(exercise.id, answer) },
                        label = { Text(answer) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = if (isLastQuestion) onSubmit else onNext,
                    modifier = Modifier.weight(1f),
                    enabled = selectedAnswers.isNotEmpty()
                ) {
                    Text(if (isLastQuestion) "Trimite" else "Următoarea")
                }
            }
        }
    }
}