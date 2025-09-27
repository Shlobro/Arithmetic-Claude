package com.example.arithmiticpracticeclaude.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun ConfettiEffect(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val particles = remember {
        List(30) {
            ConfettiParticle(
                Random.nextFloat() * with(density) { 400.dp.toPx() },
                Random.nextFloat() * with(density) { 400.dp.toPx() },
                Random.nextFloat() * 360f,
                Random.nextFloat() * 5f + 2f,
                Color(Random.nextLong(0xFFFFFFFF))
            )
        }
    }

    var animationProgress by remember { mutableStateOf(0f) }

    val animationSpec = tween<Float>(
        durationMillis = 2000,
        easing = LinearEasing
    )

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = animationSpec
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animationProgress = 1f
        } else {
            animationProgress = 0f
        }
    }

    if (animatedProgress > 0f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            particles.forEach { particle ->
                particle.draw(this, animatedProgress)
            }
        }
    }
}

@Composable
fun CelebrationStars(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val stars = remember {
        List(15) {
            StarParticle(
                Random.nextFloat() * with(density) { 400.dp.toPx() },
                Random.nextFloat() * with(density) { 400.dp.toPx() },
                Random.nextFloat() * 10f + 5f,
                Random.nextFloat() * 2f + 1f
            )
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        )
    )

    if (animatedProgress > 0f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            stars.forEach { star ->
                star.draw(this, animatedProgress)
            }
        }
    }
}

private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val rotation: Float,
    val speed: Float,
    val color: Color
) {
    fun draw(drawScope: DrawScope, progress: Float) {
        val x = initialX + sin(progress * PI * 2 + rotation).toFloat() * 50f
        val y = initialY + progress * speed * 200f
        val alpha = (1f - progress).coerceAtLeast(0f)
        val size = (10f * (1f - progress * 0.5f)).coerceAtLeast(2f)

        drawScope.drawCircle(
            color = color.copy(alpha = alpha),
            radius = size,
            center = Offset(x, y)
        )
    }
}

private data class StarParticle(
    val x: Float,
    val y: Float,
    val maxSize: Float,
    val pulseSpeed: Float
) {
    fun draw(drawScope: DrawScope, progress: Float) {
        val pulse = sin(progress * PI * pulseSpeed).toFloat()
        val size = maxSize * (0.5f + 0.5f * pulse) * (1f - progress * 0.3f)
        val alpha = (1f - progress * 0.7f).coerceAtLeast(0f)

        drawScope.drawCircle(
            color = Color.Yellow.copy(alpha = alpha),
            radius = size,
            center = Offset(x, y)
        )

        // Draw star rays
        val rayLength = size * 2f
        for (i in 0..7) {
            val angle = i * PI / 4
            val startX = x + cos(angle).toFloat() * size
            val startY = y + sin(angle).toFloat() * size
            val endX = x + cos(angle).toFloat() * rayLength
            val endY = y + sin(angle).toFloat() * rayLength

            drawScope.drawLine(
                color = Color.Yellow.copy(alpha = alpha * 0.7f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun ShakeEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shakeOffset by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = if (isActive) {
            repeatable(
                iterations = 3,
                animation = tween(100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(0)
        }
    )

    val offsetX = sin(shakeOffset * PI * 8).toFloat() * 10f

    androidx.compose.foundation.layout.Box(
        modifier = modifier.graphicsLayer(translationX = offsetX)
    ) {
        content()
    }
}