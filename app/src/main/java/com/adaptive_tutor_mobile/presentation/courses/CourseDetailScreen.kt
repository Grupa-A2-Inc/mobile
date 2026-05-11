package com.adaptive_tutor_mobile.presentation.course

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adaptive_tutor_mobile.domain.model.Chapter
import com.adaptive_tutor_mobile.domain.model.CourseDetail
import com.adaptive_tutor_mobile.domain.model.LessonSummary

@Composable
fun CourseDetailScreen(
    onNavigateToLesson: (lessonId: String) -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> FullScreenLoading()
        uiState.error != null -> ErrorMessage(
            message   = uiState.error!!,
            onRetry   = viewModel::loadCourseDetail
        )
        uiState.courseDetail != null -> CourseDetailContent(
            courseDetail     = uiState.courseDetail!!,
            expandedChapters = uiState.expandedChapters,
            onToggleChapter  = viewModel::toggleChapter,
            onLessonClick    = onNavigateToLesson
        )
    }
}

@Composable
private fun CourseDetailContent(
    courseDetail: CourseDetail,
    expandedChapters: Set<String>,
    onToggleChapter: (String) -> Unit,
    onLessonClick: (String) -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(bottom = 24.dp)
    ) {
        item {
            CourseHeader(
                title       = courseDetail.title,
                description = courseDetail.description
            )
        }

        items(
            items = courseDetail.chapters,
            key   = { it.id }
        ) { chapter ->
            ChapterItem(
                chapter     = chapter,
                isExpanded  = chapter.id in expandedChapters,
                onToggle    = { onToggleChapter(chapter.id) },
                onLesson    = onLessonClick
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun CourseHeader(
    title: String,
    description: String
) {
    Surface(
        color    = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text  = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text  = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: Chapter,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLesson: (String) -> Unit
) {
    Column {
        // Chapter row – acts as expand/collapse trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = chapter.title,
                style    = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector        = if (isExpanded) Icons.Default.ExpandLess
                else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Restrânge capitol"
                else "Extinde capitol"
            )
        }

        // Lesson list – shown only when expanded
        AnimatedVisibility(
            visible = isExpanded,
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column {
                chapter.lessons.forEach { lesson ->
                    LessonItem(
                        lesson    = lesson,
                        onClick   = { onLesson(lesson.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonItem(
    lesson: LessonSummary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 32.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = lesson.title,
            style    = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (lesson.hasTest) {
            Icon(
                imageVector        = Icons.Default.Assignment,
                contentDescription = "Are test",
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(
        modifier        = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Box(
        modifier        = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Reîncearcă")
            }
        }
    }
}