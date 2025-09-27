package com.example.arithmiticpracticeclaude.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemeManager(private val context: Context) {
    private val THEME_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    private val SYSTEM_DEFAULT_KEY = booleanPreferencesKey("use_system_default")

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val useSystemDefault = preferences[SYSTEM_DEFAULT_KEY] ?: true
            val isDarkMode = preferences[THEME_MODE_KEY] ?: false

            when {
                useSystemDefault -> ThemeMode.SYSTEM
                isDarkMode -> ThemeMode.DARK
                else -> ThemeMode.LIGHT
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            when (mode) {
                ThemeMode.SYSTEM -> {
                    preferences[SYSTEM_DEFAULT_KEY] = true
                    preferences.remove(THEME_MODE_KEY)
                }
                ThemeMode.LIGHT -> {
                    preferences[SYSTEM_DEFAULT_KEY] = false
                    preferences[THEME_MODE_KEY] = false
                }
                ThemeMode.DARK -> {
                    preferences[SYSTEM_DEFAULT_KEY] = false
                    preferences[THEME_MODE_KEY] = true
                }
            }
        }
    }
}

@Composable
fun rememberThemeManager(context: Context): ThemeManager {
    return remember { ThemeManager(context) }
}

@Composable
fun isDarkTheme(themeMode: ThemeMode): Boolean {
    return when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}