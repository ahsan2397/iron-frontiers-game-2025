package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthScreen
import com.example.auth.signOut
import com.example.data.BotWorldManager
import com.example.data.PartyWageManager
import com.example.data.PerkTrainingManager
import com.example.data.RivalRegionsRepository
import com.example.model.GameTab
import com.example.model.RegionState
import com.example.model.SubPanel
import com.example.model.UiState
import com.example.ui.components.RrLeftSidebar
import com.example.ui.components.TacticalBottomDock
import com.example.ui.components.TopResourceBar
import com.example.ui.screens.ArticlesTab
import com.example.ui.screens.BotChatPanel
import com.example.ui.screens.MapTab
import com.example.ui.screens.NewGameSetupScreen
import com.example.ui.screens.OverviewTab
import com.example.ui.screens.ParliamentTab
import com.example.ui.screens.PartyTab
import com.example.ui.screens.ProfileTab
import com.example.ui.screens.RankingsDialog
import com.example.ui.screens.StorageTab
import com.example.ui.screens.WarsTab
import com.example.ui.screens.WorkTab
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.RivalRegionsTheme
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg
import com.example.ui.theme.WarCrimson
import com.example.viewmodel.RivalRegionsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RivalRegionsTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            RivalRegionsGameShell(
                currentUserId = user.uid,
                displayName = user.displayName ?: "Commander"
            )
        }
    }
}

