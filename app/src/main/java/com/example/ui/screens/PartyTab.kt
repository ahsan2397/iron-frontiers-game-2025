package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PartyWageConfig
import com.example.data.PartyWageManager
import com.example.data.RankedPlayer
import com.example.data.RivalRegionsRepository
import com.example.model.PartyState
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

fun parseHexColor(hex: String, fallback: Color = PakistanEmerald): Color =
    try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }

@Composable
fun PartyTab(
    world: WorldData,
    punjabBots: List<RankedPlayer> = emptyList(),
    invitableBots: List<RankedPlayer> = emptyList(),
    getWageConfig: (PartyState) -> PartyWageConfig,
    onSetPartyWage: (PartyState, Long, Long) -> Unit,
    onCollectPartyWage: (PartyState) -> Unit,
    onJoinParty: (PartyState) -> Unit,
    onLeaveParty: (PartyState) -> Unit,
    onDonateGold: (PartyState, Long) -> Unit,
    onCreateParty: (name: String, tag: String, ideology: String, colorHex: String) -> Unit,
    onInviteBot: (PartyState, RankedPlayer) -> Unit,
    onSendChat: (String) -> Unit,
    onVoteElection: () -> Unit,
    onBack: () -> Unit = {}
) {
    val profile = world.profile
    val currentRegion = world.regions.firstOrNull { it.id == profile.currentRegionId } ?: world.regions.firstOrNull()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showWageEditor by remember { mutableStateOf(false) }
    var partyChatDraft by remember { mutableStateOf("") }

    var selectedPartyId by remember(world.profile.partyId, world.parties) {
        mutableStateOf(
            world.parties.firstOrNull { it.id == profile.partyId || it.userMember }?.id
                ?: world.parties.firstOrNull()?.id.orEmpty()
        )
    }

    val activeParty = world.parties.firstOrNull { it.id == selectedPartyId }
        ?: world.parties.firstOrNull { it.userMember }
        ?: world.parties.firstOrNull()

    val totalWedge = world.parties.sumOf { it.wedgeScore }.coerceAtLeast(1L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCanvasBg),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Header Bar matching Screenshot_20261119-114628.jpg: ← PARTY NAME ? 🔗
        item {
            Surface(color = Color(0xFF212121), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("party_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = activeParty?.name?.uppercase() ?: "POLITICAL PARTIES",
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Party Rules", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("open_create_party_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Found Party", tint = HoverYellow)
                    }
                    IconButton(onClick = onVoteElection) {
                        Icon(Icons.Default.Link, contentDescription = "Parliament Election", tint = TextPrimary)
                    }
                }
            }
        }

        // 2. Active Party Detail View (Exact Screenshot_20261119-114628.jpg RR Party Card)
        if (activeParty != null) {
            val wageCfg = getWageConfig(activeParty)
            val isOwnOrLeader = activeParty.userMember || activeParty.id == profile.partyId || profile.kingMode
            val accent = parseHexColor(activeParty.colorHex)
            val canCollectWage = profile.kingMode || profile.level >= PartyWageManager.MIN_LEVEL_FOR_PARTY_WAGE
            val canVoteElection = profile.kingMode || profile.level >= PartyWageManager.MIN_LEVEL_FOR_ELECTION_VOTE

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Emblem left + Members/Ideology & Region/State right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfacePanel)
                                .border(2.dp, accent, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.rival_crest_icon_1795066273368),
                                contentDescription = "${activeParty.name} Crest",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = TopBarBg.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = activeParty.tag,
                                    fontFamily = PixelFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = HoverYellow,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Members: ${activeParty.membersCount} citizens",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Ideology: ${activeParty.ideology}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PakistanFlagBadge(
                                    isPakistan = currentRegion?.country == "Pakistan",
                                    width = 28.dp,
                                    height = 18.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = currentRegion?.name ?: "Punjab - Pakistan",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "State: ${currentRegion?.controller ?: profile.stateName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Red "Leave party" OR Green "Join party" button
                    if (activeParty.userMember) {
                        Button(
                            onClick = { onLeaveParty(activeParty) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD93F1E),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("leave_party_btn")
                        ) {
                            Text("Leave party", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    } else {
                        Button(
                            onClick = { onJoinParty(activeParty) },
                            enabled = activeParty.membersCount < activeParty.maxMembers,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PakistanEmerald,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("join_party_${activeParty.id}")
                        ) {
                            Text(
                                text = if (activeParty.membersCount >= activeParty.maxMembers) "Party Full (60/60)" else "Join party",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Motto line
                    Text(
                        text = "${activeParty.name} — Real Nation Builders · Level 40+ for Party Wage · Level 60+ for Election Vote",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    // History button
                    Surface(
                        color = Color(0xFF2B2B2B),
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showHistory = !showHistory }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (showHistory) "Hide History" else "History",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (showHistory) {
                        Surface(
                            color = SurfacePanel,
                            border = BorderStroke(1.dp, HairlineBorder),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                wageCfg.historyEntries.forEach { entry ->
                                    Text("• $entry", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }

                    // Foundation date / Party wage (Gold) / Wage (active) ($) rows matching Screenshot_20261119-114628.jpg
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Foundation date:", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(wageCfg.foundationDate, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Party wage:", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(
                                text = "${RivalRegionsRepository.formatDots(wageCfg.goldWage)} G",
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = HoverYellow
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Wage (active):", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                                if (!isOwnOrLeader) {
                                    Text("Bot party capped < 10B", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                                }
                            }
                            Text(
                                text = "${RivalRegionsRepository.formatDots(wageCfg.cashWage)} $",
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    // Blue Election Button (Level 60+ rule)
                    Button(
                        onClick = onVoteElection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2D6AE0),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("party_election_vote_btn")
                    ) {
                        if (!canVoteElection) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (canVoteElection) {
                                "Voting in progress · Vote in Parliament Election"
                            } else {
                                "Election Voting (Requires Level 60 · Yours: LVL ${profile.level})"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Party Wage Claim (LVL 40+ rule) + Party Leader Unlimited Wage Setter
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RrActionButton(
                            onClick = { onCollectPartyWage(activeParty) },
                            containerColor = if (canCollectWage) PakistanEmerald else SurfacePanel,
                            contentColor = Color.White,
                            accentBorder = if (canCollectWage) PakistanBright else WarCrimson,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("collect_party_wage_btn")
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (canCollectWage) "Collect Wage" else "Wage: Need LVL 40+",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isOwnOrLeader) {
                            RrActionButton(
                                onClick = { showWageEditor = true },
                                containerColor = SurfaceElevated,
                                contentColor = HoverYellow,
                                accentBorder = HoverYellow,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_party_wage_btn")
                            ) {
                                Text("Set Party Wage", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Invite Bots to Come & Talk Like Normal Humans + Live Party Bot Chat
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Forum, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INVITE BOTS & TALK LIKE HUMANS",
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Tap any citizen bot below to invite them into ${activeParty.name} — they join and reply like real players!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(invitableBots.take(10), key = { it.id }) { bot ->
                                RrActionButton(
                                    onClick = { onInviteBot(activeParty, bot) },
                                    containerColor = SurfaceElevated,
                                    contentColor = PakistanBright,
                                    accentBorder = PakistanBright,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp),
                                    modifier = Modifier.testTag("invite_bot_${bot.id}")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${bot.name} (${bot.regionId})", fontSize = 11.sp)
                                }
                            }
                        }

                        // Recent human-like chat replies right in Party screen
                        val recentChats = world.chats.takeLast(5)
                        recentChats.forEach { msg ->
                            Surface(
                                color = if (msg.botSender) Color(0xFF232323) else PakistanEmerald.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, HairlineBorder),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(7.dp)) {
                                    Text(
                                        text = "${msg.senderName} (${msg.senderRole})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (msg.botSender) HoverYellow else PakistanBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(msg.message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = partyChatDraft,
                                onValueChange = { partyChatDraft = it },
                                placeholder = { Text("Talk to party bots...", fontSize = 12.sp, color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = HairlineBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("party_chat_input")
                            )
                            Button(
                                onClick = {
                                    val clean = partyChatDraft.trim()
                                    if (clean.isNotBlank()) {
                                        onSendChat(clean)
                                        partyChatDraft = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, contentColor = Color.White),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                                modifier = Modifier.testTag("party_chat_send_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 4. Party Wedge Chart + Other Parties Switcher (Notice: Bot parties stay < 10B!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ALL PARTIES & WEDGE", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        RrActionButton(
                            onClick = { showCreateDialog = true },
                            containerColor = PakistanBright,
                            contentColor = TopBarBg,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Party", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .testTag("party_wedge_chart"),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val diameter = size.minDimension - 8.dp.toPx()
                                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                                val arcSize = Size(diameter, diameter)
                                var startAngle = -90f
                                val gap = 2.4f
                                world.parties.forEach { party ->
                                    val sweep = ((party.wedgeScore.toFloat() / totalWedge.toFloat()) * 360f) - gap
                                    if (sweep > 0f) {
                                        drawArc(
                                            color = parseHexColor(party.colorHex),
                                            startAngle = startAngle,
                                            sweepAngle = sweep,
                                            useCenter = true,
                                            topLeft = topLeft,
                                            size = arcSize
                                        )
                                        startAngle += sweep + gap
                                    }
                                }
                                drawCircle(color = SurfacePanel, radius = diameter * 0.25f, center = center)
                            }
                            Text(totalWedge.toString(), style = MaterialTheme.typography.labelLarge, color = HoverYellow)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            world.parties.forEach { party ->
                                val cfg = getWageConfig(party)
                                val accent = parseHexColor(party.colorHex)
                                val selected = party.id == activeParty?.id
                                Surface(
                                    color = if (selected) SurfaceElevated else Color(0xFF242424),
                                    border = BorderStroke(1.dp, if (selected) HoverYellow else HairlineBorder),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { selectedPartyId = party.id }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("[${party.tag}] ${party.name}", style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (party.userMember) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PakistanBright, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                            Text(
                                                text = "Wage: ${RivalRegionsRepository.formatCompact(cfg.cashWage)} $ · ${cfg.goldWage}G · ${party.membersCount}/${party.maxMembers}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }
                                        RrActionButton(
                                            onClick = { onDonateGold(party, 50L) },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = KingGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("50G", fontSize = 10.sp, color = KingGold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Punjab Bots Rallying in Your Party
        if (punjabBots.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, PakistanBright.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "PUNJAB BOTS JOINING YOUR PARTY (${punjabBots.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = PakistanBright,
                            fontWeight = FontWeight.Bold
                        )
                        punjabBots.take(6).forEach { bot ->
                            val wageStatus = if (bot.level >= PartyWageManager.MIN_LEVEL_FOR_PARTY_WAGE) {
                                "LVL ${bot.level} · Wage Eligible"
                            } else {
                                "LVL ${bot.level} (<40: No Wage)"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${bot.name} (Punjab)", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Text(
                                    text = wageStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (bot.level >= PartyWageManager.MIN_LEVEL_FOR_PARTY_WAGE) PakistanBright else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWageEditor && activeParty != null) {
        val currentCfg = getWageConfig(activeParty)
        PartyWageEditorDialog(
            party = activeParty,
            initialCash = currentCfg.cashWage,
            initialGold = currentCfg.goldWage,
            playerLevel = profile.level,
            onDismiss = { showWageEditor = false },
            onSave = { cash, gold ->
                showWageEditor = false
                onSetPartyWage(activeParty, cash, gold)
            }
        )
    }

    if (showCreateDialog) {
        CreatePartyDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, tag, ideology, hex ->
                showCreateDialog = false
                onCreateParty(name, tag, ideology, hex)
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = SurfacePanel,
            titleContentColor = TextPrimary,
            title = { Text("Party Wage & Election Rules") },
            text = {
                Text(
                    text = "• Party Leader can set Party Wage ($ & Gold) as high as you want for your party.\n" +
                        "• Other bot/hot parties are capped under 10 Billion (<10B $).\n" +
                        "• Players below Level 40 cannot receive Party Wage.\n" +
                        "• Election voting requires Level 60+.\n" +
                        "• When you make a party, Punjab bots join automatically & you can invite bots to chat anytime!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Close", color = PakistanBright)
                }
            }
        )
    }
}

@Composable
private fun PartyWageEditorDialog(
    party: PartyState,
    initialCash: Long,
    initialGold: Long,
    playerLevel: Long,
    onDismiss: () -> Unit,
    onSave: (Long, Long) -> Unit
) {
    var cashText by remember { mutableStateOf(initialCash.toString()) }
    var goldText by remember { mutableStateOf(initialGold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePanel,
        titleContentColor = TextPrimary,
        title = { Text("SET PARTY WAGE · [${party.tag}]") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Set Wage as high as you want (No 10B limit on your party). Members need Level 40+ to claim Wage (scaled to player level, your LVL: $playerLevel).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = cashText,
                    onValueChange = { raw -> cashText = raw.filter { it.isDigit() }.take(13) },
                    label = { Text("Wage (active) in $") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HoverYellow,
                        unfocusedBorderColor = HairlineBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "10B" to 10_000_000_000L,
                        "50B" to 50_000_000_000L,
                        "100B" to 100_000_000_000L,
                        "500B" to 500_000_000_000L
                    ).forEach { (lbl, amt) ->
                        RrActionButton(
                            onClick = { cashText = amt.toString() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(lbl, fontSize = 11.sp, color = HoverYellow)
                        }
                    }
                }
                OutlinedTextField(
                    value = goldText,
                    onValueChange = { raw -> goldText = raw.filter { it.isDigit() }.take(7) },
                    label = { Text("Party wage in Gold (G)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HoverYellow,
                        unfocusedBorderColor = HairlineBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            RrActionButton(
                onClick = {
                    val cash = cashText.toLongOrNull() ?: initialCash
                    val gold = goldText.toLongOrNull() ?: initialGold
                    onSave(cash, gold)
                },
                containerColor = PakistanBright,
                contentColor = TopBarBg
            ) {
                Text("Save Wage", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun CreatePartyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var ideology by remember { mutableStateOf("National Reform") }
    var selectedHex by remember { mutableStateOf("#16A34A") }
    val palette = listOf("#16A34A", "#F59E0B", "#2563EB", "#DC2626", "#A855F7")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePanel,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("FOUND POLITICAL PARTY", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Starts at 30/60 citizens (Punjab bots join you automatically!). Cost: 200G (Free for Owner).")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Party Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HoverYellow,
                        unfocusedBorderColor = HairlineBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it.take(5).uppercase() },
                    label = { Text("Tag (2-5 chars)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HoverYellow,
                        unfocusedBorderColor = HairlineBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = ideology,
                    onValueChange = { ideology = it },
                    label = { Text("Ideology") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HoverYellow,
                        unfocusedBorderColor = HairlineBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    palette.forEach { hex ->
                        val c = parseHexColor(hex)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedHex == hex) 3.dp else 1.dp,
                                    color = if (selectedHex == hex) HoverYellow else HairlineBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            RrActionButton(
                onClick = { onConfirm(name, tag, ideology, selectedHex) },
                enabled = name.trim().length >= 2 && tag.trim().length >= 2,
                containerColor = PakistanBright,
                contentColor = TopBarBg
            ) {
                Text("Create Party", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
