package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class GameTab(val label: String) {
    HOME("Home"),
    ARTICLES("Articles"),
    MAPS("Maps"),
    STORAGE("Storage"),
    WORK("Work"),
    WARS("Wars"),
    PROFILE("Profile")
}

enum class SubPanel {
    NONE,
    PARLIAMENT,
    PARTY,
    GLOBAL_CHAT
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class WarType {
    WAR,
    REVOLUTION,
    COUP,
    TRAINING;

    companion object {
        fun fromRaw(raw: String): WarType =
            entries.firstOrNull { it.name == raw } ?: WAR
    }
}

enum class WarStatus {
    ACTIVE,
    VICTORY,
    REPELLED;

    companion object {
        fun fromRaw(raw: String): WarStatus =
            entries.firstOrNull { it.name == raw } ?: ACTIVE
    }
}

enum class LawStatus {
    VOTING,
    PASSED,
    REJECTED;

    companion object {
        fun fromRaw(raw: String): LawStatus =
            entries.firstOrNull { it.name == raw } ?: VOTING
    }
}

enum class UserVote {
    NONE,
    YES,
    NO;

    companion object {
        fun fromRaw(raw: String): UserVote =
            entries.firstOrNull { it.name == raw } ?: NONE
    }
}

enum class StateBuildingType(
    val fieldName: String,
    val title: String,
    val effectSummary: String,
    val baseMoneyCost: Long,
    val baseWoodCost: Long,
    val baseOreCost: Long
) {
    HOSPITAL("hospitalLevel", "Hospital", "+10 Max Energy & Endurance", 5_000_000L, 40L, 25L),
    MILITARY_BASE("militaryBaseLevel", "Military Base", "+15 Region Defense & HP", 8_000_000L, 50L, 60L),
    SCHOOL("schoolLevel", "State Academy", "+10% Education & WorkExp", 6_000_000L, 45L, 30L),
    POWER_PLANT("powerPlantLevel", "Power Plant", "+15% Factory Output", 7_500_000L, 35L, 55L),
    HOUSING("housingLevel", "Housing Fund", "+50K Population & Wood Index", 4_500_000L, 70L, 30L)
}

@Keep
@IgnoreExtraProperties
data class CommanderProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val commanderName: String = "Commander",
    val stateName: String = "Pakistan",
    val setupCompleted: Boolean = false,
    val worldVersion: Long = 3L,
    val kingMode: Boolean = false,
    val level: Long = 1L,
    val xp: Long = 0L,
    val workExp: Long = 0L,
    val medals: Long = 0L,
    val premiumActive: Boolean = false,
    val strength: Long = 10L,
    val strLevel: Long = 1L,
    val education: Long = 10L,
    val eduLevel: Long = 1L,
    val endurance: Long = 100L,
    val endLevel: Long = 1L,
    val energy: Long = 100L,
    val maxEnergy: Long = 100L,
    val tickets: Long = 5L,
    val currentRegionId: String = "punjab",
    val birthRegionId: String = "punjab",
    val residencies: List<String> = listOf("punjab"),
    val partyId: String = "party_player",
    val partyName: String = "National Reform Party",
    val parliamentSeats: Long = 60L,
    val money: Long = 1_000_000_000L,
    val gold: Long = 100L,
    val wood: Long = 100L,
    val oil: Long = 50L,
    val ore: Long = 50L,
    val uranium: Long = 0L,
    val diamonds: Long = 0L,
    val tanks: Long = 0L,
    val bombers: Long = 0L,
    val missiles: Long = 0L,
    val energyDrinks: Long = 10_000L,
    val explorationUsed: Long = 0L,
    val explorationLimit: Long = 1500L,
    val factoryLevel: Long = 1L,
    val factoryPayMoney: Long = 1_000_000L,
    val factoryPayGold: Long = 10L,
    val factoryWorkCount: Long = 0L,
    val questProgressMask: Long = 0L,
    val autoWorkActive: Boolean = false,
    val totalDamage: Long = 0L,
    val warsWon: Long = 0L,
    val lawsPassed: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun machineCount(weapon: WeaponType): Long = when (weapon) {
        WeaponType.TANK -> tanks
        WeaponType.BOMBER -> bombers
        WeaponType.MISSILE -> missiles
    }
}

