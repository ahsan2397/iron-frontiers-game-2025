package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PerkTrainingManager
import com.example.data.PerkTrainingState
import com.example.data.RivalRegionsRepository
import com.example.model.SubPanel
import com.example.ui.components.PakistanFlagBadge
import com.example.ui.components.RrActionButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepCanvasBg
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.KingGold
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.PakistanEmerald
import com.example.ui.theme.PixelFontFamily
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg
import com.example.ui.theme.WarCrimson
import com.example.viewmodel.WorldData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileTab(
    world: WorldData,
    trainingState: PerkTrainingState,
    onStartPerkTraining: (String) -> Unit,
    onClaimFinishedTraining: (String) -> Unit,
    onSpeedUpPerkTraining: (String) -> Unit,
    onBuyOrMintGold: () -> Unit,
    onUseEnergyDrink: () -> Unit,
    onActivatePremium: () -> Unit,
    onClaimQuest: (Int, Long, Boolean) -> Unit,
    onSendChat: (String) -> Unit,
    onTriggerBotWave: () -> Unit,
    onOpenMaps: () -> Unit,
    onOpenRankings: () -> Unit,
    onOpenSubPanel: (SubPanel) -> Unit,
    onOpenNewGameSetup: () -> Unit,
    onSignOut: () -> Unit
) {
    val profile = world.profile
    val currentRegion = world.regions.firstOrNull { it.id == profile.currentRegionId }
    val birthRegion = world.regions.firstOrNull { it.id == profile.birthRegionId }
    val playerParty = world.parties.firstOrNull { it.id == profile.partyId || it.userMember }
    val partyTag = playerParty?.tag ?: profile.partyName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(3)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "NRP" }

    val hasPremium = profile.kingMode || profile.premiumActive
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    // Automatically finalize any perk training countdown that reached 00:00
    LaunchedEffect(nowMs, trainingState) {
        listOf("strength", "education", "endurance").forEach { perk ->
            val endMs = trainingState.endTimeFor(perk)
            if (endMs in 1L..nowMs) {
                onClaimFinishedTraining(perk)
            }
        }
    }

    var settingsExpanded by remember { mutableStateOf(false) }
    var questsExpanded by remember { mutableStateOf(false) }
    var aboutExpanded by remember { mutableStateOf(false) }
    var messagesExpanded by remember { mutableStateOf(true) }
    var expandedPerk by remember { mutableStateOf<String?>(null) }
    var draftChatMessage by remember { mutableStateOf("") }

    val q0Claimed = (profile.questProgressMask and 1L) != 0L
    val q1Claimed = (profile.questProgressMask and 2L) != 0L
    val q2Claimed = (profile.questProgressMask and 4L) != 0L
    val remainingQuests = listOf(q0Claimed, q1Claimed, q2Claimed).count { !it }
    val unclaimedGold = (if (!q0Claimed) 200L else 0L) +
        (if (!q1Claimed) 150L else 0L) +
        (if (!q2Claimed) 150L else 0L)

    val regenPerCycle = RivalRegionsRepository.computeEnergyRegenPerCycle(
        endurance = profile.endurance,
        education = profile.education,
        hospitalLevel = currentRegion?.hospitalLevel ?: 1L
    )

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCanvasBg),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Top Header Bar: [✉️ Mail]  MY PROFILE  [⚙️ Settings]
        item {
            Surface(color = Color(0xFF1F1F1F), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        IconButton(
                            onClick = {
                                messagesExpanded = true
                                coroutineScope.launch {
                                    listState.animateScrollToItem(5)
                                }
                            },
                            modifier = Modifier.testTag("profile_mail_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = "Open Messages",
                                tint = TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        if (world.chats.isNotEmpty()) {
                            Surface(
                                color = WarCrimson,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                            ) {
                                Text(
                                    text = world.chats.size.coerceAtMost(99).toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "MY PROFILE",
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )

                    Box {
                        IconButton(
                            onClick = { settingsExpanded = true },
                            modifier = Modifier.testTag("profile_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Profile Settings",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = settingsExpanded,
                            onDismissRequest = { settingsExpanded = false },
                            modifier = Modifier.background(SurfacePanel)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Parliament (${profile.parliamentSeats} Seats)", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = KingGold) },
                                onClick = {
                                    settingsExpanded = false
                                    onOpenSubPanel(SubPanel.PARLIAMENT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("New Clean Game", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = HoverYellow) },
                                onClick = {
                                    settingsExpanded = false
                                    onOpenNewGameSetup()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sign Out", color = WarCrimson) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = WarCrimson) },
                                onClick = {
                                    settingsExpanded = false
                                    onSignOut()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. Identity + Flag + [PARTY_TAG] COMMANDER_NAME + Level + XP Bar (using player's OWN identity & regions)
        item {
            val totalScore = profile.xp + (profile.workExp * 10L) + (profile.totalDamage / 50L)
            val xpProgress = ((totalScore % 1000L) / 1000f).coerceIn(0.06f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfacePanel)
                            .border(
                                width = 2.dp,
                                color = if (profile.kingMode) KingGold else HairlineBorder,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rival_crest_icon_1795066273368),
                            contentDescription = "${profile.commanderName} Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PakistanFlagBadge(
                                isPakistan = currentRegion?.country == "Pakistan" ||
                                    currentRegion?.controller.equals(profile.stateName, ignoreCase = true),
                                width = 26.dp,
                                height = 16.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "[$partyTag] ${profile.commanderName.uppercase()}",
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (profile.kingMode) {
                                "State: ${profile.stateName} · OWNER UNLIMITED · View Rankings"
                            } else {
                                "State: ${profile.stateName} · Rating Rank (View Rankings)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (profile.kingMode) KingGold else CyanAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onOpenRankings() }
                                .padding(vertical = 2.dp)
                                .testTag("profile_open_rankings_btn")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Level: ${profile.level}",
                            fontFamily = PixelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { xpProgress },
                            color = Color(0xFF55B647),
                            trackColor = SurfacePanel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Levels up from Damage & XP (${totalScore % 1000}/1000 to LVL ${profile.level + 1})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Money ($) & Gold (G) Header Row matching screenshot 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (profile.kingMode) "∞ $" else "${RivalRegionsRepository.formatDots(profile.money)} $",
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary,
                        modifier = Modifier.testTag("profile_money_text")
                    )
                    Text(
                        text = if (profile.kingMode) "∞ G" else "${RivalRegionsRepository.formatDots(profile.gold)} G",
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = HoverYellow,
                        modifier = Modifier.testTag("profile_gold_text")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Green "Buy Gold" Button
                Button(
                    onClick = onBuyOrMintGold,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF55B647),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("profile_buy_gold_btn")
                ) {
                    Text(
                        text = if (profile.kingMode) {
                            "Owner Unlimited Supply (+Gold, Wood, Troops)"
                        } else {
                            "Buy Gold (+50 G)"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Premium Status / Activation Bar
                Surface(
                    color = SurfacePanel,
                    border = BorderStroke(1.dp, if (hasPremium) PakistanEmerald else HairlineBorder),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { if (!hasPremium) onActivatePremium() }
                        .testTag("profile_premium_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasPremium) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = KingGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (profile.kingMode) {
                                    "Premium (Owner Unlimited · Auto Work & War ON)"
                                } else {
                                    "Premium (Active · Auto Work & War ON)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        } else {
                            Text(
                                text = "Activate Premium (-500 G) · Unlocks Auto Work & War",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HoverYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 3. Available Quests Accordion Bar matching screenshot 3
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Surface(
                    color = SurfacePanel,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { questsExpanded = !questsExpanded }
                        .testTag("profile_quests_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("Available quests: $remainingQuests, reward: ")
                                withStyle(SpanStyle(fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, color = HoverYellow)) {
                                    append("+${unclaimedGold} G")
                                }
                                if (!q0Claimed) {
                                    append(", ")
                                    withStyle(SpanStyle(fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, color = CyanAccent)) {
                                        append("PREMIUM")
                                    }
                                }
                            },
                            fontSize = 13.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (questsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand Quests",
                            tint = TextSecondary
                        )
                    }
                }

                if (questsExpanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuestRow(
                                title = "1. Complete a Work Shift (or claim starter grant)",
                                rewardLabel = "+200 G + FREE PREMIUM",
                                claimed = q0Claimed,
                                onClaim = { onClaimQuest(0, 200L, true) }
                            )
                            QuestRow(
                                title = "2. Start or complete a Perk Training",
                                rewardLabel = "+150 G",
                                claimed = q1Claimed,
                                onClaim = { onClaimQuest(1, 150L, false) }
                            )
                            QuestRow(
                                title = "3. Join Military Training / Deal α-Damage",
                                rewardLabel = "+150 G",
                                claimed = q2Claimed,
                                onClaim = { onClaimQuest(2, 150L, false) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Region / Residency region / Party Card (player's OWN real country & party)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenMaps() }
                    ) {
                        PakistanFlagBadge(
                            isPakistan = currentRegion?.country == "Pakistan",
                            width = 40.dp,
                            height = 28.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Region: ${currentRegion?.name ?: profile.currentRegionId}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = currentRegion?.controller ?: profile.stateName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenMaps() }
                    ) {
                        PakistanFlagBadge(
                            isPakistan = birthRegion?.country == "Pakistan",
                            width = 40.dp,
                            height = 28.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Residency region: ${birthRegion?.name ?: profile.birthRegionId}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Active residencies: ${profile.residencies.size} · Free travel to home region",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSubPanel(SubPanel.PARTY) }
                            .testTag("profile_open_party_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated)
                                .border(1.dp, KingGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = "Party", tint = KingGold, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Party: ${profile.partyName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tag: [$partyTag] · ${profile.parliamentSeats} seats Parliament",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 5. LIVE MESSAGES & BOT COMMS IN PLAYER PROFILE (Bots talk to each other & with player!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Surface(
                    color = SurfacePanel,
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { messagesExpanded = !messagesExpanded }
                        .testTag("profile_messages_header")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Forum, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MESSAGES & BOT COMMS (${world.chats.size})",
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                        Icon(
                            imageVector = if (messagesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Messages",
                            tint = TextSecondary
                        )
                    }
                }

                if (messagesExpanded) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF232323)),
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bots talk to each other & reply to ${profile.commanderName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent
                                )
                                RrActionButton(
                                    onClick = onTriggerBotWave,
                                    containerColor = SteelBlue,
                                    contentColor = Color.White,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("profile_bot_wave_btn")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bot Talk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            val recentChats = world.chats.takeLast(8)
                            if (recentChats.isEmpty()) {
                                Text(
                                    text = "Tap 'Bot Talk' or send a message below to start bot conversations!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            } else {
                                recentChats.forEach { msg ->
                                    val isBot = msg.botSender
                                    Surface(
                                        color = if (isBot) SurfacePanel else PakistanEmerald.copy(alpha = 0.18f),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isBot) HairlineBorder else PakistanBright.copy(alpha = 0.6f)
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${msg.senderName} (${msg.senderRole})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isBot) HoverYellow else PakistanBright,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (isBot) "BOT" else "YOU",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isBot) CyanAccent else PakistanBright
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = msg.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = draftChatMessage,
                                    onValueChange = { draftChatMessage = it },
                                    placeholder = {
                                        Text("Message bots or ask @Citizen_01...", fontSize = 13.sp, color = TextSecondary)
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = HairlineBorder,
                                        focusedContainerColor = SurfacePanel,
                                        unfocusedContainerColor = SurfacePanel
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("profile_chat_input")
                                )
                                Button(
                                    onClick = {
                                        val clean = draftChatMessage.trim()
                                        if (clean.isNotBlank()) {
                                            onSendChat(clean)
                                            draftChatMessage = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PakistanEmerald,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                                    modifier = Modifier.testTag("profile_chat_send_btn")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Collapsible "About me"
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Surface(
                    color = SurfacePanel,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { aboutExpanded = !aboutExpanded }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("About me", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Icon(
                            imageVector = if (aboutExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "About me",
                            tint = TextSecondary
                        )
                    }
                }
                if (aboutExpanded) {
                    Surface(
                        color = Color(0xFF232323),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${profile.commanderName} serves ${profile.stateName} from ${currentRegion?.name ?: profile.currentRegionId}. Strength boosts military α-damage; Endurance & Education accelerate energy regain and tactical combat power.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // 7. Timed Learning Perks: STRENGTH / EDUCATION / ENDURANCE (Start at 2 hours each, countdown like screenshot "STRENGTH 40:10"!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                TimedPerkAccordionRow(
                    perkKey = "strength",
                    title = "STRENGTH",
                    icon = Icons.Default.FitnessCenter,
                    value = profile.strength,
                    perkLevel = profile.strLevel,
                    effectSummary = "Boosts Military α-Damage (+${RivalRegionsRepository.formatDots(profile.strength * 250L)} DMG per strike)",
                    endAtMs = trainingState.strengthEndMs,
                    nowMs = nowMs,
                    expanded = expandedPerk == "strength",
                    isOwner = profile.kingMode,
                    onToggleExpand = {
                        expandedPerk = if (expandedPerk == "strength") null else "strength"
                    },
                    onStartTraining = { onStartPerkTraining("strength") },
                    onClaimFinished = { onClaimFinishedTraining("strength") },
                    onSpeedUp = { onSpeedUpPerkTraining("strength") }
                )
                HorizontalDivider(color = HairlineBorder.copy(alpha = 0.45f))

                TimedPerkAccordionRow(
                    perkKey = "education",
                    title = "EDUCATION",
                    icon = Icons.Default.School,
                    value = profile.education,
                    perkLevel = profile.eduLevel,
                    effectSummary = "Helps regain Energy faster & boosts Damage (+${RivalRegionsRepository.formatDots(profile.education * 120L)} DMG, +WorkExp)",
                    endAtMs = trainingState.educationEndMs,
                    nowMs = nowMs,
                    expanded = expandedPerk == "education",
                    isOwner = profile.kingMode,
                    onToggleExpand = {
                        expandedPerk = if (expandedPerk == "education") null else "education"
                    },
                    onStartTraining = { onStartPerkTraining("education") },
                    onClaimFinished = { onClaimFinishedTraining("education") },
                    onSpeedUp = { onSpeedUpPerkTraining("education") }
                )
                HorizontalDivider(color = HairlineBorder.copy(alpha = 0.45f))

                TimedPerkAccordionRow(
                    perkKey = "endurance",
                    title = "ENDURANCE",
                    icon = Icons.Default.WaterDrop,
                    value = profile.endurance,
                    perkLevel = profile.endLevel,
                    effectSummary = "Helps regain Energy faster, raises Max Energy (+10/Lvl) & adds Damage (+${RivalRegionsRepository.formatDots(profile.endurance * 15L)} DMG)",
                    endAtMs = trainingState.enduranceEndMs,
                    nowMs = nowMs,
                    expanded = expandedPerk == "endurance",
                    isOwner = profile.kingMode,
                    onToggleExpand = {
                        expandedPerk = if (expandedPerk == "endurance") null else "endurance"
                    },
                    onStartTraining = { onStartPerkTraining("endurance") },
                    onClaimFinished = { onClaimFinishedTraining("endurance") },
                    onSpeedUp = { onSpeedUpPerkTraining("endurance") }
                )
            }
        }

        // 8. Detailed information Block matching bottom of screenshot 3
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Detailed information",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    HorizontalDivider(color = HairlineBorder.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Energy:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Endurance (${profile.endurance}) + Education (${profile.education}) + Medicine Lv.${currentRegion?.hospitalLevel ?: 1} regains +${regenPerCycle}E per cycle and boosts α-damage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (profile.kingMode) "∞ E" else "${profile.energy} E / ${profile.maxEnergy} E",
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PakistanBright,
                                modifier = Modifier.testTag("profile_energy_text")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onUseEnergyDrink,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF55B647),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("profile_refill_energy_btn")
                            ) {
                                Text(
                                    text = "Refill (${if (profile.kingMode) "∞" else RivalRegionsRepository.formatDots(profile.energyDrinks)})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    DetailedInfoRow("Exp", RivalRegionsRepository.formatDots(profile.xp))
                    DetailedInfoRow("WorkExp", RivalRegionsRepository.formatDots(profile.workExp))
                    DetailedInfoRow("Total Damage", RivalRegionsRepository.formatDots(profile.totalDamage))
                    DetailedInfoRow("Tickets", if (profile.kingMode) "∞" else profile.tickets.toString())
                    DetailedInfoRow("Wars Won", profile.warsWon.toString())
                    DetailedInfoRow("Medals", profile.medals.toString())
                    DetailedInfoRow(
                        "Auto Mode (Work & War)",
                        if (hasPremium) "ON (Premium)" else "OFF (Manual Play)"
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RrActionButton(
                            onClick = onOpenNewGameSetup,
                            accentBorder = HoverYellow,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_new_game_btn")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Clean Game", style = MaterialTheme.typography.labelSmall)
                        }
                        RrActionButton(
                            onClick = onSignOut,
                            containerColor = SurfaceElevated,
                            contentColor = WarCrimson,
                            accentBorder = WarCrimson,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimedPerkAccordionRow(
    perkKey: String,
    title: String,
    icon: ImageVector,
    value: Long,
    perkLevel: Long,
    effectSummary: String,
    endAtMs: Long,
    nowMs: Long,
    expanded: Boolean,
    isOwner: Boolean,
    onToggleExpand: () -> Unit,
    onStartTraining: () -> Unit,
    onClaimFinished: () -> Unit,
    onSpeedUp: () -> Unit
) {
    val isTraining = endAtMs > 0L
    val remainingMs = (endAtMs - nowMs).coerceAtLeast(0L)
    val isFinishedWaiting = isTraining && remainingMs == 0L
    val nextDurationText = PerkTrainingManager.formatCountdown(
        PerkTrainingManager.durationSecondsForLevel(perkLevel) * 1000L
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF212121))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .testTag("perk_${perkKey}_row"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                if (isTraining) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isFinishedWaiting) "READY!" else PerkTrainingManager.formatCountdown(remainingMs),
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isFinishedWaiting) PakistanBright else HoverYellow
                    )
                }
            }

            Text(
                text = value.toString(),
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand $title",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfacePanel)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Level $perkLevel · $effectSummary",
                    style = MaterialTheme.typography.bodySmall,
                    color = PakistanBright
                )
                when {
                    isFinishedWaiting -> {
                        Button(
                            onClick = onClaimFinished,
                            colors = ButtonDefaults.buttonColors(containerColor = PakistanEmerald, contentColor = Color.White),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("perk_${perkKey}_claim_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Claim +1 $title Now!", fontWeight = FontWeight.Bold)
                        }
                    }
                    isTraining -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = HoverYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Learning... ${PerkTrainingManager.formatCountdown(remainingMs)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HoverYellow
                                )
                            }
                            RrActionButton(
                                onClick = onSpeedUp,
                                containerColor = if (isOwner) KingGold else SteelBlue,
                                contentColor = if (isOwner) TopBarBg else Color.White,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("perk_${perkKey}_speedup_btn")
                            ) {
                                Text(
                                    text = if (isOwner) "Instant +1 (Owner)" else "Speed Up (-50G)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RrActionButton(
                                onClick = onStartTraining,
                                containerColor = SteelBlue,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("perk_${perkKey}_upgrade_btn")
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Train +1 ($nextDurationText)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            if (isOwner) {
                                RrActionButton(
                                    onClick = onSpeedUp,
                                    containerColor = KingGold,
                                    contentColor = TopBarBg,
                                    modifier = Modifier.testTag("perk_${perkKey}_owner_instant_btn")
                                ) {
                                    Text("Owner +1", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestRow(
    title: String,
    rewardLabel: String,
    claimed: Boolean,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            Text(rewardLabel, style = MaterialTheme.typography.labelSmall, color = HoverYellow)
        }
        Spacer(modifier = Modifier.width(8.dp))
        RrActionButton(
            onClick = onClaim,
            enabled = !claimed,
            containerColor = if (claimed) SurfacePanel else PakistanEmerald,
            contentColor = Color.White,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(if (claimed) "Claimed" else "Claim", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailedInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            text = value,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}
