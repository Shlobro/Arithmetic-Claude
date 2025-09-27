package com.example.arithmiticpracticeclaude.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arithmiticpracticeclaude.data.AchievementCategory
import com.example.arithmiticpracticeclaude.data.GameRepository
import com.example.arithmiticpracticeclaude.ui.GameViewModel
import com.example.arithmiticpracticeclaude.ui.GameViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { GameRepository(context) }
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(repository)
    )

    val gameState by viewModel.gameState.collectAsState()
    val achievements = viewModel.achievementManager.getAllAchievements()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Achievement summary
            AchievementSummaryCard(
                totalAchievements = achievements.size,
                unlockedAchievements = achievements.count { it.isUnlocked }
            )

            // Achievement categories
            val categories = AchievementCategory.values()
            var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }

            // Category filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { selectedCategory = null },
                    label = { Text("All") },
                    selected = selectedCategory == null
                )

                categories.forEach { category ->
                    FilterChip(
                        onClick = { selectedCategory = category },
                        label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        selected = selectedCategory == category
                    )
                }
            }

            // Achievement grid
            val filteredAchievements = if (selectedCategory != null) {
                achievements.filter { it.category == selectedCategory }
            } else {
                achievements
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredAchievements) { achievement ->
                    val progress = viewModel.achievementManager.getAchievementProgress(achievement, gameState)
                    AchievementBadge(
                        achievement = achievement,
                        progress = progress,
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementSummaryCard(
    totalAchievements: Int,
    unlockedAchievements: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Achievement Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$unlockedAchievements / $totalAchievements Unlocked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "${(unlockedAchievements.toFloat() / totalAchievements * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            LinearProgressIndicator(
                progress = unlockedAchievements.toFloat() / totalAchievements,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}