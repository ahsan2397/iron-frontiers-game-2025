package com.example

import com.example.data.GameWorldSeed
import com.example.data.PerkTrainingManager
import com.example.data.RivalRegionsRepository
import com.example.model.CommanderProfile
import com.example.model.GameTab
import com.example.model.RegionState
import com.example.model.WarType
import com.example.model.WeaponType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun seven_tabs_exist() {
        assertEquals(7, GameTab.entries.size)
        assertTrue(WarType.entries.contains(WarType.TRAINING))
    }

    @Test
    fun forty_real_countries_seed_within_resource_bounds() {
        assertEquals(40, GameWorldSeed.mapNodes.size)
        GameWorldSeed.mapNodes.forEach { node ->
            assertTrue("${node.fullName} gold out of range", node.gold in 50L..250L)
            assertTrue("${node.fullName} oil out of range", node.oil in 50L..250L)
            assertTrue("${node.fullName} ore out of range", node.ore in 50L..200L)
            assertTrue("${node.fullName} wood out of range", node.wood in 50L..250L)
        }
    }

    @Test
    fun clean_start_stats_ten_thousand_drinks_and_punjab_shield() {
        val profile = GameWorldSeed.initialProfileMap(
            uid = "test_uid",
            commanderName = "Player One",
            stateName = "Pakistan",
            regionId = "punjab",
            partyId = "party_player",
            partyName = "National Reform Party",
            isOwner = false,
            setupCompleted = true
        )
        assertEquals(1L, profile["level"])
        assertEquals(1_000_000_000L, profile["money"])
        assertEquals(100L, profile["gold"])
        assertEquals(100L, profile["energy"])
        assertEquals(10_000L, profile["energyDrinks"])
        assertEquals(GameWorldSeed.CURRENT_WORLD_VERSION, profile["worldVersion"])
        assertEquals(5L, profile["tickets"])
        assertEquals(10L, profile["education"])
        assertEquals(10L, profile["strength"])
        assertEquals(100L, profile["endurance"])
        assertEquals(0L, profile["workExp"])
        assertEquals(1L, profile["eduLevel"])
        assertEquals(1L, profile["strLevel"])
        assertEquals(1L, profile["endLevel"])

        val punjab = RegionState(id = "punjab", name = "Punjab - Pakistan", conquerable = false)
        val tokyo = RegionState(id = "tokyo", name = "Tokyo - Japan", conquerable = true)
        assertTrue(punjab.isSovereignProtected)
        assertTrue(punjab.isCapitalStar)
        assertFalse(tokyo.isSovereignProtected)
    }

    @Test
    fun timed_perk_learning_starts_at_two_hours_and_player_level_scales_from_xp_and_damage() {
        // Strength / Education / Endurance first upgrade takes 2 hours = 7200s
        assertEquals(7200L, PerkTrainingManager.durationSecondsForLevel(1L))
        assertEquals("02:00:00", PerkTrainingManager.formatCountdown(7_200_000L))
        assertTrue(PerkTrainingManager.durationSecondsForLevel(2L) > 7200L)

        // Player level scales from both damage and XP with no wait timer
        val lvlStart = RivalRegionsRepository.computePlayerLevel(xp = 0L, workExp = 0L, totalDamage = 0L)
        val lvlDmg = RivalRegionsRepository.computePlayerLevel(xp = 0L, workExp = 0L, totalDamage = 250_000L)
        val lvlExp = RivalRegionsRepository.computePlayerLevel(xp = 3_000L, workExp = 200L, totalDamage = 0L)
        assertEquals(1L, lvlStart)
        assertTrue(lvlDmg > 1L)
        assertTrue(lvlExp > 1L)

        // Strength drives primary damage; Endurance + Education boost damage & energy regain
        val baseProfile = CommanderProfile(strength = 10L, education = 10L, endurance = 100L)
        val highStr = baseProfile.copy(strength = 40L)
        val highEduEnd = baseProfile.copy(education = 35L, endurance = 200L)
        assertTrue(
            RivalRegionsRepository.computeAlphaDamage(highStr, WeaponType.TANK) >
                RivalRegionsRepository.computeAlphaDamage(baseProfile, WeaponType.TANK)
        )
        assertTrue(
            RivalRegionsRepository.computeAlphaDamage(highEduEnd, null) >
                RivalRegionsRepository.computeAlphaDamage(baseProfile, null)
        )
        assertTrue(
            RivalRegionsRepository.computeEnergyRegenPerCycle(highEduEnd.endurance, highEduEnd.education, 1L) >
                RivalRegionsRepository.computeEnergyRegenPerCycle(baseProfile.endurance, baseProfile.education, 1L)
        )

        assertTrue(com.example.data.PartyWageManager.BOT_PARTY_MAX_WAGE_UNDER_10B < 10_000_000_000L)
        assertEquals(40L, com.example.data.PartyWageManager.MIN_LEVEL_FOR_PARTY_WAGE)
        assertEquals(60L, com.example.data.PartyWageManager.MIN_LEVEL_FOR_ELECTION_VOTE)
    }
}
