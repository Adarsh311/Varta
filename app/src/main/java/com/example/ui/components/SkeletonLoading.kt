package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim.value - 200f, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    Box(
        modifier = modifier
            .background(brush = brush, shape = shape)
    )
}

@Composable
fun SkeletonEditorialFeed(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Lead story shimmer with image box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerPlaceholder(modifier = Modifier.width(80.dp).height(16.dp))
                Spacer(modifier = Modifier.width(12.dp))
                ShimmerPlaceholder(modifier = Modifier.width(50.dp).height(12.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(26.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.85f).height(26.dp))
            Spacer(modifier = Modifier.height(10.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(16.dp))
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp))
            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }

        // Secondary stories shimmer
        repeat(4) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ShimmerPlaceholder(modifier = Modifier.width(60.dp).height(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            ShimmerPlaceholder(modifier = Modifier.width(40.dp).height(12.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(20.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp))
                    }
                    ShimmerPlaceholder(
                        modifier = Modifier.size(92.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }
    }
}
