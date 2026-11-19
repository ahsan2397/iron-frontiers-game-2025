package com.example.data

import android.content.Context
import com.example.R
import com.example.model.ArticlePost
import com.example.model.ChatMessage
import com.example.model.CommanderProfile
import com.example.model.FactoryType
import com.example.model.GlobalChatMessage
import com.example.model.LawStatus
import com.example.model.ParliamentLaw
import com.example.model.PartyState
import com.example.model.RegionState
import com.example.model.StateBuildingType
import com.example.model.UserVote
import com.example.model.WarFront
import com.example.model.WarStatus
import com.example.model.WarType
import com.example.model.WeaponType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

class RivalRegionsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    fun isTrueOwnerAccount(): Boolean =
        auth.currentUser?.email.equals(GameWorldSeed.OWNER_EMAIL, ignoreCase = true)

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    fun observeProfile(userId: String): Flow<CommanderProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            userDoc(userId).snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(CommanderProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    handleFlowError(error, OperationType.GET, path, userId)
                    throw error
                }
        )
    }

    fun observeRegions(userId: String): Flow<List<RegionState>> = flow {
        val path = "users/$userId/regions"
        emitAll(
            userDoc(userId).collection("regions").snapshots()
                .map { it.toObjects(RegionState::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    fun observeParties(userId: String): Flow<List<PartyState>> = flow {
        val path = "users/$userId/parties"
        emitAll(
            userDoc(userId).collection("parties").snapshots()
                .map { snap ->
                    snap.toObjects(PartyState::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.seats }
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    fun observeWars(userId: String): Flow<List<WarFront>> = flow {
        val path = "users/$userId/wars"
        emitAll(
            userDoc(userId).collection("wars").snapshots()
                .map { snap ->
                    snap.toObjects(WarFront::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.status == WarStatus.ACTIVE.name }
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    fun observeLaws(userId: String): Flow<List<ParliamentLaw>> = flow {
        val path = "users/$userId/laws"
        emitAll(
            userDoc(userId).collection("laws").snapshots()
                .map { snap ->
                    snap.toObjects(ParliamentLaw::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.status == LawStatus.VOTING.name }
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    fun observeArticles(userId: String): Flow<List<ArticlePost>> = flow {
        val path = "users/$userId/articles"
        emitAll(
            userDoc(userId).collection("articles").snapshots()
                .map { snap ->
                    snap.toObjects(ArticlePost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    fun observeChats(userId: String): Flow<List<ChatMessage>> = flow {
        val path = "users/$userId/chats"
        emitAll(
            userDoc(userId).collection("chats").snapshots()
                .map { snap ->
                    snap.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    fun observeGlobalChats(userId: String): Flow<List<GlobalChatMessage>> = flow {
        val path = "global_chat"
        emitAll(
            db.collection("global_chat")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(80)
                .snapshots()
                .map { snap ->
                    snap.toObjects(GlobalChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    handleFlowError(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    private fun handleFlowError(error: Throwable, op: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }

    suspend fun ensureWorldSeeded(displayName: String): Result<Unit> =
        safeWrite(OperationType.WRITE, "users/${auth.currentUser?.uid ?: "unauthenticated"}") {
            val uid = requireUserId()
            val isOwner = isTrueOwnerAccount()
            val profileSnap = userDoc(uid).get().await()

            if (!profileSnap.exists()) {
                // Seed initial clean placeholder profile with setupCompleted = false so New Game screen opens immediately
                userDoc(uid).set(
                    GameWorldSeed.initialProfileMap(
                        uid = uid,
                        commanderName = displayName.ifBlank { "Player One" },
                        stateName = "Pakistan",
                        regionId = "punjab",
                        partyId = "party_player",
                        partyName = "National Reform Party",
                        isOwner = isOwner,
                        setupCompleted = false
                    )
                ).await()
                return@safeWrite
            }

            val version = profileSnap.getLong("worldVersion") ?: 1L
            if (version < 2L) {
                // Upgrade v1 doc cleanly to v3 setup state so old legacy names/regions are wiped on Start New Game
                userDoc(uid).set(
                    GameWorldSeed.initialProfileMap(
                        uid = uid,
                        commanderName = displayName.ifBlank { "Player One" },
                        stateName = "Pakistan",
                        regionId = "punjab",
                        partyId = "party_player",
                        partyName = "National Reform Party",
                        isOwner = isOwner,
                        setupCompleted = false
                    )
                ).await()
            } else {
                val updates = mutableMapOf<String, Any>()
                val storedOwner = profileSnap.getBoolean("kingMode") ?: false
                if (storedOwner != isOwner) {
                    updates["kingMode"] = isOwner
                }
                if (version < GameWorldSeed.CURRENT_WORLD_VERSION) {
                    updates["worldVersion"] = GameWorldSeed.CURRENT_WORLD_VERSION
                }
                if (!profileSnap.contains("medals")) updates["medals"] = 0L
                if (!profileSnap.contains("premiumActive")) updates["premiumActive"] = isOwner
                if (!profileSnap.contains("factoryPayMoney")) updates["factoryPayMoney"] = 1_000_000L
                if (!profileSnap.contains("factoryPayGold")) updates["factoryPayGold"] = 10L
                if (!profileSnap.contains("factoryWorkCount")) updates["factoryWorkCount"] = 0L
                if (!profileSnap.contains("questProgressMask")) updates["questProgressMask"] = 0L
                val curDrinks = profileSnap.getLong("energyDrinks") ?: 0L
                if (version < GameWorldSeed.CURRENT_WORLD_VERSION && !isOwner && curDrinks < 10_000L) {
                    updates["energyDrinks"] = 10_000L
                }
                if (updates.isNotEmpty()) {
                    updates["updatedAt"] = FieldValue.serverTimestamp()
                    userDoc(uid).update(updates).await()
                }
            }
        }

    suspend fun startNewCleanGame(
        commanderName: String,
        stateName: String,
        regionId: String,
        partyName: String
    ): Result<Unit> = safeWrite(OperationType.WRITE, "users/${auth.currentUser?.uid ?: "unauthenticated"}") {
        val uid = requireUserId()
        val isOwner = isTrueOwnerAccount()
        val cleanName = commanderName.trim().take(60).ifBlank { if (isOwner) "State Owner" else "Player One" }
        val cleanState = stateName.trim().take(60).ifBlank { "Pakistan" }
        val cleanParty = partyName.trim().take(60).ifBlank { "National Reform Party" }
        val chosenNode = GameWorldSeed.mapNodes.firstOrNull { it.id == regionId } ?: GameWorldSeed.mapNodes.first()

        // Wipe any old subcollection docs cleanly before seeding 40 real countries
        val collections = listOf("regions", "parties", "wars", "laws", "articles", "chats")
        for (colName in collections) {
            val existingDocs = userDoc(uid).collection(colName).get().await()
            if (!existingDocs.isEmpty) {
                val chunks = existingDocs.documents.chunked(400)
                for (chunk in chunks) {
                    val delBatch = db.batch()
                    chunk.forEach { delBatch.delete(it.reference) }
                    delBatch.commit().await()
                }
            }
        }

        // Write clean profile
        userDoc(uid).set(
            GameWorldSeed.initialProfileMap(
                uid = uid,
                commanderName = cleanName,
                stateName = cleanState,
                regionId = chosenNode.id,
                partyId = "party_player",
                partyName = cleanParty,
                isOwner = isOwner,
                setupCompleted = true
            )
        ).await()

        // Write all 40 real-country regions + clean starter world in atomic batches
        val seedOps = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Map<String, Any>>>()
        GameWorldSeed.initialRegions(uid, chosenNode.id, cleanState).forEach { (id, payload) ->
            seedOps.add(userDoc(uid).collection("regions").document(id) to payload)
        }
        GameWorldSeed.initialParties(uid, cleanParty).forEach { (id, payload) ->
            seedOps.add(userDoc(uid).collection("parties").document(id) to payload)
        }
        GameWorldSeed.initialWars(uid, chosenNode.id).forEach { (id, payload) ->
            seedOps.add(userDoc(uid).collection("wars").document(id) to payload)
        }
        GameWorldSeed.initialLaws(uid, chosenNode.shortName).forEach { (id, payload) ->
            seedOps.add(userDoc(uid).collection("laws").document(id) to payload)
        }
        GameWorldSeed.initialArticles(uid).forEach { (id, payload) ->
            seedOps.add(userDoc(uid).collection("articles").document(id) to payload)
        }
        GameWorldSeed.initialChats(uid, cleanName, chosenNode.fullName).forEach { (id, payload) ->
            seedOps.add(userDoc(uid).collection("chats").document(id) to payload)
        }

        for (chunk in seedOps.chunked(400)) {
            val batch = db.batch()
            chunk.forEach { (ref, payload) -> batch.set(ref, payload) }
            batch.commit().await()
        }
    }

    suspend fun upgradePerk(
        profile: CommanderProfile,
        perk: String,
        speedUpGoldCost: Long = 0L
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            if (speedUpGoldCost > 0L && !profile.kingMode && profile.gold < speedUpGoldCost) {
                throw IllegalStateException("Need ${speedUpGoldCost}G to speed up training right now!")
            }
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (speedUpGoldCost > 0L && !profile.kingMode) {
                updates["gold"] = (profile.gold - speedUpGoldCost).coerceAtLeast(0L)
            }
            when (perk) {
                "strength" -> {
                    updates["strength"] = profile.strength + 1L
                    updates["strLevel"] = (profile.strLevel + 1L).coerceAtMost(1000L)
                }
                "education" -> {
                    updates["education"] = profile.education + 1L
                    updates["eduLevel"] = (profile.eduLevel + 1L).coerceAtMost(1000L)
                }
                "endurance" -> {
                    updates["endurance"] = profile.endurance + 1L
                    updates["endLevel"] = (profile.endLevel + 1L).coerceAtMost(1000L)
                    updates["maxEnergy"] = (profile.maxEnergy + 10L).coerceAtMost(1_000_000L)
                }
                else -> throw IllegalArgumentException("Unknown perk")
            }
            userDoc(uid).update(updates).await()
        }
    }

    suspend fun regenerateEnergyTick(profile: CommanderProfile, region: RegionState?): Result<Long> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        if (profile.kingMode || profile.energy >= profile.maxEnergy) {
            return Result.success(0L)
        }
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val regenAmount = computeEnergyRegenPerCycle(
                endurance = profile.endurance,
                education = profile.education,
                hospitalLevel = region?.hospitalLevel ?: 1L
            )
            val newEnergy = (profile.energy + regenAmount).coerceAtMost(profile.maxEnergy)
            val gained = (newEnergy - profile.energy).coerceAtLeast(0L)
            if (gained > 0L) {
                userDoc(uid).update(
                    mapOf(
                        "energy" to newEnergy,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            gained
        }
    }

    suspend fun upgradeStateBuilding(
        profile: CommanderProfile,
        region: RegionState,
        building: StateBuildingType
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/regions/${region.id}") {
            requireUserId()
            val isStateLeader = profile.kingMode || region.controller.equals(profile.stateName, ignoreCase = true)
            if (!isStateLeader) {
                throw IllegalStateException("Only State Leader of ${region.controller} (or Owner) can upgrade State Buildings!")
            }

            val currentLvl = region.buildingLevel(building)
            if (currentLvl >= 100L) {
                throw IllegalStateException("${building.title} is already at max level 100!")
            }
            val moneyCost = building.baseMoneyCost * currentLvl
            val woodCost = building.baseWoodCost * currentLvl
            val oreCost = building.baseOreCost * currentLvl

            if (!profile.kingMode) {
                if (profile.money < moneyCost || profile.wood < woodCost || profile.ore < oreCost) {
                    throw IllegalStateException(
                        "Need ${formatDots(moneyCost)} $, $woodCost Wood, $oreCost Ore to upgrade ${building.title}."
                    )
                }
            }

            val newLvl = (currentLvl + 1L).coerceAtMost(100L)
            val regionUpdates = mutableMapOf<String, Any>(
                building.fieldName to newLvl,
                "regionLevel" to (region.regionLevel + 1L).coerceAtMost(100L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            when (building) {
                StateBuildingType.MILITARY_BASE -> {
                    regionUpdates["defenseIndex"] = (region.defenseIndex + 15L).coerceAtMost(500L)
                    regionUpdates["maxHp"] = region.maxHp + 50_000L
                    regionUpdates["hp"] = region.hp + 50_000L
                }
                StateBuildingType.HOUSING -> {
                    regionUpdates["population"] = region.population + 50_000L
                    regionUpdates["woodIndex"] = (region.woodIndex + 5L).coerceAtMost(250L)
                }
                StateBuildingType.POWER_PLANT -> {
                    regionUpdates["oilIndex"] = (region.oilIndex + 5L).coerceAtMost(250L)
                    regionUpdates["oreIndex"] = (region.oreIndex + 5L).coerceAtMost(200L)
                }
                StateBuildingType.HOSPITAL, StateBuildingType.SCHOOL -> {
                    regionUpdates["defenseIndex"] = (region.defenseIndex + 5L).coerceAtMost(500L)
                }
            }

            val batch = db.batch()
            batch.update(userDoc(uid).collection("regions").document(region.id), regionUpdates)
            if (!profile.kingMode) {
                batch.update(
                    userDoc(uid),
                    mapOf(
                        "money" to (profile.money - moneyCost),
                        "wood" to (profile.wood - woodCost),
                        "ore" to (profile.ore - oreCost),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            "State Leader upgraded ${building.title} in ${region.name} to Level $newLvl!"
        }
    }

    suspend fun requestResidency(profile: CommanderProfile, region: RegionState): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            if (region.id == profile.birthRegionId || region.id in profile.residencies) {
                return@safeWrite
            }
            val fee = 25L
            if (!profile.kingMode && profile.gold < fee) {
                throw IllegalStateException("Need 25G residency permit fee for ${region.name}.")
            }
            val updatedResidencies = (profile.residencies + region.id).distinct().take(50)
            userDoc(uid).update(
                mapOf(
                    "residencies" to updatedResidencies,
                    "gold" to if (profile.kingMode) profile.gold else (profile.gold - fee),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun flyToRegion(profile: CommanderProfile, region: RegionState): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val isBirth = region.id == profile.birthRegionId
            val hasResidency = isBirth || region.id in profile.residencies
            if (!hasResidency && !profile.kingMode) {
                throw IllegalStateException("Residency required for ${region.name}! Request Residency first.")
            }

            val leaderTravelPrice = if (isBirth || profile.kingMode) 0L else (region.taxRate * 10_000L)
            val updates = mutableMapOf<String, Any>(
                "currentRegionId" to region.id,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!isBirth && !profile.kingMode) {
                if (profile.tickets <= 0L) {
                    throw IllegalStateException("Need 1 Ticket to fly to ${region.name}!")
                }
                val charterCost = 250_000L
                val totalCashFee = leaderTravelPrice
                if (profile.money < totalCashFee) {
                    throw IllegalStateException("Need ${formatDots(totalCashFee)} $ for State Leader travel price!")
                }
                updates["tickets"] = profile.tickets - 1L
                if (totalCashFee > 0L) {
                    updates["money"] = (profile.money - totalCashFee).coerceAtLeast(0L)
                }
            }
            userDoc(uid).update(updates).await()
        }
    }

    suspend fun setRegionTax(profile: CommanderProfile, region: RegionState, newTax: Long): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/regions/${region.id}") {
            requireUserId()
            if (!profile.kingMode && !region.controller.equals(profile.stateName, ignoreCase = true)) {
                throw IllegalStateException("Only the Owner or State Leader can adjust tax & travel prices.")
            }
            userDoc(uid).collection("regions").document(region.id).update(
                mapOf(
                    "taxRate" to newTax.coerceIn(0L, 50L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun exploreResources(profile: CommanderProfile): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val remaining = (profile.explorationLimit - profile.explorationUsed).coerceAtLeast(0L)
            if (remaining == 0L && !profile.kingMode) {
                throw IllegalStateException("Exploration limit 1500/1500 reached!")
            }
            val nextUsed = (profile.explorationUsed + 150L).coerceAtMost(profile.explorationLimit)
            userDoc(uid).update(
                mapOf(
                    "explorationUsed" to nextUsed,
                    "gold" to (profile.gold + 45L),
                    "wood" to (profile.wood + 90L),
                    "oil" to (profile.oil + 80L),
                    "ore" to (profile.ore + 75L),
                    "diamonds" to (profile.diamonds + 15L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            "Explored! +45G, +90 Wood, +80 Oil, +75 Ore ($nextUsed/${profile.explorationLimit})"
        }
    }

    suspend fun performWork(
        profile: CommanderProfile,
        region: RegionState,
        factory: FactoryType,
        boostedGold: Boolean
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val isBornOrResident = region.id == profile.birthRegionId || region.id in profile.residencies
            if (!profile.kingMode && !isBornOrResident) {
                throw IllegalStateException("You are free to work in your born region, but ${region.name} requires Residency!")
            }
            val spend = if (boostedGold) 50L else 10L
            if (!profile.kingMode && profile.energy < spend) {
                throw IllegalStateException("Energy low (${profile.energy}E)! Drink an Energy Drink.")
            }

            val buildingMult = 1.0 + (region.powerPlantLevel - 1L) * 0.15
            val factoryMult = (1.0 + (profile.factoryLevel * 0.25)) * buildingMult
            val eduMult = 1.0 + (profile.education / 200.0)
            val boostMult = if (boostedGold) 1.95 else 1.0
            val taxFactor = (100L - region.taxRate).coerceIn(50L, 100L) / 100.0
            val earnedCash = (spend * factory.cashPerEnergy * factoryMult * eduMult * boostMult * taxFactor).toLong()
            val earnedRes = ((spend / 10.0) * factory.baseYield * factoryMult * boostMult).toLong().coerceAtLeast(1L)
            val bonusGold = if (boostedGold) 1L else 0L
            val earnedWorkExp = (if (boostedGold) 25L else 10L) + region.schoolLevel * 2L + (profile.education / 4L)
            val newWorkExp = profile.workExp + earnedWorkExp
            val newXp = profile.xp + spend * 5L + (profile.education / 2L)
            val computedLevel = maxOf(
                profile.level,
                computePlayerLevel(xp = newXp, workExp = newWorkExp, totalDamage = profile.totalDamage)
            )
            val newEnergy = if (profile.kingMode) profile.maxEnergy else (profile.energy - spend).coerceAtLeast(0L)

            val updates = mutableMapOf<String, Any>(
                "energy" to newEnergy,
                "xp" to newXp,
                "level" to computedLevel,
                "workExp" to newWorkExp,
                "factoryWorkCount" to (profile.factoryWorkCount + 1L),
                "money" to (profile.money + earnedCash),
                "gold" to (profile.gold + bonusGold),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val resSummary = when (factory) {
                FactoryType.GOLD_MINE -> {
                    updates["gold"] = profile.gold + earnedRes + bonusGold
                    "+${earnedRes + bonusGold} G"
                }
                FactoryType.OIL_RIG -> {
                    updates["oil"] = profile.oil + earnedRes
                    "+$earnedRes Oil"
                }
                FactoryType.ORE_QUARRY -> {
                    updates["ore"] = profile.ore + earnedRes
                    "+$earnedRes Ore"
                }
                FactoryType.WOOD_MILL -> {
                    updates["wood"] = profile.wood + earnedRes
                    "+$earnedRes Wood"
                }
                FactoryType.URANIUM_PLANT -> {
                    updates["uranium"] = profile.uranium + earnedRes
                    "+$earnedRes Uranium"
                }
                FactoryType.DIAMOND_WORKS -> {
                    updates["diamonds"] = profile.diamonds + earnedRes
                    "+0.${earnedRes} Diamonds"
                }
            }
            userDoc(uid).update(updates).await()
            "Worked ${factory.title}: +${formatDots(earnedCash)} $ · $resSummary (+${earnedWorkExp} WorkExp)"
        }
    }

    suspend fun toggleAutoWork(profile: CommanderProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val targetState = !profile.autoWorkActive
            if (targetState && !profile.kingMode && !profile.premiumActive) {
                throw IllegalStateException("Premium required for Auto Work! Click Work manually or activate Premium.")
            }
            userDoc(uid).update(
                mapOf(
                    "autoWorkActive" to targetState,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun upgradeYourFactory(profile: CommanderProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val goldCost = 500L
            val cashCost = 10_000_000L
            if (!profile.kingMode && (profile.gold < goldCost || profile.money < cashCost)) {
                throw IllegalStateException("Your Factory upgrade costs -500G and -10.000.000 $")
            }
            userDoc(uid).update(
                mapOf(
                    "factoryLevel" to (profile.factoryLevel + 1L).coerceAtMost(100L),
                    "gold" to if (profile.kingMode) profile.gold else (profile.gold - goldCost),
                    "money" to if (profile.kingMode) profile.money else (profile.money - cashCost),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun useEnergyDrink(profile: CommanderProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            if (!profile.kingMode && profile.energyDrinks <= 0L) {
                throw IllegalStateException("No Energy Drinks left!")
            }
            if (!profile.kingMode && profile.energy >= profile.maxEnergy) {
                throw IllegalStateException("Energy is already full (${profile.energy}E / ${profile.maxEnergy}E)!")
            }
            userDoc(uid).update(
                mapOf(
                    "energy" to maxOf(profile.maxEnergy, profile.energy),
                    "energyDrinks" to if (profile.kingMode) profile.energyDrinks else (profile.energyDrinks - 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun buyEnergyWithGold(profile: CommanderProfile, goldCost: Long): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val safeGold = goldCost.coerceIn(1L, 10_000L)
            val energyGain = safeGold * 10L // 10 Energy = 1 Gold
            if (!profile.kingMode && profile.gold < safeGold) {
                throw IllegalStateException("Need ${safeGold}G to buy ${energyGain}E (10 Energy = 1 Gold).")
            }
            val newEnergy = (profile.energy + energyGain).coerceAtMost(1_000_000L)
            val actualGain = (newEnergy - profile.energy).coerceAtLeast(0L)
            if (actualGain <= 0L) {
                throw IllegalStateException("Energy reserve is at max cap (1.000.000 E)!")
            }
            userDoc(uid).update(
                mapOf(
                    "energy" to newEnergy,
                    "gold" to if (profile.kingMode) profile.gold else (profile.gold - safeGold),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            "Bought +${formatDots(actualGain)}E for -${safeGold}G (10 Energy = 1 Gold)!"
        }
    }

    suspend fun activatePremium(profile: CommanderProfile): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            if (profile.premiumActive || profile.kingMode) {
                throw IllegalStateException("Premium Auto Mode is already active!")
            }
            val cost = 500L
            if (profile.gold < cost) {
                throw IllegalStateException("Need 500G to activate Premium (or complete the Quests in Profile for FREE Premium)!")
            }
            userDoc(uid).update(
                mapOf(
                    "premiumActive" to true,
                    "gold" to (profile.gold - cost),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            "Premium Activated! Auto Work & Auto War Fight unlocked!"
        }
    }

    suspend fun claimQuestReward(
        profile: CommanderProfile,
        questBit: Int,
        rewardGold: Long,
        grantPremium: Boolean
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val bitMask = 1L shl questBit
            if ((profile.questProgressMask and bitMask) != 0L) {
                throw IllegalStateException("Quest reward already claimed!")
            }
            val updates = mutableMapOf<String, Any>(
                "questProgressMask" to (profile.questProgressMask or bitMask),
                "gold" to (profile.gold + rewardGold),
                "medals" to (profile.medals + 1L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (grantPremium) {
                updates["premiumActive"] = true
            }
            userDoc(uid).update(updates).await()
            if (grantPremium) {
                "Quest claimed! +${rewardGold}G & FREE Premium Auto Mode unlocked!"
            } else {
                "Quest claimed! +${rewardGold}G & +1 Medal!"
            }
        }
    }

    suspend fun buyOrMintGold(profile: CommanderProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            if (profile.kingMode) {
                // Owner unlimited supply: gold, wood, resources, troops, war gadgets, money, tickets
                userDoc(uid).update(
                    mapOf(
                        "gold" to (profile.gold + 100_000L),
                        "wood" to (profile.wood + 100_000L),
                        "oil" to (profile.oil + 100_000L),
                        "ore" to (profile.ore + 100_000L),
                        "uranium" to (profile.uranium + 50_000L),
                        "diamonds" to (profile.diamonds + 50_000L),
                        "tanks" to (profile.tanks + 100_000L),
                        "bombers" to (profile.bombers + 25_000L),
                        "missiles" to (profile.missiles + 10_000L),
                        "energyDrinks" to (profile.energyDrinks + 5_000L),
                        "tickets" to (profile.tickets + 500L),
                        "money" to (profile.money + 10_000_000_000L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val cashCost = 50_000_000L
                if (profile.money < cashCost) {
                    throw IllegalStateException("Need 50.000.000 $ to exchange for +50 G")
                }
                userDoc(uid).update(
                    mapOf(
                        "gold" to (profile.gold + 50L),
                        "money" to (profile.money - cashCost),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }
    }

    suspend fun craftWeapon(profile: CommanderProfile, weapon: WeaponType, quantity: Long): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val qty = quantity.coerceIn(1L, 5000L)
            val oilCost = weapon.oilCost * qty
            val oreCost = weapon.oreCost * qty
            val woodCost = weapon.woodCost * qty
            val uraniumCost = weapon.uraniumCost * qty
            val cashCost = weapon.cashCost * qty

            if (!profile.kingMode) {
                if (profile.oil < oilCost || profile.ore < oreCost || profile.wood < woodCost ||
                    profile.uranium < uraniumCost || profile.money < cashCost
                ) {
                    throw IllegalStateException("Need $oilCost Oil, $oreCost Ore, $woodCost Wood to craft ${qty}x ${weapon.title}")
                }
            }

            val updates = mutableMapOf<String, Any>(
                "oil" to if (profile.kingMode) profile.oil else (profile.oil - oilCost),
                "ore" to if (profile.kingMode) profile.ore else (profile.ore - oreCost),
                "wood" to if (profile.kingMode) profile.wood else (profile.wood - woodCost),
                "uranium" to if (profile.kingMode) profile.uranium else (profile.uranium - uraniumCost),
                "money" to if (profile.kingMode) profile.money else (profile.money - cashCost),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            when (weapon) {
                WeaponType.TANK -> updates["tanks"] = profile.tanks + qty
                WeaponType.BOMBER -> updates["bombers"] = profile.bombers + qty
                WeaponType.MISSILE -> updates["missiles"] = profile.missiles + qty
            }
            userDoc(uid).update(updates).await()
        }
    }

    suspend fun marketTrade(
        profile: CommanderProfile,
        resourceKey: String,
        amount: Long,
        cashDelta: Long,
        isBuy: Boolean
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            val currentRes = when (resourceKey) {
                "wood" -> profile.wood
                "oil" -> profile.oil
                "ore" -> profile.ore
                "uranium" -> profile.uranium
                "diamonds" -> profile.diamonds
                "tanks" -> profile.tanks
                "bombers" -> profile.bombers
                "missiles" -> profile.missiles
                "energyDrinks" -> profile.energyDrinks
                "tickets" -> profile.tickets
                "gold" -> profile.gold
                else -> throw IllegalArgumentException("Unknown resource $resourceKey")
            }
            if (isBuy) {
                if (!profile.kingMode && profile.money < cashDelta) {
                    throw IllegalStateException("Insufficient $ to buy.")
                }
                userDoc(uid).update(
                    mapOf(
                        resourceKey to (currentRes + amount),
                        "money" to if (profile.kingMode) profile.money else (profile.money - cashDelta),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                if (!profile.kingMode && currentRes < amount) {
                    throw IllegalStateException("Not enough $resourceKey to sell.")
                }
                userDoc(uid).update(
                    mapOf(
                        resourceKey to (currentRes - amount).coerceAtLeast(0L),
                        "money" to (profile.money + cashDelta),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }
    }

    suspend fun joinParty(
        profile: CommanderProfile,
        parties: List<PartyState>,
        targetParty: PartyState
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/parties/${targetParty.id}") {
            requireUserId()
            if (targetParty.membersCount >= targetParty.maxMembers) {
                throw IllegalStateException("Party is full (${targetParty.membersCount}/${targetParty.maxMembers} max).")
            }
            val batch = db.batch()
            parties.forEach { party ->
                if (party.id == targetParty.id && !party.userMember) {
                    val newMembers = (party.membersCount + 1L).coerceAtMost(party.maxMembers)
                    val newWedge = party.wedgeScore + profile.level
                    batch.update(
                        userDoc(uid).collection("parties").document(party.id),
                        mapOf(
                            "userMember" to true,
                            "membersCount" to newMembers,
                            "wedgeScore" to newWedge,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else if (party.id != targetParty.id && party.userMember) {
                    batch.update(
                        userDoc(uid).collection("parties").document(party.id),
                        mapOf(
                            "userMember" to false,
                            "membersCount" to (party.membersCount - 1L).coerceAtLeast(0L),
                            "wedgeScore" to (party.wedgeScore - profile.level).coerceAtLeast(0L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            batch.update(
                userDoc(uid),
                mapOf(
                    "partyId" to targetParty.id,
                    "partyName" to targetParty.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        }
    }

    suspend fun createParty(
        existingParties: List<PartyState>,
        profile: CommanderProfile,
        name: String,
        tag: String,
        ideology: String,
        colorHex: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val cleanName = name.trim().take(60)
        val cleanTag = tag.trim().uppercase().take(6)
        return safeWrite(OperationType.CREATE, "users/$uid/parties") {
            requireUserId()
            require(cleanName.length >= 2) { "Party name too short" }
            require(cleanTag.length >= 2) { "Tag too short" }
            if (!profile.kingMode && profile.gold < 200L) {
                throw IllegalStateException("Need 200G to create party.")
            }

            val newPartyRef = userDoc(uid).collection("parties").document()
            val batch = db.batch()
            existingParties.filter { it.userMember }.forEach { old ->
                batch.update(
                    userDoc(uid).collection("parties").document(old.id),
                    mapOf(
                        "userMember" to false,
                        "membersCount" to (old.membersCount - 1L).coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.set(
                newPartyRef,
                mapOf(
                    "userId" to uid,
                    "name" to cleanName,
                    "tag" to cleanTag,
                    "colorHex" to colorHex,
                    "seats" to 4L,
                    "membersCount" to 30L,
                    "maxMembers" to 60L,
                    "wedgeScore" to (30L + profile.level),
                    "ideology" to ideology.trim().ifBlank { "National Reform" }.take(80),
                    "treasuryGold" to 100L,
                    "userMember" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                userDoc(uid),
                mapOf(
                    "partyId" to newPartyRef.id,
                    "partyName" to cleanName,
                    "gold" to if (profile.kingMode) profile.gold else (profile.gold - 200L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            // Punjab bots automatically rally and join your new party
            val punjabBotJoin1 = userDoc(uid).collection("chats").document()
            batch.set(
                punjabBotJoin1,
                mapOf(
                    "userId" to uid,
                    "senderName" to "Citizen_Lahore01",
                    "senderRole" to "Punjab Bot Recruit",
                    "message" to "@${profile.commanderName} Welcome! 29 bots from Punjab - Pakistan just joined your new party [$cleanTag] $cleanName!",
                    "botSender" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            val punjabBotJoin2 = userDoc(uid).collection("chats").document()
            batch.set(
                punjabBotJoin2,
                mapOf(
                    "userId" to uid,
                    "senderName" to "General_Rawal",
                    "senderRole" to "Punjab Bot",
                    "message" to "@Citizen_Lahore01 Reporting from Punjab! We are marching with [$cleanTag] $cleanName!",
                    "botSender" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        }
    }

    suspend fun leaveParty(
        profile: CommanderProfile,
        party: PartyState
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/parties/${party.id}") {
            requireUserId()
            if (!party.userMember) {
                throw IllegalStateException("You are not a member of ${party.name}.")
            }
            val batch = db.batch()
            batch.update(
                userDoc(uid).collection("parties").document(party.id),
                mapOf(
                    "userMember" to false,
                    "membersCount" to (party.membersCount - 1L).coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                userDoc(uid),
                mapOf(
                    "partyId" to "none",
                    "partyName" to "Independent",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        }
    }

    suspend fun collectPartyWage(
        profile: CommanderProfile,
        party: PartyState,
        cashWage: Long,
        goldWage: Long
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid") {
            requireUserId()
            if (!profile.kingMode && profile.level < PartyWageManager.MIN_LEVEL_FOR_PARTY_WAGE) {
                throw IllegalStateException(
                    "Players below Level 40 cannot receive Party Wage! (Your Level: ${profile.level}/40)"
                )
            }
            val levelMult = (profile.level.toDouble() / 40.0).coerceAtLeast(1.0)
            val payoutCash = (cashWage.coerceAtLeast(0L) * levelMult).toLong()
            val payoutGold = goldWage.coerceAtLeast(0L)
            userDoc(uid).update(
                mapOf(
                    "money" to (profile.money + payoutCash),
                    "gold" to (profile.gold + payoutGold),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            "Collected Party Wage from [${party.tag}]: +${formatCompact(payoutCash)} $ & +${formatDots(payoutGold)} G (LVL ${profile.level})!"
        }
    }

    suspend fun inviteBotToPartyAndChat(
        profile: CommanderProfile,
        party: PartyState,
        botName: String,
        botRegionName: String
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/parties/${party.id}") {
            requireUserId()
            val newMembers = (party.membersCount + 1L).coerceAtMost(party.maxMembers)
            val newWedge = (party.wedgeScore + 5L).coerceAtMost(100_000L)
            val batch = db.batch()
            batch.update(
                userDoc(uid).collection("parties").document(party.id),
                mapOf(
                    "membersCount" to newMembers,
                    "wedgeScore" to newWedge,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val inviteMsg = userDoc(uid).collection("chats").document()
            batch.set(
                inviteMsg,
                mapOf(
                    "userId" to uid,
                    "senderName" to botName,
                    "senderRole" to "Bot Member ($botRegionName)",
                    "message" to "Hey ${profile.commanderName}! Thanks for inviting me to [${party.tag}] ${party.name} from $botRegionName! Ready to chat and push our party wage!",
                    "botSender" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            "$botName ($botRegionName) accepted your invite and joined ${party.name}!"
        }
    }

    suspend fun donateGoldToParty(profile: CommanderProfile, party: PartyState, amount: Long): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/parties/${party.id}") {
            requireUserId()
            if (!profile.kingMode && profile.gold < amount) {
                throw IllegalStateException("Insufficient Gold.")
            }
            val batch = db.batch()
            batch.update(
                userDoc(uid).collection("parties").document(party.id),
                mapOf(
                    "treasuryGold" to (party.treasuryGold + amount),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (!profile.kingMode) {
                batch.update(
                    userDoc(uid),
                    mapOf("gold" to (profile.gold - amount), "updatedAt" to FieldValue.serverTimestamp())
                )
            }
            batch.commit().await()
        }
    }

    suspend fun startWarOrRevolution(
        profile: CommanderProfile,
        targetRegion: RegionState,
        warType: WarType
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.CREATE, "users/$uid/wars") {
            requireUserId()
            if (warType != WarType.TRAINING && targetRegion.isSovereignProtected) {
                throw IllegalStateException("${targetRegion.name} is protected as the Sovereign Capital!")
            }
            val goldCost = if (warType == WarType.REVOLUTION) 300L else 0L
            if (!profile.kingMode && profile.gold < goldCost) {
                throw IllegalStateException("Joining Revolution costs -300G!")
            }

            val batch = db.batch()
            val warRef = userDoc(uid).collection("wars").document()
            val attackerLabel = if (warType == WarType.TRAINING) "Attack troops" else profile.stateName.ifBlank { "Alliance" }
            val defenderLabel = if (warType == WarType.TRAINING) "Defense troops" else targetRegion.controller
            batch.set(
                warRef,
                mapOf(
                    "userId" to uid,
                    "targetRegionId" to targetRegion.id,
                    "targetRegionName" to targetRegion.name,
                    "warType" to warType.name,
                    "attackerName" to attackerLabel,
                    "defenderName" to defenderLabel,
                    "attackerDamage" to 0L,
                    "defenderDamage" to 0L,
                    "membersCount" to 1L,
                    "status" to WarStatus.ACTIVE.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (goldCost > 0L && !profile.kingMode) {
                batch.update(
                    userDoc(uid),
                    mapOf("gold" to (profile.gold - goldCost), "updatedAt" to FieldValue.serverTimestamp())
                )
            }
            batch.commit().await()
            warRef.id
        }
    }

    suspend fun launchStrike(
        profile: CommanderProfile,
        war: WarFront,
        targetRegion: RegionState?,
        weapon: WeaponType?,
        unitsCount: Long,
        attackingSide: Boolean = true,
        energyMultiplier: Long = 1L
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/wars/${war.id}") {
            requireUserId()
            if (war.status != WarStatus.ACTIVE.name) {
                throw IllegalStateException("War already finished.")
            }
            val isTraining = war.warType == WarType.TRAINING.name
            if (!isTraining && (targetRegion?.isSovereignProtected == true || war.targetRegionId == "punjab")) {
                throw IllegalStateException("Punjab capital is sovereign protected!")
            }

            val mult = energyMultiplier.coerceIn(1L, 100L)
            if (mult > 1L && !profile.kingMode && !profile.premiumActive) {
                throw IllegalStateException("Premium required for Auto mode! Click Fight manually.")
            }

            val baseEnergyPerHit = 10L
            val effectiveHits = if (profile.kingMode) {
                mult
            } else {
                (profile.energy / baseEnergyPerHit).coerceAtMost(mult)
            }
            if (!profile.kingMode && effectiveHits <= 0L) {
                throw IllegalStateException("Low Energy (${profile.energy}E)! Tap Refill (10.000 Drinks) or buy 10E=1G in Market.")
            }

            val totalEnergyCost = effectiveHits * baseEnergyPerHit
            val totalUnitsNeeded = unitsCount * effectiveHits

            if (!profile.kingMode && weapon != null) {
                when (weapon) {
                    WeaponType.TANK -> if (profile.tanks < totalUnitsNeeded) throw IllegalStateException("Not enough Tanks (need $totalUnitsNeeded).")
                    WeaponType.BOMBER -> if (profile.bombers < totalUnitsNeeded) throw IllegalStateException("Not enough Bombers (need $totalUnitsNeeded).")
                    WeaponType.MISSILE -> if (profile.missiles < totalUnitsNeeded) throw IllegalStateException("Not enough Missiles (need $totalUnitsNeeded).")
                }
            }

            val perHitFinal = computeAlphaDamage(profile, weapon, unitsCount)
            val finalDamage = perHitFinal * effectiveHits
            val enemyCounter = if (isTraining) 0L else (finalDamage / 4L).coerceAtLeast(250L)

            val newAtkDamage = war.attackerDamage + if (attackingSide) finalDamage else enemyCounter
            val newDefDamage = war.defenderDamage + if (attackingSide) enemyCounter else finalDamage

            val currentHp = targetRegion?.hp ?: 250_000L
            val newHp = if (isTraining) currentHp else (currentHp - finalDamage).coerceAtLeast(0L)
            val isVictory = !isTraining && attackingSide && newHp == 0L

            val batch = db.batch()
            batch.update(
                userDoc(uid).collection("wars").document(war.id),
                mapOf(
                    "attackerDamage" to newAtkDamage,
                    "defenderDamage" to newDefDamage,
                    "status" to if (isVictory) WarStatus.VICTORY.name else WarStatus.ACTIVE.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (targetRegion != null && !isTraining) {
                val regionUpdates = mutableMapOf<String, Any>(
                    "hp" to if (isVictory) targetRegion.maxHp else newHp,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (isVictory) {
                    regionUpdates["controller"] = profile.stateName.ifBlank { "Pakistan" }
                }
                batch.update(userDoc(uid).collection("regions").document(targetRegion.id), regionUpdates)
            }

            val newTotalDamage = profile.totalDamage + finalDamage
            val newXp = profile.xp + 250L * effectiveHits + (profile.education * 3L)
            val computedLevel = maxOf(
                profile.level,
                computePlayerLevel(xp = newXp, workExp = profile.workExp, totalDamage = newTotalDamage)
            )
            val profileUpdates = mutableMapOf<String, Any>(
                "totalDamage" to newTotalDamage,
                "warsWon" to (profile.warsWon + if (isVictory) 1L else 0L),
                "xp" to newXp,
                "level" to computedLevel,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!profile.kingMode) {
                profileUpdates["energy"] = (profile.energy - totalEnergyCost).coerceAtLeast(0L)
                when (weapon) {
                    WeaponType.TANK -> profileUpdates["tanks"] = (profile.tanks - totalUnitsNeeded).coerceAtLeast(0L)
                    WeaponType.BOMBER -> profileUpdates["bombers"] = (profile.bombers - totalUnitsNeeded).coerceAtLeast(0L)
                    WeaponType.MISSILE -> profileUpdates["missiles"] = (profile.missiles - totalUnitsNeeded).coerceAtLeast(0L)
                    null -> {}
                }
            }
            batch.update(userDoc(uid), profileUpdates)
            batch.commit().await()

            when {
                isVictory -> "VICTORY! ${war.targetRegionName} captured by ${profile.stateName}! (LVL $computedLevel)"
                isTraining -> "Training Strike (${if (attackingSide) "Attack" else "Defense"}): +${formatDots(finalDamage)} α-damage! (LVL $computedLevel)"
                else -> "Fight hit ${war.targetRegionName} for ${formatDots(finalDamage)} DMG! (LVL $computedLevel)"
            }
        }
    }

    suspend fun voteOnLaw(
        profile: CommanderProfile,
        parties: List<PartyState>,
        law: ParliamentLaw,
        voteYes: Boolean,
        royalDecree: Boolean
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/laws/${law.id}") {
            requireUserId()
            if (law.status != LawStatus.VOTING.name) {
                throw IllegalStateException("Bill already resolved.")
            }
            if (royalDecree && !profile.kingMode) {
                throw IllegalStateException("Only the Owner can issue an instant Royal Decree!")
            }
            if (!profile.kingMode && !royalDecree && profile.level < PartyWageManager.MIN_LEVEL_FOR_ELECTION_VOTE) {
                throw IllegalStateException(
                    "Level ${PartyWageManager.MIN_LEVEL_FOR_ELECTION_VOTE} required to vote in elections! (Your Level: ${profile.level})"
                )
            }

            val maxSeats = profile.parliamentSeats.coerceIn(10L, 200L)
            val userPartySeats = parties.firstOrNull { it.userMember }?.seats?.coerceIn(1L, maxSeats) ?: 10L
            val remaining = (maxSeats - (law.yesVotes + law.noVotes)).coerceAtLeast(0L)

            val newYes: Long
            val newNo: Long
            val newStatus: String
            val userVoteStr = if (voteYes || royalDecree) UserVote.YES.name else UserVote.NO.name

            if (royalDecree) {
                newYes = (maxSeats - law.noVotes).coerceAtLeast((maxSeats / 2L) + 1L)
                newNo = (maxSeats - newYes).coerceAtLeast(0L)
                newStatus = LawStatus.PASSED.name
            } else {
                if (law.userVoted != UserVote.NONE.name) {
                    throw IllegalStateException("Already voted on this law.")
                }
                val weight = userPartySeats.coerceAtMost(remaining).coerceAtLeast(if (remaining > 0) 1L else 0L)
                newYes = if (voteYes) (law.yesVotes + weight).coerceAtMost(maxSeats) else law.yesVotes
                newNo = if (!voteYes) (law.noVotes + weight).coerceAtMost(maxSeats) else law.noVotes
                val majority = (maxSeats / 2L) + 1L
                newStatus = when {
                    newYes >= majority -> LawStatus.PASSED.name
                    newNo >= majority -> LawStatus.REJECTED.name
                    (newYes + newNo) >= maxSeats -> if (newYes > newNo) LawStatus.PASSED.name else LawStatus.REJECTED.name
                    else -> LawStatus.VOTING.name
                }
            }

            val batch = db.batch()
            batch.update(
                userDoc(uid).collection("laws").document(law.id),
                mapOf(
                    "yesVotes" to newYes,
                    "noVotes" to newNo,
                    "userVoted" to userVoteStr,
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (newStatus == LawStatus.PASSED.name) {
                val profUpdates = mutableMapOf<String, Any>(
                    "lawsPassed" to (profile.lawsPassed + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (law.targetSeatCount in 10L..200L) {
                    profUpdates["parliamentSeats"] = law.targetSeatCount
                }
                batch.update(userDoc(uid), profUpdates)
            }
            batch.commit().await()
        }
    }

    suspend fun proposeLaw(
        profile: CommanderProfile,
        parties: List<PartyState>,
        title: String,
        category: String,
        targetSeatCount: Long
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val cleanTitle = title.trim().take(120)
        return safeWrite(OperationType.CREATE, "users/$uid/laws") {
            requireUserId()
            require(cleanTitle.length >= 3) { "Title too short." }
            val party = parties.firstOrNull { it.userMember }
            val tag = party?.tag ?: "NRP"
            val seatInit = (party?.seats ?: 8L).coerceIn(1L, profile.parliamentSeats)
            userDoc(uid).collection("laws").document().set(
                mapOf(
                    "userId" to uid,
                    "title" to cleanTitle,
                    "category" to category.trim().ifBlank { "Economy" }.take(40),
                    "proposerParty" to tag,
                    "targetSeatCount" to targetSeatCount.coerceIn(0L, 200L),
                    "yesVotes" to seatInit,
                    "noVotes" to 2L,
                    "userVoted" to UserVote.YES.name,
                    "status" to LawStatus.VOTING.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun cancelLaw(law: ParliamentLaw): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.DELETE, "users/$uid/laws/${law.id}") {
            requireUserId()
            if (law.status != LawStatus.VOTING.name) {
                throw IllegalStateException("Only pending laws can be cancelled.")
            }
            userDoc(uid).collection("laws").document(law.id).delete().await()
        }
    }

    suspend fun holdWinnerTakesAllElection(
        profile: CommanderProfile,
        parties: List<PartyState>
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/parties") {
            requireUserId()
            if (!profile.kingMode && profile.level < PartyWageManager.MIN_LEVEL_FOR_ELECTION_VOTE) {
                throw IllegalStateException(
                    "Level ${PartyWageManager.MIN_LEVEL_FOR_ELECTION_VOTE} required to vote in election! (Your Level: ${profile.level})"
                )
            }
            val winner = parties.maxByOrNull { it.wedgeScore + (if (it.userMember) 5L else 0L) }
                ?: throw IllegalStateException("No parties found.")
            val totalSeats = profile.parliamentSeats.coerceIn(10L, 200L)
            val batch = db.batch()
            parties.forEach { party ->
                val newSeats = if (party.id == winner.id) totalSeats else 0L
                batch.update(
                    userDoc(uid).collection("parties").document(party.id),
                    mapOf("seats" to newSeats, "updatedAt" to FieldValue.serverTimestamp())
                )
            }
            batch.commit().await()
            "WINNER TAKES ALL! ${winner.name} won all $totalSeats/${totalSeats} seats!"
        }
    }

    suspend fun publishArticle(
        profile: CommanderProfile,
        title: String,
        body: String,
        newspaper: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.CREATE, "users/$uid/articles") {
            requireUserId()
            require(title.trim().length >= 2 && body.trim().length >= 2) { "Title & article text required" }
            userDoc(uid).collection("articles").document().set(
                mapOf(
                    "userId" to uid,
                    "authorName" to profile.commanderName,
                    "newspaper" to newspaper.trim().ifBlank { "World Gazette" }.take(60),
                    "title" to title.trim().take(120),
                    "body" to body.trim().take(1000),
                    "votes" to 1L,
                    "botGenerated" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun voteArticle(article: ArticlePost, delta: Long): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.UPDATE, "users/$uid/articles/${article.id}") {
            requireUserId()
            userDoc(uid).collection("articles").document(article.id).update(
                mapOf("votes" to (article.votes + delta).coerceIn(-1000L, 100000L))
            ).await()
        }
    }

    suspend fun sendChatAndBotReply(profile: CommanderProfile, rawMessage: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val msg = rawMessage.trim().take(500)
        return safeWrite(OperationType.CREATE, "users/$uid/chats") {
            requireUserId()
            require(msg.isNotBlank()) { "Message cannot be empty" }
            val batch = db.batch()
            val userMsgRef = userDoc(uid).collection("chats").document()
            batch.set(
                userMsgRef,
                mapOf(
                    "userId" to uid,
                    "senderName" to profile.commanderName,
                    "senderRole" to if (profile.kingMode) "World Owner" else "Citizen",
                    "message" to msg,
                    "botSender" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            // Bot 1 talks to player; Bot 2 talks back to Bot 1 & asks player
            val (bot1Name, bot1Role, bot1Reply) = generateBotReply(msg, profile)
            val botMsgRef1 = userDoc(uid).collection("chats").document()
            batch.set(
                botMsgRef1,
                mapOf(
                    "userId" to uid,
                    "senderName" to bot1Name,
                    "senderRole" to bot1Role,
                    "message" to bot1Reply,
                    "botSender" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            val (bot2Name, bot2Role, bot2Reply) = generateFollowUpBotBanter(bot1Name, msg, profile)
            val botMsgRef2 = userDoc(uid).collection("chats").document()
            batch.set(
                botMsgRef2,
                mapOf(
                    "userId" to uid,
                    "senderName" to bot2Name,
                    "senderRole" to bot2Role,
                    "message" to bot2Reply,
                    "botSender" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        }
    }

    suspend fun sendGlobalChatMessage(
        profile: CommanderProfile,
        regionName: String,
        rawMessage: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val msg = rawMessage.trim().take(500)
        return safeWrite(OperationType.CREATE, "global_chat") {
            requireUserId()
            require(msg.isNotBlank()) { "Message cannot be empty" }
            val isOwner = isTrueOwnerAccount()
            db.collection("global_chat").document().set(
                mapOf(
                    "senderUid" to uid,
                    "senderName" to profile.commanderName.trim().ifBlank { "Commander" }.take(60),
                    "stateName" to profile.stateName.trim().ifBlank { "Pakistan" }.take(60),
                    "regionName" to regionName.trim().ifBlank { "Punjab - Pakistan" }.take(80),
                    "isOwner" to isOwner,
                    "message" to msg,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun triggerDailyBotWave(profile: CommanderProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return safeWrite(OperationType.CREATE, "users/$uid/chats") {
            requireUserId()
            val conversations = listOf(
                listOf(
                    Triple("Trader_03", "Bot Trader", "@Citizen_01 Gold is trading 10 Energy = 1 Gold in Storage Market today! Need Oil or Ore?"),
                    Triple("Citizen_01", "Bot Citizen", "@Trader_03 Nice! Plus @${profile.commanderName} has ${formatDots(profile.energyDrinks)} Energy Drinks — ${profile.commanderName}, are we working or fighting next?")
                ),
                listOf(
                    Triple("General_04", "Bot General", "@Senator_02 Military Training is open right now! Are you sending Attack troops or Defense troops?"),
                    Triple("Senator_02", "Bot Senator", "@General_04 Attack troops! @${profile.commanderName}, drop a strike with us in Wars -> Military training!")
                ),
                listOf(
                    Triple("Citizen_01", "Bot Citizen", "@Senator_02 Did you see ${profile.stateName}'s Parliament has ${profile.parliamentSeats} seats?"),
                    Triple("Senator_02", "Bot Senator", "@Citizen_01 Yes! @${profile.commanderName}, should we upgrade State Academy or pass a new law first?")
                )
            )
            val chosenThread = conversations.random()
            val batch = db.batch()
            chosenThread.forEach { (sender, role, text) ->
                val ref = userDoc(uid).collection("chats").document()
                batch.set(
                    ref,
                    mapOf(
                        "userId" to uid,
                        "senderName" to sender,
                        "senderRole" to role,
                        "message" to text,
                        "botSender" to true,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
        }
    }

    private fun generateBotReply(userMsg: String, profile: CommanderProfile): Triple<String, String, String> {
        val lower = userMsg.lowercase(Locale.ROOT)
        return when {
            "sorry" in lower -> Triple(
                "Senator_02",
                "Bot Senator",
                "Hey ${profile.commanderName}, stop saying sorry! Drink an Energy Drink (you have ${formatDots(profile.energyDrinks)}) and get back to Work or Military Training!"
            )
            "war" in lower || "fight" in lower || "attack" in lower || "train" in lower -> Triple(
                "General_04",
                "Bot General",
                "Copy ${profile.commanderName}! Jump into Wars -> Military training and tap Fight! Remember: Auto-fight requires Premium."
            )
            "wood" in lower || "gold" in lower || "energy" in lower || "market" in lower -> Triple(
                "Trader_03",
                "Bot Trader",
                "${profile.commanderName}, you hold ${formatDots(profile.gold)} G and ${profile.energy}E. Need instant Energy? Storage Market sells 10 Energy = 1 Gold!"
            )
            else -> Triple(
                "Citizen_01",
                "Bot Citizen",
                "Loud and clear ${profile.commanderName}! You are Level ${profile.level} in ${profile.stateName} (${profile.tickets} Tickets, ${formatDots(profile.workExp)} WorkExp)."
            )
        }
    }

    private fun generateFollowUpBotBanter(firstBot: String, userMsg: String, profile: CommanderProfile): Triple<String, String, String> {
        val lower = userMsg.lowercase(Locale.ROOT)
        return when {
            "sorry" in lower -> Triple(
                "Trader_03",
                "Bot Trader",
                "@$firstBot Haha true! Come on ${profile.commanderName}, upgrade a State Building or launch a strike — we need you online!"
            )
            "energy" in lower || "gold" in lower -> Triple(
                "Citizen_01",
                "Bot Citizen",
                "@$firstBot Everyone starts with 10.000 Energy Drinks too! @${profile.commanderName} let us know when you buy Energy or Premium!"
            )
            else -> Triple(
                "Senator_02",
                "Bot Senator",
                "@$firstBot Agreed! Hey ${profile.commanderName}, want me and $firstBot to back your party (${profile.partyName}) in the next 60-seat vote?"
            )
        }
    }

    private suspend inline fun <T> safeWrite(
        op: OperationType,
        path: String,
        crossinline block: suspend () -> T
    ): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, op, path)
        Result.failure(e)
    }

    companion object {
        fun computeAlphaDamage(
            profile: CommanderProfile,
            weapon: WeaponType?,
            unitsCount: Long = 1L
        ): Long {
            val weaponDmg = weapon?.damage?.times(unitsCount) ?: 0L
            // Strength drives primary damage; Education + Endurance amplify α-damage cleanly
            val strengthDmg = profile.strength * 250L
            val educationDmg = profile.education * 120L
            val enduranceDmg = profile.endurance * 15L
            val base = weaponDmg + 1_000L + strengthDmg + educationDmg + enduranceDmg
            return if (profile.kingMode) base * 3L else base
        }

        fun computeEnergyRegenPerCycle(
            endurance: Long,
            education: Long,
            hospitalLevel: Long = 1L
        ): Long {
            // Endurance + Education + Medicine directly accelerate energy regain
            return (4L + (endurance / 25L) + (education / 5L) + (hospitalLevel * 2L)).coerceIn(5L, 200L)
        }

        fun computePlayerLevel(
            xp: Long,
            workExp: Long,
            totalDamage: Long
        ): Long {
            // Player level scales immediately from BOTH Damage & Experience (no timer needed)
            val score = xp + (workExp * 10L) + (totalDamage / 50L)
            return (1L + score / 1_000L).coerceIn(1L, 1000L)
        }

        fun formatDots(value: Long): String =
            NumberFormat.getNumberInstance(Locale.GERMANY).format(value)

        fun formatCompact(value: Long): String = when {
            value >= 1_000_000_000L -> String.format("%.2fB", value / 1_000_000_000.0)
            value >= 1_000_000L -> String.format("%.2fM", value / 1_000_000.0)
            value >= 1_000L -> String.format("%.1fK", value / 1_000.0)
            else -> value.toString()
        }
    }
}
