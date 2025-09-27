package com.example.arithmiticpracticeclaude.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arithmiticpracticeclaude.data.GameRepository
import com.example.arithmiticpracticeclaude.ui.GameViewModel
import com.example.arithmiticpracticeclaude.ui.GameViewModelFactory
import com.example.arithmiticpracticeclaude.ui.HapticManager
import com.example.arithmiticpracticeclaude.ui.SoundManager
import com.example.arithmiticpracticeclaude.ui.theme.ThemeManager
import com.example.arithmiticpracticeclaude.ui.theme.ThemeMode
import com.example.arithmiticpracticeclaude.ui.theme.rememberThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = rememberThemeManager(context)
    val soundManager = remember { SoundManager(context) }
    val hapticManager = remember { HapticManager(context) }
    val scope = rememberCoroutineScope()

    val currentThemeMode by themeManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    var soundEnabled by remember { mutableStateOf(soundManager.isSoundEnabled()) }
    var hapticEnabled by remember { mutableStateOf(hapticManager.isHapticEnabled()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
            // Theme Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ThemeMode.values().forEach { mode ->
                        val (icon, title, description) = when (mode) {
                            ThemeMode.SYSTEM -> Triple(
                                Icons.Default.Phone,
                                "System Default",
                                "Use device theme setting"
                            )
                            ThemeMode.LIGHT -> Triple(
                                Icons.Default.Info,
                                "Light Mode",
                                "Always use light theme"
                            )
                            ThemeMode.DARK -> Triple(
                                Icons.Default.Settings,
                                "Dark Mode",
                                "Always use dark theme"
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentThemeMode == mode,
                                    onClick = {
                                        hapticManager.buttonPressFeedback()
                                        scope.launch {
                                            themeManager.setThemeMode(mode)
                                        }
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = currentThemeMode == mode,
                                onClick = {
                                    hapticManager.buttonPressFeedback()
                                    scope.launch {
                                        themeManager.setThemeMode(mode)
                                    }
                                }
                            )

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (currentThemeMode == mode) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (currentThemeMode == mode) {
                                        FontWeight.Medium
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Audio Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Audio & Feedback",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sound Effects Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sound Effects",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Play sounds for correct/incorrect answers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { enabled ->
                                soundEnabled = enabled
                                soundManager.setSoundEnabled(enabled)
                                hapticManager.buttonPressFeedback()
                            }
                        )
                    }

                    Divider()

                    // Haptic Feedback Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vibration",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Vibrate on button presses and feedback",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { enabled ->
                                hapticEnabled = enabled
                                hapticManager.setHapticEnabled(enabled)
                                if (enabled) {
                                    hapticManager.buttonPressFeedback()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Cleanup sound manager when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }
}