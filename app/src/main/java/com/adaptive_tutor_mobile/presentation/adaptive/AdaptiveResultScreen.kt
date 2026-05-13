package com.adaptive_tutor_mobile.presentation.adaptive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.data.remote.dto.QuestionForAttemptReportDTO
import com.adaptive_tutor_mobile.presentation.components.AdaptiveTopBar
import com.adaptive_tutor_mobile.presentation.components.ScoreCircle
import com.adaptive_tutor_mobile.presentation.components.EmptyScreen
import androidx.compose.material.icons.filled.Assessment

@Composable
fun AdaptiveResultScreen(
    onBackToHome: () -> Unit,
    viewModel: AdaptiveViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val result = state.result
    val questions = state.session?.questions.orEmpty()

    Scaffold(
        topBar = { AdaptiveTopBar(title = "Rezultat sesiune", onBack = onBackToHome) }
    ) { innerPadding ->
        if (result == null) {
            EmptyScreen(
                message = "Nu există încă un rezultat pentru sesiunea adaptivă.",
                icon = Icons.Filled.Assessment
            )
            return@Scaffold
        }

        val scorePercent = result.scorePercent ?: 0.0
        val passed = result.passed == true

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
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
                        if (result.score != null) {
                            Text(
                                text = "Scor: ${String.format("%.1f", result.score)}",
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

            itemsIndexed(result.questions.orEmpty()) { index, rq ->
                val original = questions.firstOrNull { it.questionId == rq.questionId }
                AdaptiveResultQuestionCard(
                    index = index + 1,
                    reportQuestion = rq,
                    selectedTexts = rq.selectedOptionIds.orEmpty().mapNotNull { id ->
                        original?.options.orEmpty().firstOrNull { it.optionId == id }?.text
                    },
                    correctTexts = rq.correctOptionIds.orEmpty().mapNotNull { id ->
                        original?.options.orEmpty().firstOrNull { it.optionId == id }?.text
                    }
                )
            }

            item {
                Button(onClick = onBackToHome, modifier = Modifier.fillMaxWidth()) {
                    Text("Înapoi la home")
                }
            }
        }
    }
}

@Composable
private fun AdaptiveResultQuestionCard(
    index: Int,
    reportQuestion: QuestionForAttemptReportDTO,
    selectedTexts: List<String>,
    correctTexts: List<String>
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (selectedTexts.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Răspunsul tău:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = selectedTexts.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = accent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!correct && correctTexts.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Răspuns corect:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = correctTexts.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
