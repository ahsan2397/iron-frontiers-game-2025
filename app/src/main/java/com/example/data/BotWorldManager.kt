package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CommanderProfile
import com.example.model.RegionState
import kotlin.math.abs

data class RankedPlayer(
    val id: String,
    val name: String,
    val partyTag: String,
    val partyName: String,
    val stateName: String,
    val regionId: String,
    val regionName: String,
    val bornRegionId: String,
    val level: Long,
    val strength: Long,
    val endurance: Long,
    val education: Long,
    val totalDamage: Long,
    val hiddenWealthScore: Long,
    val joinedDay: Int,
    val isCurrentUser: Boolean = false
) {
    fun wealthTierLabel(rankOneBased: Int): String = when {
        rankOneBased == 1 -> "RICHEST #1"
        rankOneBased <= 3 -> "TYCOON"
        hiddenWealthScore >= 5_000_000_000L -> "ULTRA RICH"
        hiddenWealthScore >= 1_500_000_000L -> "WEALTHY"
        hiddenWealthScore >= 500_000_000L -> "PROSPEROUS"
        else -> "RISING"
    }
}

class BotWorldManager(
    context: Context,
    private val userId: String
) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "iron_frontiers_bot_world_$userId",
        Context.MODE_PRIVATE
    )

    private fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    fun ensureDailyPopulationUpdated(): Int {
        val today = currentEpochDay()
        val startDay = prefs.getLong(KEY_START_DAY, 0L)
        if (startDay == 0L) {
            prefs.edit()
                .putLong(KEY_START_DAY, today)
                .putLong(KEY_LAST_DAY, today)
                .putInt(KEY_SIMULATED_EXTRA_DAYS, 0)
                .apply()
            return 1
        }
        val lastDay = prefs.getLong(KEY_LAST_DAY, startDay)
        if (today > lastDay) {
            prefs.edit().putLong(KEY_LAST_DAY, today).apply()
        }
        return totalGameDays()
    }

    fun simulateNextDay(): Int {
        ensureDailyPopulationUpdated()
        val extra = prefs.getInt(KEY_SIMULATED_EXTRA_DAYS, 0) + 1
        prefs.edit().putInt(KEY_SIMULATED_EXTRA_DAYS, extra).apply()
        return totalGameDays()
    }

    fun totalGameDays(): Int {
        val startDay = prefs.getLong(KEY_START_DAY, currentEpochDay())
        val realDays = (currentEpochDay() - startDay).coerceAtLeast(0L).toInt()
        val extra = prefs.getInt(KEY_SIMULATED_EXTRA_DAYS, 0)
        return 1 + realDays + extra
    }

    fun buildRankedRoster(
        profile: CommanderProfile,
        regions: List<RegionState>
    ): List<RankedPlayer> {
        val days = ensureDailyPopulationUpdated().coerceAtMost(90)
        val regionsById = regions.associateBy { it.id }
        val currentRegion = regionsById[profile.currentRegionId]
        val playerTag = profile.partyName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "NRP" }

        val userEntry = RankedPlayer(
            id = "user_$userId",
            name = profile.commanderName,
            partyTag = playerTag,
            partyName = profile.partyName,
            stateName = profile.stateName,
            regionId = profile.currentRegionId,
            regionName = currentRegion?.name ?: "Punjab - Pakistan",
            bornRegionId = profile.birthRegionId,
            level = profile.level,
            strength = profile.strength,
            endurance = profile.endurance,
            education = profile.education,
            totalDamage = profile.totalDamage,
            hiddenWealthScore = if (profile.kingMode) Long.MAX_VALUE / 2 else (profile.money + profile.gold * 1_000_000L),
            joinedDay = 1,
            isCurrentUser = true
        )

        val bots = mutableListOf<RankedPlayer>()

        // Day 1 founding citizens & commanders across clean real countries
        FOUNDING_BOTS.forEachIndexed { idx, seed ->
            val reg = regionsById[seed.regionId]
            val ageBonus = (days - 1).coerceAtLeast(0)
            bots += RankedPlayer(
                id = "bot_found_$idx",
                name = seed.name,
                partyTag = seed.partyTag,
                partyName = seed.partyName,
                stateName = reg?.controller ?: seed.country,
                regionId = seed.regionId,
                regionName = reg?.name ?: seed.fallbackRegionName,
                bornRegionId = seed.regionId,
                level = seed.baseLevel + (ageBonus / 2),
                strength = seed.baseStrength + (ageBonus * 2),
                endurance = seed.baseEndurance + (ageBonus * 3),
                education = seed.baseEducation + ageBonus,
                totalDamage = seed.baseDamage + (ageBonus * 18_500L),
                hiddenWealthScore = seed.wealthScore + (ageBonus * 125_000_000L),
                joinedDay = 1
            )
        }

        // Daily arriving new players (each new day adds 3 new players, including Punjab recruits!)
        val nodes = GameWorldSeed.mapNodes
        for (day in 2..days) {
            for (slot in 0 until BOTS_PER_NEW_DAY) {
                val globalIndex = (day - 2) * BOTS_PER_NEW_DAY + slot
                // Slot 0 each day is born in Punjab so Punjab grows & joins player parties!
                val bornNode = if (slot == 0) {
                    nodes.first { it.id == "punjab" }
                } else {
                    nodes[abs(globalIndex * 7 + day * 3) % nodes.size]
                }
                val reg = regionsById[bornNode.id]
                val namePrefix = DAILY_NAMES[globalIndex % DAILY_NAMES.size]
                val botName = "${namePrefix}_${day}${slot + 1}"
                val daysActive = (days - day).coerceAtLeast(0)
                val joinedPlayerParty = bornNode.id == "punjab" && profile.partyId.isNotBlank()
                bots += RankedPlayer(
                    id = "bot_day_${day}_$slot",
                    name = botName,
                    partyTag = if (joinedPlayerParty) playerTag else "NEW",
                    partyName = if (joinedPlayerParty) profile.partyName else "${bornNode.country} Citizens",
                    stateName = reg?.controller ?: bornNode.country,
                    regionId = bornNode.id,
                    regionName = reg?.name ?: bornNode.fullName,
                    bornRegionId = bornNode.id,
                    level = 1L + (daysActive / 2L),
                    strength = 10L + daysActive,
                    endurance = 100L + (daysActive * 2L),
                    education = 10L + daysActive,
                    totalDamage = (daysActive * 6_500L) + ((slot + 1) * 1_200L),
                    hiddenWealthScore = 900_000_000L + (daysActive * 95_000_000L) + (slot * 45_000_000L),
                    joinedDay = day
                )
            }
        }

        return (listOf(userEntry) + bots)
    }

    fun getPunjabBotsInParty(
        profile: CommanderProfile,
        regions: List<RegionState>
    ): List<RankedPlayer> {
        return buildRankedRoster(profile, regions).filter {
            !it.isCurrentUser && it.bornRegionId == "punjab"
        }
    }

    private data class FoundingBotSeed(
        val name: String,
        val partyTag: String,
        val partyName: String,
        val regionId: String,
        val country: String,
        val fallbackRegionName: String,
        val baseLevel: Long,
        val baseStrength: Long,
        val baseEndurance: Long,
        val baseEducation: Long,
        val baseDamage: Long,
        val wealthScore: Long
    )

    companion object {
        private const val KEY_START_DAY = "bot_world_start_day"
        private const val KEY_LAST_DAY = "bot_world_last_day"
        private const val KEY_SIMULATED_EXTRA_DAYS = "bot_world_extra_days"
        private const val BOTS_PER_NEW_DAY = 3

        private val DAILY_NAMES = listOf(
            "Tariq_PK", "Hamza_LHR", "Usman_ISB", "Raza_KHI",
            "Kenji_TYO", "Lukas_BER", "Marco_ROM", "Omar_CAI",
            "Liam_LDN", "Arjun_DEL", "Ali_IST", "Ivan_MSC"
        )

        private val FOUNDING_BOTS = listOf(
            FoundingBotSeed("Citizen_Lahore01", "NRP", "National Reform Party", "punjab", "Pakistan", "Punjab - Pakistan", 14L, 48L, 160L, 42L, 820_000L, 8_400_000_000L),
            FoundingBotSeed("Trader_Karachi", "PKT", "Federal Industrial Front", "karachi", "Pakistan", "Karachi - Pakistan", 12L, 35L, 145L, 50L, 510_000L, 14_200_000_000L),
            FoundingBotSeed("Senator_Punjab02", "NRP", "National Reform Party", "punjab", "Pakistan", "Punjab - Pakistan", 11L, 38L, 140L, 46L, 495_000L, 4_200_000_000L),
            FoundingBotSeed("General_Rawal", "NRP", "National Reform Party", "punjab", "Pakistan", "Punjab - Pakistan", 16L, 62L, 180L, 38L, 1_340_000L, 3_800_000_000L),
            FoundingBotSeed("Marshal_Ankara", "GRN", "Civic Unity Movement", "ankara", "Turkey", "Ankara - Turkey", 15L, 55L, 155L, 44L, 1_110_000L, 6_700_000_000L),
            FoundingBotSeed("Tycoon_Dubai", "GCF", "Global Commerce Bloc", "dubai", "UAE", "Dubai - UAE", 13L, 30L, 130L, 60L, 380_000L, 22_900_000_000L),
            FoundingBotSeed("Commander_Tokyo", "TYO", "Pacific Defense", "tokyo", "Japan", "Tokyo - Japan", 17L, 64L, 175L, 52L, 1_520_000L, 9_500_000_000L),
            FoundingBotSeed("Banker_London", "LDN", "Thames Alliance", "london", "UK", "London - UK", 11L, 28L, 125L, 58L, 290_000L, 18_100_000_000L),
            FoundingBotSeed("Guard_Punjab03", "NRP", "National Reform Party", "punjab", "Pakistan", "Punjab - Pakistan", 9L, 34L, 135L, 30L, 410_000L, 2_100_000_000L),
            FoundingBotSeed("Enforcer_Delhi", "DEL", "Republic Front", "delhi", "India", "Delhi - India", 13L, 49L, 150L, 39L, 760_000L, 5_100_000_000L),
            FoundingBotSeed("Builder_Berlin", "BER", "European Union League", "berlin", "Germany", "Berlin - Germany", 10L, 32L, 128L, 54L, 340_000L, 7_300_000_000L),
            FoundingBotSeed("Rookie_Lahore04", "NRP", "National Reform Party", "punjab", "Pakistan", "Punjab - Pakistan", 5L, 22L, 120L, 24L, 145_000L, 1_250_000_000L)
        )
    }
}
