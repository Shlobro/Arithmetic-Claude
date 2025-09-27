package com.example.arithmiticpracticeclaude.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

class SoundManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isSoundEnabled = true

    fun playCorrectSound() {
        if (isSoundEnabled) {
            // Using ToneGenerator for sound effects as a simple alternative
            try {
                val toneGen = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_NOTIFICATION,
                    100
                )
                toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP)
                toneGen.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playIncorrectSound() {
        if (isSoundEnabled) {
            // Using ToneGenerator for incorrect answer
            try {
                val toneGen = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_NOTIFICATION,
                    100
                )
                toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
                toneGen.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playLevelUpSound() {
        if (isSoundEnabled) {
            // Using ToneGenerator for level up
            try {
                val toneGen = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_NOTIFICATION,
                    100
                )
                toneGen.startTone(android.media.ToneGenerator.TONE_PROP_ACK)
                toneGen.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun isSoundEnabled(): Boolean {
        return isSoundEnabled
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}