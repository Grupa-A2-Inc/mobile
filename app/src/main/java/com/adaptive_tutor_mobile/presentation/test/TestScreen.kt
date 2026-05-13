package com.adaptive_tutor_mobile.presentation.test

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    viewModel: TestViewModel = hiltViewModel(),
    onNavigateToResult: (String) -> Unit
) {
    val attempt by viewModel.attempt.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val submittedAttemptId by viewModel.isSubmitted.collectAsState()

    LaunchedEffect(submittedAttemptId) {
        submittedAttemptId?.let { onNavigateToResult(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test în desfășurare") },
                actions = {
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    val color = if (remainingSeconds < 30) Color.Red else MaterialTheme.colorScheme.onPrimary
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        color = color,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        attempt?.let { data ->
            val totalQuestions = data.questions.size
            val currentQuestion = data.questions[currentIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Întrebarea ${currentIndex + 1} / $totalQuestions",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuestionCard(
                    question = currentQuestion,
                    selectedOptionIds = selectedAnswers[currentQuestion.id] ?: emptyList(),
                    onAnswerSelected = { answers ->
                        viewModel.selectAnswer(currentQuestion.id, answers)
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.prevQuestion() },
                        enabled = currentIndex > 0
                    ) {
                        Text("Previous")
                    }

                    if (currentIndex == totalQuestions - 1) {
                        Button(onClick = { viewModel.submit() }) {
                            Text("Submit")
                        }
                    } else {
                        Button(onClick = { viewModel.nextQuestion() }) {
                            Text("Next")
                        }
                    }
                }
            }
        } ?: CenterCircularProgressIndicator()
    }
}

@Composable
fun CenterCircularProgressIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        CircularProgressIndicator()
    }
}