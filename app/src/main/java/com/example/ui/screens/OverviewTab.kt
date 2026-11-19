package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.RivalRegionsRepository
import com.example.model.GameTab
import com.example.model.SubPanel
import com.example.model.WarStatus
import com.example.ui.components.PakistanFlagBadge
import com.example.ui.components.RrActionButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.KingGold
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.PixelFontFamily
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg
import com.example.ui.theme.WarCrimson
import com.example.viewmodel.WorldData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val RrHomeBg = Color(0xFF1F1F1F)
private val RrTileBg = Color(0xFF2B2B2B)
private val RrLabelGrey = Color(0xFF9E9E9E)

@Composable
fun OverviewTab(
    world: WorldData,
    totalRankedPlayers: Int,
    onSelectTab: (GameTab) -> Unit,
    onOpenSubPanel: (SubPanel) -> Unit,
    onOpenRankings: () -> Unit,
    onMintGold: () -> Unit,
    onUseEnergyDrink: () -> Unit,
    onSendChat: (String) -> Unit,
    onTriggerBotWave: () -> Unit,
    onOpenNewGameSetup: () -> Unit
) {
    val profile = world.profile
    val currentRegion = world.regions.firstOrNull { it.id == profile.currentRegionId } ?: world.regions.firstOrNull()
    val playerParty = world.parties.firstOrNull { it.id == profile.partyId || it.userMember }
    val partyTag = playerParty?.tag ?: "NRP"
    val activeWars = world.wars.count { it.status == WarStatus.ACTIVE.name }
    val controlledRegions = world.regions.filter {
        it.controller.equals(profile.stateName, ignoreCase = true)
    }
    val distinctStates = world.regions.map { it.controller }.distinct().size
    val totalWorldPop = world.regions.sumOf { it.population }
    val statePop = controlledRegions.sumOf { it.population }.coerceAtLeast(currentRegion?.population ?: 0L)
    val pendingLaws = world.laws.count { it.status == "VOTING" }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var chatTabIndex by remember { mutableIntStateOf(0) } // 0 = ENGLISH, 1 = SHOW MORE
    var chatDraft by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RrHomeBg),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        // 1. Player Identity + Huge Blocky Money & Gold Readout
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(GameTab.PROFILE) }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rival_crest_icon_1795066273368),
                        contentDescription = "Commander Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                if (profile.kingMode) KingGold else HairlineBorder,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "[$partyTag] ${profile.commanderName.uppercase()}",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Level: ${profile.level}",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = RrLabelGrey
                            )
                            if (profile.kingMode) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "OWNER",
                                    fontFamily = PixelFontFamily,
                                    fontSize = 9.sp,
                                    color = KingGold
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .clickable { onSelectTab(GameTab.PROFILE) }
                        .testTag("home_money_gold_header")
                ) {
                    Text(
                        text = "${RivalRegionsRepository.formatDots(profile.money)} $",
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = if (profile.kingMode) {
                            "${RivalRegionsRepository.formatDots(profile.gold)} G ∞"
                        } else {
                            "${RivalRegionsRepository.formatDots(profile.gold)} G"
                        },
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = KingGold,
                        maxLines = 1
                    )
                }
            }
        }

        // 2. Compact Stat Pill Strip (Number on top + Silhouette Icon below)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                RrMiniStatTile(
                    value = profile.strength.toString(),
                    icon = Icons.Default.FitnessCenter,
                    onClick = { onSelectTab(GameTab.PROFILE) }
                )
                RrMiniStatTile(
                    value = profile.education.toString(),
                    icon = Icons.Default.School,
                    onClick = { onSelectTab(GameTab.PROFILE) }
                )
                RrMiniStatTile(
                    value = profile.endurance.toString(),
                    icon = Icons.Default.SelfImprovement,
                    onClick = { onSelectTab(GameTab.PROFILE) }
                )
                RrMiniStatTile(
                    value = if (profile.kingMode) "∞E" else "${profile.energy}E",
                    icon = Icons.Default.Bolt,
                    valueColor = if (profile.energy <= 20L && !profile.kingMode) WarCrimson else TextPrimary,
                    onClick = onUseEnergyDrink
                )
                RrMiniStatTile(
                    value = RivalRegionsRepository.formatCompact(profile.oil),
                    icon = Icons.Default.LocalGasStation,
                    onClick = { onSelectTab(GameTab.STORAGE) }
                )
                RrMiniStatTile(
                    value = RivalRegionsRepository.formatCompact(profile.tanks + profile.bombers + profile.missiles),
                    icon = Icons.Default.Flight,
                    onClick = { onSelectTab(GameTab.STORAGE) }
                )
                RrMiniStatTile(
                    value = RivalRegionsRepository.formatCompact(profile.wood),
                    icon = Icons.Default.Park,
                    onClick = { onSelectTab(GameTab.STORAGE) }
                )
                RrMiniStatTile(
                    value = RivalRegionsRepository.formatCompact(profile.ore),
                    icon = Icons.Default.Hardware,
                    onClick = { onSelectTab(GameTab.STORAGE) }
                )
                RrMiniStatTile(
                    value = if (profile.kingMode) "∞T" else "${profile.tickets}",
                    icon = Icons.Default.ConfirmationNumber,
                    onClick = { onSelectTab(GameTab.MAPS) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. World / Wars / Free / Regions / States Tile Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                RrMetricTile(
                    label = "World",
                    value = RivalRegionsRepository.formatCompact(totalWorldPop),
                    icon = Icons.Default.Groups,
                    onClick = { onSelectTab(GameTab.MAPS) }
                )
                RrMetricTile(
                    label = "Wars",
                    value = activeWars.toString(),
                    icon = Icons.Default.LocalFireDepartment,
                    onClick = { onSelectTab(GameTab.WARS) }
                )
                RrMetricTile(
                    label = "Free",
                    value = if (profile.kingMode) "∞" else "${profile.energy}",
                    icon = Icons.Default.PanTool,
                    iconTint = PakistanBright,
                    valueColor = PakistanBright,
                    onClick = { onSelectTab(GameTab.WORK) }
                )
                RrMetricTile(
                    label = "Rankings",
                    value = totalRankedPlayers.coerceAtLeast(1).toString(),
                    icon = Icons.Default.EmojiEvents,
                    iconTint = HoverYellow,
                    valueColor = HoverYellow,
                    onClick = onOpenRankings
                )
                RrMetricTile(
                    label = "Regions",
                    value = world.regions.size.toString(),
                    icon = Icons.Default.Place,
                    onClick = { onSelectTab(GameTab.MAPS) }
                )
                RrMetricTile(
                    label = "States",
                    value = distinctStates.toString(),
                    icon = Icons.Default.Hub,
                    onClick = { onSelectTab(GameTab.MAPS) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. "Your region: <Name>" Row (Emblem Left + Tiles Right)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 66.dp, height = 68.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfacePanel)
                        .border(1.dp, KingGold.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                        .clickable { onSelectTab(GameTab.MAPS) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PakistanFlagBadge(
                            isPakistan = currentRegion?.country == "Pakistan" ||
                                currentRegion?.controller.equals(profile.stateName, ignoreCase = true),
                            width = 48.dp,
                            height = 30.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentRegion?.country?.uppercase() ?: "PAKISTAN",
                            fontFamily = PixelFontFamily,
                            fontSize = 8.sp,
                            color = KingGold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your region: ${currentRegion?.name ?: "Punjab - Pakistan"}",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = RrLabelGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        RrMetricTile(
                            label = "Population",
                            value = RivalRegionsRepository.formatCompact(currentRegion?.population ?: 0L),
                            icon = Icons.Default.Groups,
                            onClick = { onSelectTab(GameTab.MAPS) }
                        )
                        RrMetricTile(
                            label = "Academy",
                            value = "${currentRegion?.schoolLevel ?: 1L}",
                            icon = Icons.Default.Apartment,
                            onClick = { onSelectTab(GameTab.MAPS) }
                        )
                        RrMetricTile(
                            label = "Parties",
                            value = world.parties.size.toString(),
                            icon = Icons.Default.AccountBalance,
                            onClick = { onOpenSubPanel(SubPanel.PARTY) }
                        )
                        RrMetricTile(
                            label = "Factories",
                            value = "${profile.factoryLevel}",
                            icon = Icons.Default.Construction,
                            onClick = { onSelectTab(GameTab.WORK) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 5. "State: <StateName>" Row (Flag Left + Orders/Parliament/Regions/Pop Tiles Right)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 66.dp, height = 68.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF14212B))
                        .border(1.dp, SteelBlue.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .clickable { onOpenSubPanel(SubPanel.PARLIAMENT) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PakistanFlagBadge(
                            isPakistan = true,
                            width = 48.dp,
                            height = 30.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "STATE",
                            fontFamily = PixelFontFamily,
                            fontSize = 8.sp,
                            color = CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "State: ${profile.stateName}",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = RrLabelGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        RrMetricTile(
                            label = "Orders",
                            value = pendingLaws.toString(),
                            icon = Icons.Default.Gavel,
                            onClick = { onOpenSubPanel(SubPanel.PARLIAMENT) }
                        )
                        RrMetricTile(
                            label = "Parliament",
                            value = "${profile.parliamentSeats}",
                            icon = Icons.Default.AccountBalance,
                            onClick = { onOpenSubPanel(SubPanel.PARLIAMENT) }
                        )
                        RrMetricTile(
                            label = "Regions",
                            value = controlledRegions.size.coerceAtLeast(1).toString(),
                            icon = Icons.Default.Hub,
                            onClick = { onSelectTab(GameTab.MAPS) }
                        )
                        RrMetricTile(
                            label = "Pop",
                            value = RivalRegionsRepository.formatCompact(statePop),
                            icon = Icons.Default.Groups,
                            onClick = { onSelectTab(GameTab.MAPS) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 6. ENGLISH | SHOW MORE Split Tab Bar (Exact RR Chat/Command Header)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { chatTabIndex = 0 }
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ENGLISH",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (chatTabIndex == 0) TextPrimary else RrLabelGrey
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(if (chatTabIndex == 0) SteelBlue else Color.Transparent)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { chatTabIndex = 1 }
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SHOW MORE",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (chatTabIndex == 1) TextPrimary else RrLabelGrey
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(if (chatTabIndex == 1) SteelBlue else Color.Transparent)
                        )
                    }
                }
                HorizontalDivider(color = HairlineBorder.copy(alpha = 0.55f), thickness = 1.dp)
            }
        }

        if (chatTabIndex == 0) {
            // Live Inline Chat Input (Compact RR Style)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatDraft,
                        onValueChange = { chatDraft = it },
                        placeholder = {
                            Text("Message world chat...", fontSize = 12.sp, color = TextMuted)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SteelBlue,
                            unfocusedBorderColor = HairlineBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_chat_input")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (chatDraft.isNotBlank()) {
                                onSendChat(chatDraft)
                                chatDraft = ""
                            }
                        },
                        modifier = Modifier.testTag("home_chat_send")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Chat",
                            tint = PakistanBright
                        )
                    }
                    IconButton(
                        onClick = onTriggerBotWave,
                        modifier = Modifier.testTag("home_bot_wave")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Bot Check-In",
                            tint = RrLabelGrey
                        )
                    }
                }
            }

            // Live Chat Rows matching Screenshot (Avatar left, Sender grey + Message white, Time + 文A right)
            items(world.chats.reversed(), key = { it.id }) { msg ->
                val dateMillis = msg.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                val timeStr = timeFormatter.format(Date(dateMillis))
                val avatarColor = when {
                    msg.senderName == profile.commanderName -> PakistanBright
                    msg.botSender -> SteelBlue
                    else -> KingGold
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSubPanel(SubPanel.GLOBAL_CHAT) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(avatarColor.copy(alpha = 0.22f))
                            .border(1.dp, avatarColor.copy(alpha = 0.55f), RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = msg.senderName.take(2).uppercase(),
                            fontFamily = PixelFontFamily,
                            fontSize = 11.sp,
                            color = avatarColor
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = msg.senderName,
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = RrLabelGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = msg.message,
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeStr,
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                HorizontalDivider(color = HairlineBorder.copy(alpha = 0.22f), thickness = 1.dp)
            }
        } else {
            // SHOW MORE: Clean World Actions, Owner Unlimited Controls, State Buildings & New Game
            item {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (profile.kingMode) {
                        Surface(
                            color = SurfacePanel,
                            border = BorderStroke(1.dp, KingGold),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = KingGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("OWNER UNLIMITED EVERYTHING", style = MaterialTheme.typography.titleMedium, color = KingGold)
                                }
                                Text(
                                    text = "Unlimited troops, war gadgets, resources, Gold & Wood + upgrade any State Building.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                RrActionButton(
                                    onClick = onMintGold,
                                    containerColor = KingGold,
                                    contentColor = TopBarBg,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_king_mint")
                                ) {
                                    Text("UNLIMITED SUPPLY (+GOLD, WOOD, RESOURCES & TROOPS)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RrActionButton(
                            onClick = { onSelectTab(GameTab.MAPS) },
                            accentBorder = CyanAccent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("State Buildings", style = MaterialTheme.typography.labelSmall)
                        }
                        RrActionButton(
                            onClick = { onOpenSubPanel(SubPanel.PARLIAMENT) },
                            accentBorder = HoverYellow,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Parliament (${profile.parliamentSeats})", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    RrActionButton(
                        onClick = onOpenNewGameSetup,
                        containerColor = SurfaceElevated,
                        contentColor = HoverYellow,
                        accentBorder = HoverYellow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_new_game_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Start New Game", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START NEW GAME (CLEAN 40-COUNTRY WORLD)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RrMiniStatTile(
    value: String,
    icon: ImageVector,
    valueColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Surface(
        color = RrTileBg,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = valueColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RrLabelGrey,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun RrMetricTile(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = RrLabelGrey,
    valueColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Surface(
        color = RrTileBg,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .width(86.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)) {
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontSize = 11.sp,
                color = RrLabelGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = value,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = valueColor,
                    maxLines = 1
                )
            }
        }
    }
}
