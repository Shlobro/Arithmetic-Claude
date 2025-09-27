package com.example.arithmiticpracticeclaude.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arithmiticpracticeclaude.data.UnlockedAchievement
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AchievementNotificationOverlay(
    newAchievements: List<UnlockedAchievement>,
    onDismiss: () -> Unit
) {
    if (newAchievements.isNotEmpty()) {
        var currentIndex by remember { mutableStateOf(0) }
        val currentAchievement = newAchievements.getOrNull(currentIndex)

        LaunchedEffect(currentIndex) {
            if (currentIndex < newAchievements.size) {
                delay(3000) // Show each achievement for 3 seconds
                if (currentIndex < newAchievements.size - 1) {
                    currentIndex++
                } else {
                    onDismiss()
                }
            }
        }

        currentAchievement?.let { achievement ->
            AnimatedContent(
                targetState = achievement,
                transitionSpec = {
                    fadeIn(tween(500)) + slideInVertically(tween(500)) { -it } with
                    fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it }
                }
            ) { targetAchievement ->
                TopAchievementCard(
                    achievement = targetAchievement,
                    onDismiss = {
                        if (currentIndex < newAchievements.size - 1) {
                            currentIndex++
                        } else {
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TopAchievementCard(
    achievement: UnlockedAchievement,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(3000) // Auto-dismiss after 3 seconds
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.95f),
                                Color(0xFFFFA500).copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Achievement icon
                    Text(
                        text = achievement.achievement.icon,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🎉 Achievement Unlocked!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )

                        Text(
                            text = achievement.achievement.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: UnlockedAchievement,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.9f),
                                Color(0xFFFFA500).copy(alpha = 0.8f),
                                Color(0xFFFF8C00).copy(alpha = 0.7f)
                            ),
                            radius = 400f
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Trophy icon with animation
                    var iconScale by remember { mutableStateOf(0.5f) }

                    LaunchedEffect(Unit) {
                        iconScale = 1.2f
                        delay(200)
                        iconScale = 1f
                    }

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier
                            .size((48 * iconScale).dp)
                            .animateContentSize(),
                        tint = Color.White
                    )

                    // Achievement unlocked text
                    Text(
                        text = "🎉 ACHIEVEMENT UNLOCKED! 🎉",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Achievement emoji
                    Text(
                        text = achievement.achievement.icon,
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )

                    // Achievement title
                    Text(
                        text = achievement.achievement.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Achievement description
                    Text(
                        text = achievement.achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    // Dismiss button
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color.White, Color.White))
                        )
                    ) {
                        Text("Continue", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementBadge(
    achievement: com.example.arithmiticpracticeclaude.data.Achievement,
    progress: Float = 1f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.isUnlocked) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Achievement icon
            Text(
                text = achievement.icon,
                fontSize = 32.sp,
                color = if (achievement.isUnlocked) Color.Unspecified else Color.Gray
            )

            // Achievement title
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (achievement.isUnlocked) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                maxLines = 2
            )

            // Progress bar for locked achievements
            if (!achievement.isUnlocked && progress > 0f) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}