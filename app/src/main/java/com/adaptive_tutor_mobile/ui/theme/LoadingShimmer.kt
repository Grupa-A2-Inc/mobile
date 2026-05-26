package com.adaptive_tutor_mobile.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Brush-ul animat — folosit intern de toate variantele de shimmer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun shimmerBrush(): Brush {
    val isDark = isSystemInDarkTheme()
    val base = if (isDark) ShimmerBaseDark else ShimmerBase
    val highlight = if (isDark) ShimmerHighlightDark else ShimmerHighlight

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(x = translateAnim - 300f, y = 0f),
        end = Offset(x = translateAnim, y = 0f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Bloc de shimmer de bază — dreptunghi animat
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp,
    brush: Brush = shimmerBrush()
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Card shimmer — înlocuiește un CourseCard în timp ce se încarcă
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CourseCardShimmer(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badge categorie
            ShimmerBlock(
                modifier = Modifier.width(80.dp),
                height = 12.dp,
                brush = brush
            )
            Spacer(Modifier.height(10.dp))
            // Titlu curs — linie lungă
            ShimmerBlock(
                modifier = Modifier.fillMaxWidth(0.85f),
                height = 18.dp,
                brush = brush
            )
            Spacer(Modifier.height(6.dp))
            // Subtitlu — linie mai scurtă
            ShimmerBlock(
                modifier = Modifier.fillMaxWidth(0.55f),
                height = 14.dp,
                brush = brush
            )
            Spacer(Modifier.height(16.dp))
            // Bara de progres
            ShimmerBlock(
                modifier = Modifier.fillMaxWidth(),
                height = 8.dp,
                cornerRadius = 4.dp,
                brush = brush
            )
            Spacer(Modifier.height(6.dp))
            // Procent progres
            ShimmerBlock(
                modifier = Modifier.width(48.dp),
                height = 11.dp,
                brush = brush
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Listă completă de shimmer — înlocuiește CircularProgressIndicator pe ecrane
// cu liste. Folosiți asta oriunde aveați CircularProgressIndicator într-o listă.
//
// Exemplu de utilizare:
//   is CoursesUiState.Loading -> LoadingShimmerList()
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoadingShimmerList(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(itemCount) {
            CourseCardShimmer(
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shimmer pentru header (ex: bannerul de greeting din DashboardTab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HeaderShimmer(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Shimmer simplu pentru un rând cu iconiță + text (ex: liste de notificări)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RowShimmer(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar / iconiță
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(50))
                .background(brush)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShimmerBlock(modifier = Modifier.fillMaxWidth(0.7f), height = 14.dp, brush = brush)
            ShimmerBlock(modifier = Modifier.fillMaxWidth(0.5f), height = 12.dp, brush = brush)
        }
    }
}