package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class PartyWageConfig(
    val partyId: String,
    val goldWage: Long,
    val cashWage: Long,
    val foundationDate: String,
    val historyEntries: List<String>
)

class PartyWageManager(
    context: Context,
    private val userId: String
) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "iron_frontiers_party_wages_$userId",
        Context.MODE_PRIVATE
    )

    fun getConfig(partyId: String, isPlayerOrOwnerParty: Boolean): PartyWageConfig {
        val defaultCash = if (isPlayerOrOwnerParty) 50_000_000_000L else 4_500_000_000L
        val defaultGold = if (isPlayerOrOwnerParty) 640L else 120L

        val rawCash = prefs.getLong("${KEY_CASH_PREFIX}$partyId", defaultCash)
        val clampedCash = if (isPlayerOrOwnerParty) {
            rawCash.coerceAtLeast(0L)
        } else {
            rawCash.coerceIn(0L, BOT_PARTY_MAX_WAGE_UNDER_10B)
        }
        val gold = prefs.getLong("${KEY_GOLD_PREFIX}$partyId", defaultGold).coerceAtLeast(0L)
        val historyRaw = prefs.getString("${KEY_HISTORY_PREFIX}$partyId", null)
        val history = historyRaw
            ?.split("\n")
            ?.filter { it.isNotBlank() }
            ?: listOf(
                "Founded in Punjab - Pakistan with 30 citizens.",
                "Wage policy: Level < 40 ineligible; Level 40+ earns scaled Party Wage.",
                "Election voting requires Level 60+."
            )

        return PartyWageConfig(
            partyId = partyId,
            goldWage = gold,
            cashWage = clampedCash,
            foundationDate = prefs.getString("${KEY_FOUNDED_PREFIX}$partyId", "Day 1 · 18 Nov 2026") ?: "Day 1 · 18 Nov 2026",
            historyEntries = history
        )
    }

    fun setPartyWages(
        partyId: String,
        isPlayerOrOwnerParty: Boolean,
        cashWage: Long,
        goldWage: Long,
        note: String
    ): PartyWageConfig {
        val safeCash = if (isPlayerOrOwnerParty) {
            cashWage.coerceAtLeast(0L)
        } else {
            cashWage.coerceIn(0L, BOT_PARTY_MAX_WAGE_UNDER_10B)
        }
        val safeGold = goldWage.coerceAtLeast(0L)
        val existing = getConfig(partyId, isPlayerOrOwnerParty)
        val updatedHistory = (listOf(note) + existing.historyEntries).take(12)

        prefs.edit()
            .putLong("${KEY_CASH_PREFIX}$partyId", safeCash)
            .putLong("${KEY_GOLD_PREFIX}$partyId", safeGold)
            .putString("${KEY_HISTORY_PREFIX}$partyId", updatedHistory.joinToString("\n"))
            .apply()

        return existing.copy(
            goldWage = safeGold,
            cashWage = safeCash,
            historyEntries = updatedHistory
        )
    }

    fun appendHistory(partyId: String, isPlayerOrOwnerParty: Boolean, entry: String) {
        val existing = getConfig(partyId, isPlayerOrOwnerParty)
        val updated = (listOf(entry) + existing.historyEntries).take(12)
        prefs.edit()
            .putString("${KEY_HISTORY_PREFIX}$partyId", updated.joinToString("\n"))
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_CASH_PREFIX = "party_cash_wage_"
        private const val KEY_GOLD_PREFIX = "party_gold_wage_"
        private const val KEY_FOUNDED_PREFIX = "party_founded_"
        private const val KEY_HISTORY_PREFIX = "party_history_"

        // Other bot ("hot") parties can NEVER reach 10 Billion ($10B)
        const val BOT_PARTY_MAX_WAGE_UNDER_10B = 9_999_999_999L
        const val MIN_LEVEL_FOR_PARTY_WAGE = 40L
        const val MIN_LEVEL_FOR_ELECTION_VOTE = 60L
    }
}
