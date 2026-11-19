package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PerkTrainingState(
    val strengthEndMs: Long = 0L,
    val educationEndMs: Long = 0L,
    val enduranceEndMs: Long = 0L
) {
    fun endTimeFor(perk: String): Long = when (perk) {
        "strength" -> strengthEndMs
        "education" -> educationEndMs
        "endurance" -> enduranceEndMs
        else -> 0L
    }
}

class PerkTrainingManager(
    context: Context,
    private val userId: String
) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "iron_frontiers_training_$userId",
        Context.MODE_PRIVATE
    )

    private val _state = MutableStateFlow(readState())
    val state: StateFlow<PerkTrainingState> = _state.asStateFlow()

    private fun readState(): PerkTrainingState = PerkTrainingState(
        strengthEndMs = prefs.getLong(KEY_STRENGTH_END, 0L),
        educationEndMs = prefs.getLong(KEY_EDUCATION_END, 0L),
        enduranceEndMs = prefs.getLong(KEY_ENDURANCE_END, 0L)
    )

    fun startTraining(perk: String, currentPerkLevel: Long, nowMs: Long = System.currentTimeMillis()): Long {
        val durationMs = durationSecondsForLevel(currentPerkLevel) * 1000L
        val endMs = nowMs + durationMs
        prefs.edit().putLong(keyFor(perk), endMs).apply()
        _state.value = readState()
        return endMs
    }

    fun clearTraining(perk: String) {
        prefs.edit().remove(keyFor(perk)).apply()
        _state.value = readState()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        _state.value = PerkTrainingState()
    }

    private fun keyFor(perk: String): String = when (perk) {
        "strength" -> KEY_STRENGTH_END
        "education" -> KEY_EDUCATION_END
        "endurance" -> KEY_ENDURANCE_END
        else -> throw IllegalArgumentException("Unknown perk: $perk")
    }

    companion object {
        private const val KEY_STRENGTH_END = "strength_end_ms"
        private const val KEY_EDUCATION_END = "education_end_ms"
        private const val KEY_ENDURANCE_END = "endurance_end_ms"

        const val TWO_HOURS_SECONDS = 2L * 60L * 60L // 7200s = 2 hours start

        fun durationSecondsForLevel(currentPerkLevel: Long): Long {
            val safeLvl = currentPerkLevel.coerceAtLeast(1L)
            // First level-up starts at 2 hours (7200s), +30 minutes per subsequent level
            return TWO_HOURS_SECONDS + (safeLvl - 1L) * 1800L
        }

        fun formatCountdown(remainingMs: Long): String {
            val totalSec = (remainingMs / 1000L).coerceAtLeast(0L)
            val hours = totalSec / 3600L
            val minutes = (totalSec % 3600L) / 60L
            val seconds = totalSec % 60L
            return if (hours > 0L) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }
}