@Composable
fun RivalRegionsGameShell(
    currentUserId: String,
    displayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: RivalRegionsViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                RivalRegionsViewModel(
                    repository = RivalRegionsRepository(db),
                    currentUserId = currentUserId,
                    displayName = displayName,
                    trainingManager = PerkTrainingManager(app, currentUserId),
                    botWorldManager = BotWorldManager(app, currentUserId),
                    partyWageManager = PartyWageManager(app, currentUserId)
                )
            }
        }
    )

    val worldState by viewModel.worldState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val subPanel by viewModel.subPanel.collectAsStateWithLifecycle()
    val selectedRegionId by viewModel.selectedRegionId.collectAsStateWithLifecycle()
    val forceNewGameSetup by viewModel.forceNewGameSetup.collectAsStateWithLifecycle()
    val trainingState by viewModel.trainingState.collectAsStateWithLifecycle()
    val botDayCount by viewModel.botDayCount.collectAsStateWithLifecycle()
    val wageRevision by viewModel.wageRevision.collectAsStateWithLifecycle()
    var showRankingsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    BackHandler(enabled = subPanel != SubPanel.NONE || selectedTab != GameTab.HOME) {
        if (subPanel != SubPanel.NONE) {
            viewModel.openSubPanel(SubPanel.NONE)
        } else {
            viewModel.selectTab(GameTab.HOME)
        }
    }

    when (val state = worldState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = PakistanBright,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Syncing Iron Frontiers State...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Command Sync Error", style = MaterialTheme.typography.titleLarge, color = WarCrimson)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.retrySeed() },
                        colors = ButtonDefaults.buttonColors(containerColor = PakistanBright, contentColor = TopBarBg)
                    ) {
                        Text("Retry Connection", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        is UiState.Success -> {
            val world = state.data
            if (!world.profile.setupCompleted || forceNewGameSetup) {
                Scaffold(
                    containerColor = ObsidianBg,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { setupPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(setupPadding)) {
                        NewGameSetupScreen(
                            initialCommanderName = world.profile.commanderName,
                            initialStateName = world.profile.stateName,
                            initialPartyName = world.profile.partyName,
                            initialRegionId = world.profile.currentRegionId,
                            isOwner = world.profile.kingMode,
                            canCancel = world.profile.setupCompleted,
                            onStartNewGame = viewModel::startNewGame,
                            onCancel = viewModel::cancelNewGameSetup
                        )
                    }
                }
                return
            }

            val rankedRoster = remember(world, botDayCount) {
                viewModel.getRankedRoster(world)
            }
            val punjabBots = remember(world, botDayCount) {
                viewModel.getPunjabBotsInParty(world)
            }

            val currentRegion = world.regions.firstOrNull { it.id == world.profile.currentRegionId }
                ?: world.regions.firstOrNull()
                ?: RegionState(id = "punjab", name = "Punjab - Pakistan", country = "Pakistan", controller = "Pakistan", conquerable = false)

            Scaffold(
                containerColor = ObsidianBg,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopResourceBar(
                        profile = world.profile,
                        currentRegion = currentRegion,
                        showCompactSubStrip = (selectedTab != GameTab.HOME && selectedTab != GameTab.MAPS) || subPanel != SubPanel.NONE,
                        onSearchClick = { viewModel.selectTab(GameTab.MAPS) },
                        onProfileClick = { viewModel.selectTab(GameTab.PROFILE) },
                        onMapClick = { viewModel.selectTab(GameTab.MAPS) },
                        onEnergyClick = { viewModel.useEnergyDrink(world.profile) },
                        onOpenSubPanel = viewModel::openSubPanel,
                        onMintGold = { viewModel.buyOrMintGold(world.profile) },
                        onOpenNewGameSetup = viewModel::openNewGameSetup,
                        onSignOut = {
                            signOut(credentialManager = credentialManager, scope = scope)
                        }
                    )
                },
                bottomBar = {
                    TacticalBottomDock(
                        selectedTab = selectedTab,
                        subPanelActive = subPanel != SubPanel.NONE,
                        onSelectTab = viewModel::selectTab
                    )
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (selectedTab != GameTab.MAPS || subPanel != SubPanel.NONE) {
                        RrLeftSidebar(
                            selectedTab = selectedTab,
                            subPanel = subPanel,
                            onSelectTab = viewModel::selectTab,
                            onSelectSubPanel = viewModel::openSubPanel
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        when (subPanel) {
                            SubPanel.PARLIAMENT -> ParliamentTab(
                                world = world,
                                onVote = { law, yes, decree ->
                                    viewModel.voteOnLaw(world.profile, world.parties, law, yes, decree)
                                },
                                onProposeLaw = { title, cat, targetSeats ->
                                    viewModel.proposeLaw(world.profile, world.parties, title, cat, targetSeats)
                                },
                                onCancelLaw = { law ->
                                    viewModel.cancelLaw(law)
                                },
                                onWinnerTakesAll = {
                                    viewModel.holdWinnerTakesAllElection(world.profile, world.parties)
                                },
                                onBack = { viewModel.openSubPanel(SubPanel.NONE) }
                            )

                            SubPanel.PARTY -> PartyTab(
                                world = world,
                                punjabBots = punjabBots,
                                invitableBots = rankedRoster.filter { !it.isCurrentUser },
                                getWageConfig = { party ->
                                    wageRevision // observe recomposition on wage updates
                                    viewModel.getPartyWageConfig(party, world.profile)
                                },
                                onSetPartyWage = { party, cash, gold ->
                                    viewModel.setPartyWage(world.profile, party, cash, gold)
                                },
                                onCollectPartyWage = { party ->
                                    viewModel.collectPartyWage(world.profile, party)
                                },
                                onJoinParty = { party ->
                                    viewModel.joinParty(world.profile, world.parties, party)
                                },
                                onLeaveParty = { party ->
                                    viewModel.leaveParty(world.profile, party)
                                },
                                onDonateGold = { party, amt ->
                                    viewModel.donateToParty(world.profile, party, amt)
                                },
                                onCreateParty = { name, tag, ideology, hex ->
                                    viewModel.createParty(world.parties, world.profile, name, tag, ideology, hex)
                                },
                                onInviteBot = { party, bot ->
                                    viewModel.inviteBotToParty(world.profile, party, bot)
                                },
                                onSendChat = { msg -> viewModel.sendChat(world.profile, msg) },
                                onVoteElection = {
                                    viewModel.holdWinnerTakesAllElection(world.profile, world.parties)
                                },
                                onBack = { viewModel.openSubPanel(SubPanel.NONE) }
                            )

                            SubPanel.GLOBAL_CHAT -> BotChatPanel(
                                world = world,
                                onSendMessage = { msg -> viewModel.sendChat(world.profile, msg) },
                                onTriggerBotWave = { viewModel.triggerDailyBotWave(world.profile) },
                                onBack = { viewModel.openSubPanel(SubPanel.NONE) }
                            )

                            SubPanel.NONE -> when (selectedTab) {
                                GameTab.HOME -> OverviewTab(
                                    world = world,
                                    totalRankedPlayers = rankedRoster.size,
                                    onSelectTab = viewModel::selectTab,
                                    onOpenSubPanel = viewModel::openSubPanel,
                                    onOpenRankings = { showRankingsDialog = true },
                                    onMintGold = { viewModel.buyOrMintGold(world.profile) },
                                    onUseEnergyDrink = { viewModel.useEnergyDrink(world.profile) },
                                    onSendChat = { msg -> viewModel.sendChat(world.profile, msg) },
                                    onTriggerBotWave = { viewModel.triggerDailyBotWave(world.profile) },
                                    onOpenNewGameSetup = viewModel::openNewGameSetup
                                )

                                GameTab.ARTICLES -> ArticlesTab(
                                    world = world,
                                    onPublishArticle = { title, body, paper ->
                                        viewModel.publishArticle(world.profile, title, body, paper)
                                    },
                                    onVoteArticle = viewModel::voteArticle,
                                    onOpenBotChat = { viewModel.openSubPanel(SubPanel.GLOBAL_CHAT) }
                                )

                                GameTab.MAPS -> MapTab(
                                    world = world,
                                    selectedRegionId = selectedRegionId,
                                    onSelectRegion = viewModel::selectMapRegion,
                                    onRequestResidency = { reg -> viewModel.requestResidency(world.profile, reg) },
                                    onFlyToRegion = { reg -> viewModel.flyToRegion(world.profile, reg) },
                                    onSetTax = { reg, tax -> viewModel.setRegionTax(world.profile, reg, tax) },
                                    onDeclareWar = { reg, type -> viewModel.startWarOrRevolution(world.profile, reg, type) },
                                    onUpgradeBuilding = { reg, bld -> viewModel.upgradeStateBuilding(world.profile, reg, bld) },
                                    onOpenParliament = { viewModel.openSubPanel(SubPanel.PARLIAMENT) },
                                    onGoWork = { viewModel.selectTab(GameTab.WORK) }
                                )

                                GameTab.STORAGE -> StorageTab(
                                    world = world,
                                    onUseEnergyDrink = { viewModel.useEnergyDrink(world.profile) },
                                    onBuyEnergyWithGold = { gold -> viewModel.buyEnergyWithGold(world.profile, gold) },
                                    onMintUnlimitedSupply = { viewModel.buyOrMintGold(world.profile) },
                                    onCraftWeapon = { weapon, qty -> viewModel.craftWeapon(world.profile, weapon, qty) },
                                    onMarketTrade = { key, label, amt, cashDelta, isBuy ->
                                        viewModel.marketTrade(world.profile, key, label, amt, cashDelta, isBuy)
                                    }
                                )

                                GameTab.WORK -> WorkTab(
                                    world = world,
                                    currentRegion = currentRegion,
                                    onExplore = { viewModel.exploreResources(world.profile) },
                                    onWork = { factory, boosted ->
                                        viewModel.workInFactory(world.profile, currentRegion, factory, boosted)
                                    },
                                    onToggleAutoWork = { viewModel.toggleAutoWork(world.profile) },
                                    onUpgradeYourFactory = { viewModel.upgradeYourFactory(world.profile) },
                                    onUseEnergyDrink = { viewModel.useEnergyDrink(world.profile) }
                                )

                                GameTab.WARS -> WarsTab(
                                    world = world,
                                    onStartCampaign = { reg, type ->
                                        viewModel.startWarOrRevolution(world.profile, reg, type)
                                    },
                                    onStartTrainingWar = { reg, onCreated ->
                                        viewModel.startTrainingWar(world.profile, reg, onCreated)
                                    },
                                    onStrike = { war, region, weapon, count, attackingSide, mult ->
                                        viewModel.launchStrike(
                                            world.profile,
                                            war,
                                            region,
                                            weapon,
                                            count,
                                            attackingSide,
                                            mult
                                        )
                                    },
                                    onUseEnergyDrink = { viewModel.useEnergyDrink(world.profile) },
                                    onActivatePremium = { viewModel.activatePremium(world.profile) }
                                )

                                GameTab.PROFILE -> ProfileTab(
                                    world = world,
                                    trainingState = trainingState,
                                    onStartPerkTraining = { perk -> viewModel.startPerkTraining(world.profile, perk) },
                                    onClaimFinishedTraining = { perk -> viewModel.claimFinishedTraining(world.profile, perk) },
                                    onSpeedUpPerkTraining = { perk -> viewModel.speedUpPerkTraining(world.profile, perk) },
                                    onBuyOrMintGold = { viewModel.buyOrMintGold(world.profile) },
                                    onUseEnergyDrink = { viewModel.useEnergyDrink(world.profile) },
                                    onActivatePremium = { viewModel.activatePremium(world.profile) },
                                    onClaimQuest = { bit, gold, premium ->
                                        viewModel.claimQuestReward(world.profile, bit, gold, premium)
                                    },
                                    onSendChat = { msg -> viewModel.sendChat(world.profile, msg) },
                                    onTriggerBotWave = { viewModel.triggerDailyBotWave(world.profile) },
                                    onOpenMaps = { viewModel.selectTab(GameTab.MAPS) },
                                    onOpenRankings = { showRankingsDialog = true },
                                    onOpenSubPanel = viewModel::openSubPanel,
                                    onOpenNewGameSetup = viewModel::openNewGameSetup,
                                    onSignOut = {
                                        signOut(credentialManager = credentialManager, scope = scope)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showRankingsDialog) {
                RankingsDialog(
                    roster = rankedRoster,
                    currentDay = botDayCount,
                    onSimulateNextDay = { viewModel.simulateNewBotDay() },
                    onDismiss = { showRankingsDialog = false }
                )
            }
        }
    }
}
