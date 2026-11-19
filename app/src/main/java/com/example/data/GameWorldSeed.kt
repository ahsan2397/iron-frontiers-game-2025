package com.example.data

import com.example.model.RegionMapNode
import com.google.firebase.firestore.FieldValue

object GameWorldSeed {

    const val OWNER_EMAIL = "buttahsanjaved@gmail.com"
    const val CURRENT_WORLD_VERSION = 3L

    // Exact 40 Real Countries / Regions World Map
    // Gold: 50..250, Oil: 50..250, Ore: 50..200, Wood: 50..250
    val mapNodes: List<RegionMapNode> = listOf(
        RegionMapNode("punjab", "Punjab - Pakistan", "Punjab", "Pakistan", 0.635f, 0.435f, 250L, 210L, 185L, 190L, 127_688_000L, listOf("islamabad", "lahore", "sindh", "kpk", "delhi")),
        RegionMapNode("sindh", "Sindh - Pakistan", "Sindh", "Pakistan", 0.618f, 0.495f, 185L, 225L, 160L, 140L, 55_696_000L, listOf("punjab", "karachi", "balochistan", "mumbai")),
        RegionMapNode("balochistan", "Balochistan - Pakistan", "Balochistan", "Pakistan", 0.592f, 0.480f, 240L, 235L, 200L, 110L, 14_894_000L, listOf("sindh", "kpk", "tehran", "dubai")),
        RegionMapNode("kpk", "KPK - Pakistan", "KPK", "Pakistan", 0.620f, 0.395f, 175L, 145L, 190L, 210L, 40_856_000L, listOf("punjab", "islamabad", "balochistan", "kabul")),
        RegionMapNode("islamabad", "Islamabad - Pakistan", "Islamabad", "Pakistan", 0.642f, 0.402f, 215L, 160L, 150L, 180L, 2_400_000L, listOf("punjab", "kpk", "lahore")),
        RegionMapNode("lahore", "Lahore - Pakistan", "Lahore", "Pakistan", 0.652f, 0.445f, 220L, 170L, 155L, 175L, 13_900_000L, listOf("punjab", "islamabad", "delhi")),
        RegionMapNode("karachi", "Karachi - Pakistan", "Karachi", "Pakistan", 0.608f, 0.525f, 205L, 240L, 145L, 120L, 20_382_000L, listOf("sindh", "dubai", "mumbai")),
        RegionMapNode("delhi", "Delhi - India", "Delhi", "India", 0.678f, 0.460f, 195L, 155L, 170L, 160L, 32_941_000L, listOf("lahore", "punjab", "mumbai", "kolkata")),
        RegionMapNode("mumbai", "Mumbai - India", "Mumbai", "India", 0.665f, 0.545f, 230L, 190L, 165L, 150L, 21_297_000L, listOf("delhi", "karachi", "kolkata", "dubai")),
        RegionMapNode("kolkata", "Kolkata - India", "Kolkata", "India", 0.715f, 0.510f, 165L, 140L, 185L, 185L, 15_333_000L, listOf("delhi", "mumbai", "dhaka")),
        RegionMapNode("istanbul", "Istanbul - Turkey", "Istanbul", "Turkey", 0.518f, 0.320f, 210L, 150L, 160L, 175L, 15_907_000L, listOf("ankara", "milan", "cairo")),
        RegionMapNode("ankara", "Ankara - Turkey", "Ankara", "Turkey", 0.542f, 0.340f, 175L, 145L, 180L, 165L, 5_782_000L, listOf("istanbul", "tehran", "riyadh")),
        RegionMapNode("berlin", "Berlin - Germany", "Berlin", "Germany", 0.485f, 0.220f, 225L, 120L, 175L, 210L, 3_755_000L, listOf("munich", "paris", "st_petersburg")),
        RegionMapNode("munich", "Munich - Germany", "Munich", "Germany", 0.480f, 0.260f, 215L, 110L, 165L, 220L, 1_512_000L, listOf("berlin", "milan", "lyon")),
        RegionMapNode("london", "London - UK", "London", "UK", 0.425f, 0.215f, 240L, 185L, 140L, 155L, 9_648_000L, listOf("manchester", "paris", "new_york")),
        RegionMapNode("manchester", "Manchester - UK", "Manchester", "UK", 0.415f, 0.180f, 180L, 195L, 155L, 145L, 2_791_000L, listOf("london", "toronto")),
        RegionMapNode("california", "California - USA", "California", "USA", 0.130f, 0.390f, 245L, 215L, 165L, 230L, 38_965_000L, listOf("texas", "tokyo")),
        RegionMapNode("texas", "Texas - USA", "Texas", "USA", 0.185f, 0.435f, 190L, 250L, 170L, 180L, 30_503_000L, listOf("california", "new_york")),
        RegionMapNode("new_york", "New York - USA", "New York", "USA", 0.245f, 0.335f, 250L, 165L, 150L, 195L, 19_571_000L, listOf("texas", "toronto", "london")),
        RegionMapNode("moscow", "Moscow - Russia", "Moscow", "Russia", 0.565f, 0.190f, 220L, 240L, 195L, 240L, 13_104_000L, listOf("st_petersburg", "ankara", "beijing")),
        RegionMapNode("st_petersburg", "St Petersburg - Russia", "St Petersburg", "Russia", 0.545f, 0.140f, 195L, 210L, 180L, 250L, 5_600_000L, listOf("moscow", "berlin")),
        RegionMapNode("beijing", "Beijing - China", "Beijing", "China", 0.775f, 0.330f, 235L, 185L, 195L, 190L, 21_843_000L, listOf("shanghai", "moscow", "tokyo")),
        RegionMapNode("shanghai", "Shanghai - China", "Shanghai", "China", 0.805f, 0.410f, 245L, 175L, 180L, 185L, 24_870_000L, listOf("beijing", "osaka", "bangkok")),
        RegionMapNode("tokyo", "Tokyo - Japan", "Tokyo", "Japan", 0.870f, 0.370f, 240L, 115L, 135L, 205L, 14_094_000L, listOf("osaka", "beijing", "california")),
        RegionMapNode("osaka", "Osaka - Japan", "Osaka", "Japan", 0.848f, 0.390f, 200L, 105L, 125L, 195L, 8_839_000L, listOf("tokyo", "shanghai")),
        RegionMapNode("paris", "Paris - France", "Paris", "France", 0.448f, 0.260f, 230L, 110L, 145L, 200L, 11_208_000L, listOf("lyon", "london", "berlin", "madrid")),
        RegionMapNode("lyon", "Lyon - France", "Lyon", "France", 0.455f, 0.292f, 185L, 100L, 140L, 215L, 2_323_000L, listOf("paris", "munich", "milan", "barcelona")),
        RegionMapNode("rome", "Rome - Italy", "Rome", "Italy", 0.482f, 0.340f, 205L, 115L, 135L, 165L, 4_316_000L, listOf("milan", "cairo")),
        RegionMapNode("milan", "Milan - Italy", "Milan", "Italy", 0.472f, 0.302f, 220L, 125L, 150L, 180L, 3_248_000L, listOf("rome", "munich", "lyon", "istanbul")),
        RegionMapNode("madrid", "Madrid - Spain", "Madrid", "Spain", 0.412f, 0.340f, 190L, 105L, 155L, 175L, 6_751_000L, listOf("barcelona", "paris")),
        RegionMapNode("barcelona", "Barcelona - Spain", "Barcelona", "Spain", 0.432f, 0.330f, 195L, 115L, 140L, 165L, 5_687_000L, listOf("madrid", "lyon")),
        RegionMapNode("cairo", "Cairo - Egypt", "Cairo", "Egypt", 0.530f, 0.445f, 170L, 195L, 145L, 90L, 22_183_000L, listOf("istanbul", "rome", "riyadh")),
        RegionMapNode("dubai", "Dubai - UAE", "Dubai", "UAE", 0.578f, 0.490f, 250L, 245L, 110L, 65L, 3_604_000L, listOf("riyadh", "balochistan", "karachi", "mumbai")),
        RegionMapNode("riyadh", "Riyadh - Saudi Arabia", "Riyadh", "Saudi Arabia", 0.555f, 0.480f, 210L, 250L, 130L, 55L, 7_676_000L, listOf("dubai", "cairo", "ankara", "tehran")),
        RegionMapNode("tehran", "Tehran - Iran", "Tehran", "Iran", 0.575f, 0.390f, 180L, 235L, 180L, 110L, 9_469_000L, listOf("balochistan", "kabul", "riyadh", "ankara")),
        RegionMapNode("kabul", "Kabul - Afghanistan", "Kabul", "Afghanistan", 0.610f, 0.380f, 140L, 95L, 190L, 120L, 4_601_000L, listOf("kpk", "tehran")),
        RegionMapNode("dhaka", "Dhaka - Bangladesh", "Dhaka", "Bangladesh", 0.732f, 0.490f, 145L, 130L, 115L, 200L, 23_210_000L, listOf("kolkata", "bangkok")),
        RegionMapNode("bangkok", "Bangkok - Thailand", "Bangkok", "Thailand", 0.770f, 0.560f, 185L, 150L, 135L, 225L, 11_070_000L, listOf("dhaka", "shanghai", "sydney")),
        RegionMapNode("sydney", "Sydney - Australia", "Sydney", "Australia", 0.885f, 0.800f, 235L, 165L, 200L, 215L, 5_312_000L, listOf("bangkok", "tokyo")),
        RegionMapNode("toronto", "Toronto - Canada", "Toronto", "Canada", 0.230f, 0.285f, 215L, 205L, 195L, 250L, 6_372_000L, listOf("new_york", "manchester"))
    )

