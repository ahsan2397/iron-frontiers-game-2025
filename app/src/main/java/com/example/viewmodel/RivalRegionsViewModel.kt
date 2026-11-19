package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BotWorldManager
import com.example.data.PartyWageConfig
import com.example.data.PartyWageManager
import com.example.data.PerkTrainingManager
import com.example.data.PerkTrainingState
import com.example.data.RankedPlayer
import com.example.data.RivalRegionsRepository
import com.example.model.ArticlePost
import com.example.model.ChatMessage
import com.example.model.CommanderProfile
import com.example.model.FactoryType
import com.example.model.GameTab
import com.example.model.GlobalChatMessage
import com.example.model.ParliamentLaw
import com.example.model.PartyState
import com.example.model.RegionState
import com.example.model.StateBuildingType
import com.example.model.SubPanel
import com.example.model.UiState
import com.example.model.WarFront
import com.example.model.WarType
import com.example.model.WeaponType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class WorldData(
    val profile: CommanderProfile,
    val regions: List<RegionState>,
    val parties: List<PartyState>,
    val wars: List<WarFront>,
    val laws: List<ParliamentLaw>,
    val articles: List<ArticlePost>,
    val chats: List<ChatMessage>,
    val globalChats: List<GlobalChatMessage> = emptyList()
)

