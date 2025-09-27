package com.example.arithmiticpracticeclaude.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var isHapticEnabled = true

    fun correctAnswerFeedback() {
        if (isHapticEnabled && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Light tick for correct answer
                vibrator.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    fun incorrectAnswerFeedback() {
        if (isHapticEnabled && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Double vibration for incorrect answer
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 150, 100, 150),
                        intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 150, 100, 150), -1)
            }
        }
    }

    fun levelUpFeedback() {
        if (isHapticEnabled && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Celebration pattern for level up
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 50, 100, 50, 200),
                        intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 50, 100, 50, 200), -1)
            }
        }
    }

    fun buttonPressFeedback() {
        if (isHapticEnabled && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Light feedback for button press
                vibrator.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    fun toggleHaptic() {
        isHapticEnabled = !isHapticEnabled
    }

    fun setHapticEnabled(enabled: Boolean) {
        isHapticEnabled = enabled
    }

    fun isHapticEnabled(): Boolean {
        return isHapticEnabled
    }
}