@Keep
@IgnoreExtraProperties
data class RegionState(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val country: String = "",
    val controller: String = "",
    val parliamentTitle: String = "",
    val conquerable: Boolean = true,
    val autonomy: Boolean = false,
    val regionLevel: Long = 10L,
    val goldIndex: Long = 150L,
    val oilIndex: Long = 150L,
    val oreIndex: Long = 120L,
    val woodIndex: Long = 140L,
    val uraniumIndex: Long = 60L,
    val diamondIndex: Long = 100L,
    val defenseIndex: Long = 40L,
    val hospitalLevel: Long = 1L,
    val militaryBaseLevel: Long = 1L,
    val schoolLevel: Long = 1L,
    val powerPlantLevel: Long = 1L,
    val housingLevel: Long = 1L,
    val population: Long = 1_000_000L,
    val taxRate: Long = 10L,
    val hp: Long = 200_000L,
    val maxHp: Long = 200_000L,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isSovereignProtected: Boolean
        get() = id == "punjab" || !conquerable

    val isCapitalStar: Boolean
        get() = id == "punjab"

    fun buildingLevel(type: StateBuildingType): Long = when (type) {
        StateBuildingType.HOSPITAL -> hospitalLevel
        StateBuildingType.MILITARY_BASE -> militaryBaseLevel
        StateBuildingType.SCHOOL -> schoolLevel
        StateBuildingType.POWER_PLANT -> powerPlantLevel
        StateBuildingType.HOUSING -> housingLevel
    }
}

@Keep
@IgnoreExtraProperties
data class PartyState(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val tag: String = "",
    val colorHex: String = "#16A34A",
    val seats: Long = 0L,
    val membersCount: Long = 30L,
    val maxMembers: Long = 60L,
    val wedgeScore: Long = 30L,
    val ideology: String = "",
    val treasuryGold: Long = 0L,
    val userMember: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WarFront(
    @DocumentId val id: String = "",
    val userId: String = "",
    val targetRegionId: String = "",
    val targetRegionName: String = "",
    val warType: String = WarType.WAR.name,
    val attackerName: String = "",
    val defenderName: String = "",
    val attackerDamage: Long = 0L,
    val defenderDamage: Long = 0L,
    val membersCount: Long = 1L,
    val status: String = WarStatus.ACTIVE.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ParliamentLaw(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "Economy",
    val proposerParty: String = "NRP",
    val targetSeatCount: Long = 0L,
    val yesVotes: Long = 0L,
    val noVotes: Long = 0L,
    val userVoted: String = UserVote.NONE.name,
    val status: String = LawStatus.VOTING.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ArticlePost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val newspaper: String = "",
    val title: String = "",
    val body: String = "",
    val votes: Long = 0L,
    val botGenerated: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val senderName: String = "",
    val senderRole: String = "Citizen",
    val message: String = "",
    val botSender: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GlobalChatMessage(
    @DocumentId val id: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val stateName: String = "",
    val regionName: String = "",
    val isOwner: Boolean = false,
    val message: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class RegionMapNode(
    val id: String,
    val fullName: String,
    val shortName: String,
    val country: String,
    val xFraction: Float,
    val yFraction: Float,
    val gold: Long,
    val oil: Long,
    val ore: Long,
    val wood: Long,
    val population: Long,
    val neighbors: List<String>
)

enum class FactoryType(
    val title: String,
    val resourceName: String,
    val baseYield: Long,
    val cashPerEnergy: Long
) {
    GOLD_MINE("National Gold Works", "Gold", 4L, 2_500_000L),
    OIL_RIG("State Oil Refinery", "Oil", 35L, 1_900_000L),
    ORE_QUARRY("Continental Ore Quarry", "Ore", 32L, 1_750_000L),
    WOOD_MILL("State Timber Mill", "Wood", 36L, 1_550_000L),
    URANIUM_PLANT("Isotope Enrichment Plant", "Uranium", 15L, 2_300_000L),
    DIAMOND_WORKS("Royal Gem Works", "Diamonds", 10L, 2_100_000L)
}

enum class WeaponType(
    val title: String,
    val category: String,
    val profileField: String,
    val damage: Long,
    val oilCost: Long,
    val oreCost: Long,
    val woodCost: Long,
    val uraniumCost: Long,
    val cashCost: Long
) {
    TANK("Main Battle Tank", "Ground Armor", "tanks", 3_500L, 20L, 30L, 10L, 0L, 250_000L),
    BOMBER("Strategic Bomber", "Heavy Air", "bombers", 9_500L, 60L, 50L, 20L, 10L, 850_000L),
    MISSILE("Ballistic Missile", "Strategic Strike", "missiles", 28_000L, 100L, 80L, 30L, 30L, 2_400_000L)
}