class RivalRegionsViewModel(
    private val repository: RivalRegionsRepository,
    private val currentUserId: String,
    private val displayName: String,
    private val trainingManager: PerkTrainingManager? = null,
    private val botWorldManager: BotWorldManager? = null,
    private val partyWageManager: PartyWageManager? = null
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(GameTab.HOME)
    val selectedTab: StateFlow<GameTab> = _selectedTab.asStateFlow()

    private val _subPanel = MutableStateFlow(SubPanel.NONE)
    val subPanel: StateFlow<SubPanel> = _subPanel.asStateFlow()

    private val _selectedRegionId = MutableStateFlow("punjab")
    val selectedRegionId: StateFlow<String> = _selectedRegionId.asStateFlow()

    private val _forceNewGameSetup = MutableStateFlow(false)
    val forceNewGameSetup: StateFlow<Boolean> = _forceNewGameSetup.asStateFlow()

    private val _fallbackTrainingState = MutableStateFlow(PerkTrainingState())
    val trainingState: StateFlow<PerkTrainingState> = trainingManager?.state ?: _fallbackTrainingState.asStateFlow()

    private val _botDayCount = MutableStateFlow(botWorldManager?.ensureDailyPopulationUpdated() ?: 1)
    val botDayCount: StateFlow<Int> = _botDayCount.asStateFlow()

    private val _wageRevision = MutableStateFlow(0L)
    val wageRevision: StateFlow<Long> = _wageRevision.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val coreStreams = combine(
        repository.observeProfile(currentUserId),
        repository.observeRegions(currentUserId),
        repository.observeParties(currentUserId),
        repository.observeWars(currentUserId),
        repository.observeLaws(currentUserId)
    ) { profile, regions, parties, wars, laws ->
        CoreBundle(profile, regions, parties, wars, laws)
    }

    val worldState: StateFlow<UiState<WorldData>> = combine(
        coreStreams,
        repository.observeArticles(currentUserId),
        repository.observeChats(currentUserId),
        repository.observeGlobalChats(currentUserId)
    ) { core, articles, chats, globalChats ->
        val p = core.profile
        if (p == null) {
            UiState.Loading
        } else {
            UiState.Success(
                WorldData(
                    profile = p,
                    regions = core.regions,
                    parties = core.parties,
                    wars = core.wars,
                    laws = core.laws,
                    articles = articles,
                    chats = chats,
                    globalChats = globalChats
                )
            )
        }
    }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing world state", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to sync world state"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    init {
        viewModelScope.launch {
            repository.ensureWorldSeeded(displayName).onFailure { err ->
                _events.tryEmit(err.localizedMessage ?: "Failed to initialize game")
            }
        }
        // Passive Energy Regeneration (driven by Endurance + Education + Region Medicine)
        viewModelScope.launch {
            while (isActive) {
                delay(25_000L)
                val current = (worldState.value as? UiState.Success)?.data ?: continue
                if (!current.profile.kingMode && current.profile.energy < current.profile.maxEnergy) {
                    val reg = current.regions.firstOrNull { it.id == current.profile.currentRegionId }
                    repository.regenerateEnergyTick(current.profile, reg)
                }
            }
        }
    }

    fun selectTab(tab: GameTab) {
        _subPanel.value = SubPanel.NONE
        _selectedTab.value = tab
    }

    fun openSubPanel(panel: SubPanel) {
        _subPanel.value = panel
    }

    fun selectMapRegion(regionId: String) {
        _selectedRegionId.value = regionId
    }

    fun openNewGameSetup() {
        _forceNewGameSetup.value = true
    }

    fun cancelNewGameSetup() {
        _forceNewGameSetup.value = false
    }

    fun startNewGame(
        commanderName: String,
        stateName: String,
        regionId: String,
        partyName: String
    ) {
        viewModelScope.launch {
            repository.startNewCleanGame(commanderName, stateName, regionId, partyName)
                .onSuccess {
                    trainingManager?.clearAll()
                    partyWageManager?.clearAll()
                    _wageRevision.value++
                    _botDayCount.value = botWorldManager?.ensureDailyPopulationUpdated() ?: 1
                    _forceNewGameSetup.value = false
                    _selectedRegionId.value = regionId
                    _selectedTab.value = GameTab.HOME
                    _subPanel.value = SubPanel.NONE
                    _events.tryEmit("Clean New World created with 40 Real Countries!")
                }
                .onFailure { err ->
                    _events.tryEmit(err.localizedMessage ?: "Failed to start new game")
                }
        }
    }

    fun getPartyWageConfig(party: PartyState, profile: CommanderProfile): PartyWageConfig {
        val isPlayerControlled = party.userMember || party.id == profile.partyId || profile.kingMode
        return partyWageManager?.getConfig(party.id, isPlayerControlled)
            ?: PartyWageConfig(
                partyId = party.id,
                goldWage = if (isPlayerControlled) 640L else 120L,
                cashWage = if (isPlayerControlled) 50_000_000_000L else 4_500_000_000L,
                foundationDate = "Day 1 · 18 Nov 2026",
                historyEntries = emptyList()
            )
    }

    fun setPartyWage(profile: CommanderProfile, party: PartyState, cashWage: Long, goldWage: Long) {
        val isPlayerControlled = party.userMember || party.id == profile.partyId || profile.kingMode
        if (!isPlayerControlled) {
            _events.tryEmit("Only your own party leader can set wages above limits! Bot parties stay below 10B.")
            return
        }
        val note = "${profile.commanderName} set Wage: ${RivalRegionsRepository.formatDots(cashWage)} $ & ${goldWage} G (LVL 40+ eligible)"
        partyWageManager?.setPartyWages(party.id, true, cashWage, goldWage, note)
        _wageRevision.value++
        _events.tryEmit("Updated [${party.tag}] Party Wage to ${RivalRegionsRepository.formatCompact(cashWage)} $ & ${goldWage}G!")
    }

    fun leaveParty(profile: CommanderProfile, party: PartyState) = runAction(
        successMessage = "Left ${party.name}"
    ) {
        repository.leaveParty(profile, party)
    }

    fun collectPartyWage(profile: CommanderProfile, party: PartyState) {
        val cfg = getPartyWageConfig(party, profile)
        viewModelScope.launch {
            repository.collectPartyWage(profile, party, cfg.cashWage, cfg.goldWage)
                .onSuccess { msg ->
                    partyWageManager?.appendHistory(party.id, true, "${profile.commanderName} collected Wage (+${RivalRegionsRepository.formatCompact(cfg.cashWage)} $)")
                    _wageRevision.value++
                    _events.tryEmit(msg)
                }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Wage collection failed") }
        }
    }

    fun inviteBotToParty(profile: CommanderProfile, party: PartyState, bot: RankedPlayer) {
        viewModelScope.launch {
            repository.inviteBotToPartyAndChat(profile, party, bot.name, bot.regionName)
                .onSuccess { msg ->
                    partyWageManager?.appendHistory(party.id, party.userMember || profile.kingMode, "Invited ${bot.name} from ${bot.regionName}")
                    _wageRevision.value++
                    _events.tryEmit(msg)
                }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Invite failed") }
        }
    }

    fun simulateNewBotDay() {
        val nextDay = botWorldManager?.simulateNextDay() ?: (_botDayCount.value + 1)
        _botDayCount.value = nextDay
        _events.tryEmit("Day $nextDay: +3 new players arrived (including Punjab recruits)!")
    }

    fun getRankedRoster(world: WorldData): List<RankedPlayer> =
        botWorldManager?.buildRankedRoster(world.profile, world.regions) ?: emptyList()

    fun getPunjabBotsInParty(world: WorldData): List<RankedPlayer> =
        botWorldManager?.getPunjabBotsInParty(world.profile, world.regions) ?: emptyList()

    fun startPerkTraining(profile: CommanderProfile, perk: String) {
        val currentLvl = when (perk) {
            "strength" -> profile.strLevel
            "education" -> profile.eduLevel
            "endurance" -> profile.endLevel
            else -> 1L
        }
        val existingEnd = trainingState.value.endTimeFor(perk)
        val now = System.currentTimeMillis()
        if (existingEnd > 0L) {
            if (now >= existingEnd) {
                claimFinishedTraining(profile, perk)
            } else {
                _events.tryEmit("${perk.replaceFirstChar { it.uppercase() }} is already training (${PerkTrainingManager.formatCountdown(existingEnd - now)})!")
            }
            return
        }
        if (trainingManager != null) {
            val endMs = trainingManager.startTraining(perk, currentLvl, now)
            val countdown = PerkTrainingManager.formatCountdown(endMs - now)
            _events.tryEmit("Started ${perk.replaceFirstChar { it.uppercase() }} training ($countdown)!")
        }
    }

    fun claimFinishedTraining(profile: CommanderProfile, perk: String) {
        val endMs = trainingState.value.endTimeFor(perk)
        if (endMs <= 0L) return
        val now = System.currentTimeMillis()
        if (now < endMs) {
            _events.tryEmit("Still learning (${PerkTrainingManager.formatCountdown(endMs - now)} remaining)")
            return
        }
        viewModelScope.launch {
            repository.upgradePerk(profile, perk, speedUpGoldCost = 0L)
                .onSuccess {
                    trainingManager?.clearTraining(perk)
                    _events.tryEmit("${perk.replaceFirstChar { it.uppercase() }} leveled up +1!")
                }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Claim failed") }
        }
    }

    fun speedUpPerkTraining(profile: CommanderProfile, perk: String) {
        viewModelScope.launch {
            val goldCost = if (profile.kingMode) 0L else 50L
            repository.upgradePerk(profile, perk, speedUpGoldCost = goldCost)
                .onSuccess {
                    trainingManager?.clearTraining(perk)
                    _events.tryEmit(
                        if (profile.kingMode) {
                            "Owner Instant Complete: ${perk.replaceFirstChar { it.uppercase() }} +1!"
                        } else {
                            "Finished ${perk.replaceFirstChar { it.uppercase() }} +1 (-50G Speed-Up)!"
                        }
                    )
                }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Speed-up failed") }
        }
    }

    fun upgradeStateBuilding(
        profile: CommanderProfile,
        region: RegionState,
        building: StateBuildingType
    ) {
        viewModelScope.launch {
            repository.upgradeStateBuilding(profile, region, building)
                .onSuccess { msg -> _events.tryEmit(msg) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Upgrade failed") }
        }
    }

    fun requestResidency(profile: CommanderProfile, region: RegionState) = runAction(
        successMessage = "Residency granted in ${region.name}!"
    ) {
        repository.requestResidency(profile, region)
    }

    fun flyToRegion(profile: CommanderProfile, region: RegionState) = runAction(
        successMessage = "Flew to ${region.name}"
    ) {
        _selectedRegionId.value = region.id
        repository.flyToRegion(profile, region)
    }

    fun setRegionTax(profile: CommanderProfile, region: RegionState, tax: Long) = runAction(
        successMessage = "Updated ${region.name} tax to $tax%"
    ) {
        repository.setRegionTax(profile, region, tax)
    }

    fun exploreResources(profile: CommanderProfile) {
        viewModelScope.launch {
            repository.exploreResources(profile)
                .onSuccess { msg -> _events.tryEmit(msg) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Explore failed") }
        }
    }

    fun workInFactory(
        profile: CommanderProfile,
        region: RegionState,
        factory: FactoryType,
        boostedGold: Boolean
    ) {
        viewModelScope.launch {
            repository.performWork(profile, region, factory, boostedGold)
                .onSuccess { msg -> _events.tryEmit(msg) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Work failed") }
        }
    }

    fun toggleAutoWork(profile: CommanderProfile) = runAction(
        successMessage = if (!profile.autoWorkActive) "Automatic Mode ENABLED" else "Automatic Mode PAUSED"
    ) {
        repository.toggleAutoWork(profile)
    }

    fun upgradeYourFactory(profile: CommanderProfile) = runAction(
        successMessage = "Upgraded Your Factory to Level ${profile.factoryLevel + 1}!"
    ) {
        repository.upgradeYourFactory(profile)
    }

    fun useEnergyDrink(profile: CommanderProfile) = runAction(
        successMessage = "Energy refilled to ${profile.maxEnergy}E!"
    ) {
        repository.useEnergyDrink(profile)
    }

    fun buyEnergyWithGold(profile: CommanderProfile, goldCost: Long) {
        viewModelScope.launch {
            repository.buyEnergyWithGold(profile, goldCost)
                .onSuccess { msg -> _events.tryEmit(msg) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Energy purchase failed") }
        }
    }

    fun activatePremium(profile: CommanderProfile) {
        viewModelScope.launch {
            repository.activatePremium(profile)
                .onSuccess { msg -> _events.tryEmit(msg) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Premium activation failed") }
        }
    }

    fun claimQuestReward(
        profile: CommanderProfile,
        questBit: Int,
        rewardGold: Long,
        grantPremium: Boolean
    ) {
        viewModelScope.launch {
            repository.claimQuestReward(profile, questBit, rewardGold, grantPremium)
                .onSuccess { msg -> _events.tryEmit(msg) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Quest claim failed") }
        }
    }

    fun buyOrMintGold(profile: CommanderProfile) = runAction(
        successMessage = if (profile.kingMode) "Owner Unlimited Supply (+100K Resources, Wood, Troops & Gold)!" else "Exchanged +50 G!"
    ) {
        repository.buyOrMintGold(profile)
    }

    fun craftWeapon(profile: CommanderProfile, weapon: WeaponType, qty: Long) = runAction(
        successMessage = "Manufactured ${qty}x ${weapon.title}"
    ) {
        repository.craftWeapon(profile, weapon, qty)
    }

    fun marketTrade(
        profile: CommanderProfile,
        key: String,
        label: String,
        amount: Long,
        cashDelta: Long,
        isBuy: Boolean
    ) = runAction(
        successMessage = "${if (isBuy) "Bought" else "Sold"} $amount $label"
    ) {
        repository.marketTrade(profile, key, amount, cashDelta, isBuy)
    }

    fun joinParty(profile: CommanderProfile, parties: List<PartyState>, party: PartyState) = runAction(
        successMessage = "Joined ${party.name} [${party.tag}]"
    ) {
        repository.joinParty(profile, parties, party)
    }

    fun createParty(
        parties: List<PartyState>,
        profile: CommanderProfile,
        name: String,
        tag: String,
        ideology: String,
        colorHex: String
    ) = runAction(
        successMessage = "Founded party $name!"
    ) {
        repository.createParty(parties, profile, name, tag, ideology, colorHex)
    }

    fun donateToParty(profile: CommanderProfile, party: PartyState, amount: Long) = runAction(
        successMessage = "Donated ${amount}G to ${party.tag}"
    ) {
        repository.donateGoldToParty(profile, party, amount)
    }

    fun startWarOrRevolution(profile: CommanderProfile, region: RegionState, warType: WarType) {
        if (warType != WarType.TRAINING && region.isSovereignProtected) {
            _events.tryEmit("${region.name} is protected as Sovereign Capital!")
            return
        }
        viewModelScope.launch {
            repository.startWarOrRevolution(profile, region, warType)
                .onSuccess { _events.tryEmit("${warType.name} launched in ${region.name}!") }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Campaign failed") }
        }
    }

    fun startTrainingWar(
        profile: CommanderProfile,
        region: RegionState,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.startWarOrRevolution(profile, region, WarType.TRAINING)
                .onSuccess { id ->
                    _events.tryEmit("Military Training opened in ${region.name}!")
                    onCreated(id)
                }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Training launch failed") }
        }
    }

    fun launchStrike(
        profile: CommanderProfile,
        war: WarFront,
        targetRegion: RegionState?,
        weapon: WeaponType?,
        unitsCount: Long,
        attackingSide: Boolean = true,
        energyMultiplier: Long = 1L
    ) {
        viewModelScope.launch {
            repository.launchStrike(profile, war, targetRegion, weapon, unitsCount, attackingSide, energyMultiplier)
                .onSuccess { msg -> _events.tryEmit(msg) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Strike failed") }
        }
    }

    fun voteOnLaw(
        profile: CommanderProfile,
        parties: List<PartyState>,
        law: ParliamentLaw,
        voteYes: Boolean,
        royalDecree: Boolean
    ) = runAction(
        successMessage = if (royalDecree) "Owner Decree: Passed!" else "Parliament vote recorded"
    ) {
        repository.voteOnLaw(profile, parties, law, voteYes, royalDecree)
    }

    fun proposeLaw(
        profile: CommanderProfile,
        parties: List<PartyState>,
        title: String,
        category: String,
        targetSeatCount: Long
    ) = runAction(
        successMessage = "Bill proposed to ${profile.parliamentSeats}-Seat Parliament"
    ) {
        repository.proposeLaw(profile, parties, title, category, targetSeatCount)
    }

    fun cancelLaw(law: ParliamentLaw) = runAction(
        successMessage = "Law cancelled"
    ) {
        repository.cancelLaw(law)
    }

    fun holdWinnerTakesAllElection(profile: CommanderProfile, parties: List<PartyState>) {
        viewModelScope.launch {
            repository.holdWinnerTakesAllElection(profile, parties)
                .onSuccess { msg -> _events.tryEmit(msg) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Election failed") }
        }
    }

    fun publishArticle(profile: CommanderProfile, title: String, body: String, newspaper: String) = runAction(
        successMessage = "Article published!"
    ) {
        repository.publishArticle(profile, title, body, newspaper)
    }

    fun voteArticle(article: ArticlePost, delta: Long) = runAction(
        successMessage = if (delta > 0) "Upvoted article" else "Downvoted article"
    ) {
        repository.voteArticle(article, delta)
    }

    fun sendChat(profile: CommanderProfile, message: String) = runAction(
        successMessage = "Message sent"
    ) {
        repository.sendChatAndBotReply(profile, message)
    }

    fun sendGlobalChat(profile: CommanderProfile, regionName: String, message: String) = runAction(
        successMessage = "Broadcast to Global Chat"
    ) {
        repository.sendGlobalChatMessage(profile, regionName, message)
    }

    fun triggerDailyBotWave(profile: CommanderProfile) = runAction(
        successMessage = "Daily bot check-in arrived!"
    ) {
        repository.triggerDailyBotWave(profile)
    }

    fun retrySeed() {
        viewModelScope.launch {
            repository.ensureWorldSeeded(displayName).onFailure { err ->
                _events.tryEmit(err.localizedMessage ?: "Retry failed")
            }
        }
    }

    private fun runAction(
        successMessage: String,
        action: suspend () -> Result<Unit>
    ) {
        viewModelScope.launch {
            action()
                .onSuccess { _events.tryEmit(successMessage) }
                .onFailure { err -> _events.tryEmit(err.localizedMessage ?: "Action failed") }
        }
    }

    private data class CoreBundle(
        val profile: CommanderProfile?,
        val regions: List<RegionState>,
        val parties: List<PartyState>,
        val wars: List<WarFront>,
        val laws: List<ParliamentLaw>
    )

    private companion object {
        const val TAG = "RivalRegionsVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
