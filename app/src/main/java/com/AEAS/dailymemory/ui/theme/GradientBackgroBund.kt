package com.AEAS.dailymemory.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.AEAS.dailymemory.R

@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BgGradientStart, BgGradientEnd)
                )
            )
    ) {
        Bubbles()
        content()
    }
}

@Composable
fun FloatingMascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    drawableId: Int = R.drawable.cerebro
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_float")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_y"
    )

    Image(
        painter = painterResource(id = drawableId),
        contentDescription = "Mascota Daily Memory",
        modifier = modifier
            .size(size)
            .offset(y = offsetY.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun Bubbles() {
    val bubbles = listOf(
        BubbleSpec(size = 150.dp, duration = 16000, startX = 0.1f, delay = 0),
        BubbleSpec(size = 90.dp,  duration = 14000, startX = 0.7f, delay = 3000),
        BubbleSpec(size = 90.dp,  duration = 18000, startX = 0.5f, delay = 8000),
        BubbleSpec(size = 60.dp,  duration = 12000, startX = 0.8f, delay = 4000),
        BubbleSpec(size = 100.dp, duration = 15000, startX = 0.2f, delay = 6000),
        // Extras para rellenar
        BubbleSpec(size = 120.dp, duration = 17000, startX = 0.85f, delay = 1000),
        BubbleSpec(size = 70.dp,  duration = 13000, startX = 0.3f,  delay = 5000)
    )

    Box(Modifier.fillMaxSize()) {
        bubbles.forEach { spec -> RisingBubble(spec) }
    }
}

private data class BubbleSpec(
    val size: Dp,
    val duration: Int,
    val startX: Float,
    val delay: Int = 0,
    val alpha: Float = 0.4f
)

@Composable
private fun RisingBubble(spec: BubbleSpec) {
    val infinite = rememberInfiniteTransition(label = "bubble")

    val offsetY by infinite.animateFloat(
        initialValue = 1.2f,
        targetValue = -0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(spec.duration, delayMillis = spec.delay, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "y"
    )

    val alpha by infinite.animateFloat(
        initialValue = 0f, targetValue = spec.alpha,
        animationSpec = infiniteRepeatable(
            animation = tween(spec.duration / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(unbounded = true, align = Alignment.TopStart)
            .offset(
                x = (spec.startX * 400).dp,
                y = (offsetY * 800).dp
            )
            .size(spec.size)
            .clip(CircleShape)
            .background(Color(0xFF56E7C3).copy(alpha = alpha))
    )
}