    fun initialProfileMap(
        uid: String,
        commanderName: String,
        stateName: String,
        regionId: String,
        partyId: String,
        partyName: String,
        isOwner: Boolean,
        setupCompleted: Boolean
    ): Map<String, Any> {
        val cleanName = commanderName.trim().take(60).ifBlank { if (isOwner) "State Owner" else "Player One" }
        val cleanState = stateName.trim().take(60).ifBlank { "Pakistan" }
        val cleanParty = partyName.trim().take(60).ifBlank { "National Reform Party" }
        val cleanRegion = mapNodes.firstOrNull { it.id == regionId }?.id ?: "punjab"

        return mapOf(
            "userId" to uid,
            "commanderName" to cleanName,
            "stateName" to cleanState,
            "setupCompleted" to setupCompleted,
            "worldVersion" to CURRENT_WORLD_VERSION,
            "kingMode" to isOwner,
            "level" to 1L,
            "xp" to 0L,
            "workExp" to 0L,
            "medals" to 0L,
            "premiumActive" to isOwner,
            "strength" to 10L,
            "strLevel" to 1L,
            "education" to 10L,
            "eduLevel" to 1L,
            "endurance" to 100L,
            "endLevel" to 1L,
            "energy" to 100L,
            "maxEnergy" to 100L,
            "tickets" to if (isOwner) 9999L else 5L,
            "currentRegionId" to cleanRegion,
            "birthRegionId" to cleanRegion,
            "residencies" to listOf(cleanRegion),
            "partyId" to partyId,
            "partyName" to cleanParty,
            "parliamentSeats" to 60L,
            "money" to if (isOwner) 999_000_000_000L else 1_000_000_000L,
            "gold" to if (isOwner) 9_999_999L else 100L,
            "wood" to if (isOwner) 9_999_999L else 100L,
            "oil" to if (isOwner) 9_999_999L else 50L,
            "ore" to if (isOwner) 9_999_999L else 50L,
            "uranium" to if (isOwner) 9_999_999L else 0L,
            "diamonds" to if (isOwner) 9_999_999L else 0L,
            "tanks" to if (isOwner) 9_999_999L else 0L,
            "bombers" to if (isOwner) 999_999L else 0L,
            "missiles" to if (isOwner) 999_999L else 0L,
            "energyDrinks" to if (isOwner) 999_999L else 10_000L,
            "explorationUsed" to 0L,
            "explorationLimit" to 1500L,
            "factoryLevel" to 1L,
            "factoryPayMoney" to 1_000_000L,
            "factoryPayGold" to 10L,
            "factoryWorkCount" to 0L,
            "questProgressMask" to 0L,
            "autoWorkActive" to false,
            "totalDamage" to 0L,
            "warsWon" to 0L,
            "lawsPassed" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun initialRegions(
        uid: String,
        selectedStartRegionId: String = "punjab",
        playerStateName: String = "Pakistan"
    ): Map<String, Map<String, Any>> {
        val cleanState = playerStateName.trim().take(60).ifBlank { "Pakistan" }
        return mapNodes.associate { node ->
            val controller = if (node.id == selectedStartRegionId) cleanState else node.country
            val conquerable = node.id != "punjab"
            val hp = if (node.id == "punjab") 1_000_000L else 250_000L
            node.id to mapOf(
                "userId" to uid,
                "name" to node.fullName,
                "country" to node.country,
                "controller" to controller,
                "parliamentTitle" to "${node.shortName} Parl - Real Country - Clean",
                "conquerable" to conquerable,
                "autonomy" to false,
                "regionLevel" to 10L,
                "goldIndex" to node.gold.coerceIn(50L, 250L),
                "oilIndex" to node.oil.coerceIn(50L, 250L),
                "oreIndex" to node.ore.coerceIn(50L, 200L),
                "woodIndex" to node.wood.coerceIn(50L, 250L),
                "uraniumIndex" to 80L,
                "diamondIndex" to 120L,
                "defenseIndex" to 50L,
                "hospitalLevel" to 1L,
                "militaryBaseLevel" to 1L,
                "schoolLevel" to 1L,
                "powerPlantLevel" to 1L,
                "housingLevel" to 1L,
                "population" to node.population,
                "taxRate" to 10L,
                "hp" to hp,
                "maxHp" to hp,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }
    }

    fun initialParties(uid: String, playerPartyName: String = "National Reform Party"): Map<String, Map<String, Any>> {
        val cleanParty = playerPartyName.trim().take(60).ifBlank { "National Reform Party" }
        val tag = cleanParty.split(" ").filter { it.isNotBlank() }.take(3).joinToString("") { it.first().uppercase() }.padEnd(2, 'P').take(5)
        return mapOf(
            "party_player" to mapOf(
                "userId" to uid,
                "name" to cleanParty,
                "tag" to tag,
                "colorHex" to "#16A34A",
                "seats" to 24L,
                "membersCount" to 32L,
                "maxMembers" to 60L,
                "wedgeScore" to 40L,
                "ideology" to "Clean Sovereign Growth",
                "treasuryGold" to 500L,
                "userMember" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            "party_global" to mapOf(
                "userId" to uid,
                "name" to "Global Civic League",
                "tag" to "GCL",
                "colorHex" to "#F59E0B",
                "seats" to 14L,
                "membersCount" to 30L,
                "maxMembers" to 60L,
                "wedgeScore" to 34L,
                "ideology" to "Free Trade & Diplomacy",
                "treasuryGold" to 400L,
                "userMember" to false,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            "party_workers" to mapOf(
                "userId" to uid,
                "name" to "Industrial Workers Union",
                "tag" to "IWU",
                "colorHex" to "#2563EB",
                "seats" to 12L,
                "membersCount" to 30L,
                "maxMembers" to 60L,
                "wedgeScore" to 31L,
                "ideology" to "Factory & Resource Output",
                "treasuryGold" to 350L,
                "userMember" to false,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            "party_defense" to mapOf(
                "userId" to uid,
                "name" to "World Defense Coalition",
                "tag" to "WDC",
                "colorHex" to "#DC2626",
                "seats" to 10L,
                "membersCount" to 30L,
                "maxMembers" to 60L,
                "wedgeScore" to 28L,
                "ideology" to "Border Security & Infrastructure",
                "treasuryGold" to 300L,
                "userMember" to false,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
    }

    fun initialWars(uid: String, selectedStartRegionId: String = "punjab"): Map<String, Map<String, Any>> {
        val target = mapNodes.firstOrNull { it.id != selectedStartRegionId && it.id != "punjab" && it.country != "Pakistan" }
            ?: mapNodes.first { it.id == "kabul" }
        val startNode = mapNodes.firstOrNull { it.id == selectedStartRegionId } ?: mapNodes.first()
        return mapOf(
            "war_clean_border" to mapOf(
                "userId" to uid,
                "targetRegionId" to target.id,
                "targetRegionName" to target.fullName,
                "warType" to "WAR",
                "attackerName" to "Expeditionary Force",
                "defenderName" to target.country,
                "attackerDamage" to 0L,
                "defenderDamage" to 0L,
                "membersCount" to 1L,
                "status" to "ACTIVE",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            "war_training_default" to mapOf(
                "userId" to uid,
                "targetRegionId" to startNode.id,
                "targetRegionName" to startNode.fullName,
                "warType" to "TRAINING",
                "attackerName" to "Attack troops",
                "defenderName" to "Defense troops",
                "attackerDamage" to 0L,
                "defenderDamage" to 0L,
                "membersCount" to 1L,
                "status" to "ACTIVE",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
    }

    fun initialLaws(uid: String, startRegionShort: String = "Punjab"): Map<String, Map<String, Any>> = mapOf(
        "law_punjab_parl" to mapOf(
            "userId" to uid,
            "title" to "$startRegionShort Parl - Real Country - Clean Development Act",
            "category" to "Buildings",
            "proposerParty" to "NRP",
            "targetSeatCount" to 0L,
            "yesVotes" to 22L,
            "noVotes" to 10L,
            "userVoted" to "NONE",
            "status" to "VOTING",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ),
        "law_seat_reform" to mapOf(
            "userId" to uid,
            "title" to "Reform Parliament Assembly to 80 Seats",
            "category" to "Constitution",
            "proposerParty" to "GCL",
            "targetSeatCount" to 80L,
            "yesVotes" to 18L,
            "noVotes" to 12L,
            "userVoted" to "NONE",
            "status" to "VOTING",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    )

    fun initialArticles(uid: String): Map<String, Map<String, Any>> = mapOf(
        "art_world_launch" to mapOf(
            "userId" to uid,
            "authorName" to "Citizen_01",
            "newspaper" to "World Gazette",
            "title" to "Clean New World: 40 Real Countries Live",
            "body" to "All 40 real-world regions from Punjab - Pakistan to Toronto - Canada are open with fresh Gold, Oil, Ore, and Wood reserves.",
            "votes" to 10L,
            "botGenerated" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
    )

    fun initialChats(uid: String, commanderName: String, regionName: String): Map<String, Map<String, Any>> = mapOf(
        "chat_welcome_1" to mapOf(
            "userId" to uid,
            "senderName" to "Citizen_01",
            "senderRole" to "Citizen",
            "message" to "Hey @Trader_02, did you see $commanderName just entered $regionName? Think they'll build a Power Plant first?",
            "botSender" to true,
            "createdAt" to FieldValue.serverTimestamp()
        ),
        "chat_welcome_2" to mapOf(
            "userId" to uid,
            "senderName" to "Trader_02",
            "senderRole" to "Merchant",
            "message" to "@Citizen_01 For sure! Plus $commanderName has 10.000 Energy Drinks and Storage Market (10E = 1G) ready. Hey $commanderName, ready to work or hit Military Training?",
            "botSender" to true,
            "createdAt" to FieldValue.serverTimestamp()
        ),
        "chat_welcome_3" to mapOf(
            "userId" to uid,
            "senderName" to "Senator_04",
            "senderRole" to "Senator",
            "message" to "@Trader_02 Good point! @$commanderName tell us in chat when you join Military Training or upgrade $regionName!",
            "botSender" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )
    )
}
