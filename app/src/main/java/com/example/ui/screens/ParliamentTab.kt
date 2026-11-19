package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LawStatus
import com.example.model.ParliamentLaw
import com.example.model.RegionState
import com.example.model.UserVote
import com.example.ui.components.RrActionButton
import com.example.ui.theme.DeepCanvasBg
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KingGold
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.PakistanEmerald
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg
import com.example.ui.theme.WarCrimson
import com.example.viewmodel.WorldData
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val ChamberCanvasBg = Color(0xFF2E2E2E)
private val ChamberHeaderBg = Color(0xFF242424)
private val LawPanelBg = Color(0xFF262626)
private val LogButtonBg = Color(0xFF2B2B2B)
private val EmptySeatFill = Color(0xFF2A2A2A)
private val EmptySeatStroke = Color(0xFF474747)
private val RivalCancelRed = Color(0xFFD63626)

@Composable
fun ParliamentTab(
    world: WorldData,
    onVote: (law: ParliamentLaw, voteYes: Boolean, royalDecree: Boolean) -> Unit,
    onProposeLaw: (title: String, category: String, targetSeatCount: Long) -> Unit,
    onCancelLaw: (law: ParliamentLaw) -> Unit = {},
    onWinnerTakesAll: () -> Unit,
    onBack: () -> Unit = {}
) {
    var showProposeDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showRegionDropdown by remember { mutableStateOf(false) }
    var showHistoryLog by remember { mutableStateOf(false) }
    var showUnacceptedLog by remember { mutableStateOf(false) }
    var selectedChamberRegionId by remember(world.profile.currentRegionId) {
        mutableStateOf(world.profile.currentRegionId)
    }

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMs = System.currentTimeMillis()
        }
    }

    val totalSeats = world.profile.parliamentSeats.coerceIn(10L, 200L).toInt()
    val majority = (totalSeats / 2) + 1

    val activeRegion = remember(world.regions, selectedChamberRegionId, world.profile.currentRegionId) {
        world.regions.firstOrNull { it.id == selectedChamberRegionId }
            ?: world.regions.firstOrNull { it.id == world.profile.currentRegionId }
            ?: world.regions.firstOrNull()
    }

    val userParty = remember(world.parties, world.profile.partyId) {
        world.parties.firstOrNull { it.userMember } ?: world.parties.firstOrNull { it.id == world.profile.partyId }
    }

    val seatColors = remember(world.parties, totalSeats) {
        val list = mutableListOf<Color>()
        world.parties.forEach { party ->
            val c = parseHexColor(party.colorHex)
            repeat(party.seats.toInt().coerceAtLeast(0)) {
                if (list.size < totalSeats) list.add(c)
            }
        }
        while (list.size < totalSeats) list.add(Color.Transparent)
        list
    }

    val userSeatHighlightIndex = remember(world.parties, userParty, seatColors) {
        if (userParty == null) -1
        else {
            var idx = 0
            var found = -1
            for (party in world.parties) {
                val count = party.seats.toInt().coerceAtLeast(0)
                if (party.id == userParty.id && count > 0) {
                    found = (idx + count - 1).coerceAtMost(seatColors.lastIndex)
                    break
                }
                idx += count
            }
            found
        }
    }

    val pendingLaws = remember(world.laws) {
        world.laws.filter { it.status == LawStatus.VOTING.name }
    }
    val passedLaws = remember(world.laws) {
        world.laws.filter { it.status == LawStatus.PASSED.name }
    }
    val rejectedLaws = remember(world.laws) {
        world.laws.filter { it.status == LawStatus.REJECTED.name }
    }
    val myPendingLaw = remember(pendingLaws, userParty) {
        val tag = userParty?.tag
        pendingLaws.firstOrNull { it.proposerParty == tag } ?: pendingLaws.firstOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChamberCanvasBg)
    ) {
        // Rival Regions Style Top Header: <-  PARLIAMENT  (?)
        Surface(
            color = ChamberHeaderBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("parliament_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "PARLIAMENT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier.testTag("parliament_help_btn")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = "Parliament Rules",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = HairlineBorder, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ChamberCanvasBg),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hemicycle Chamber Canvas (Rounded square seats + center MP silhouette)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp)
                            .testTag("parliament_60_seats_hemicycle"),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val rowsCount = if (totalSeats >= 48) 4 else 3
                            val rowSpec = if (rowsCount == 4) {
                                val r1 = (totalSeats * 0.17f).toInt().coerceAtLeast(4)
                                val r2 = (totalSeats * 0.23f).toInt().coerceAtLeast(5)
                                val r3 = (totalSeats * 0.28f).toInt().coerceAtLeast(6)
                                val r4 = (totalSeats - r1 - r2 - r3).coerceAtLeast(6)
                                listOf(
                                    r1 to size.minDimension * 0.50f,
                                    r2 to size.minDimension * 0.64f,
                                    r3 to size.minDimension * 0.78f,
                                    r4 to size.minDimension * 0.92f
                                )
                            } else {
                                val r1 = (totalSeats * 0.23f).toInt().coerceAtLeast(3)
                                val r2 = (totalSeats * 0.34f).toInt().coerceAtLeast(4)
                                val r3 = (totalSeats - r1 - r2).coerceAtLeast(5)
                                listOf(
                                    r1 to size.minDimension * 0.48f,
                                    r2 to size.minDimension * 0.68f,
                                    r3 to size.minDimension * 0.88f
                                )
                            }

                            val originX = size.width / 2f
                            val originY = size.height - 18.dp.toPx()
                            val seatPx = when {
                                totalSeats > 110 -> 7.5.dp.toPx()
                                totalSeats > 75 -> 9.5.dp.toPx()
                                else -> 11.dp.toPx()
                            }
                            val half = seatPx / 2f
                            val corner = CornerRadius(2.2.dp.toPx(), 2.2.dp.toPx())
                            var seatIndex = 0

                            rowSpec.forEach { (count, radius) ->
                                for (i in 0 until count) {
                                    val fraction = if (count == 1) 0.5f else i.toFloat() / (count - 1).toFloat()
                                    val angleRad = PI - fraction * PI
                                    val cx = originX + (radius * cos(angleRad)).toFloat()
                                    val cy = originY - (radius * sin(angleRad)).toFloat()
                                    val rotDeg = (90.0 - Math.toDegrees(angleRad)).toFloat()

                                    val seatColor = seatColors.getOrElse(seatIndex) { Color.Transparent }
                                    val isFilled = seatColor != Color.Transparent
                                    val isPlayerSeat = seatIndex == userSeatHighlightIndex

                                    rotate(degrees = rotDeg, pivot = Offset(cx, cy)) {
                                        val topLeft = Offset(cx - half, cy - half)
                                        val rectSize = Size(seatPx, seatPx)
                                        drawRoundRect(
                                            color = if (isFilled) seatColor else EmptySeatFill,
                                            topLeft = topLeft,
                                            size = rectSize,
                                            cornerRadius = corner
                                        )
                                        drawRoundRect(
                                            color = if (isPlayerSeat) KingGold else if (isFilled) Color(0xFF1A1A1A) else EmptySeatStroke,
                                            topLeft = topLeft,
                                            size = rectSize,
                                            cornerRadius = corner,
                                            style = Stroke(width = if (isPlayerSeat) 1.5.dp.toPx() else 1.dp.toPx())
                                        )
                                        if (isFilled && isPlayerSeat) {
                                            for (lineIdx in 1..2) {
                                                val ly = topLeft.y + seatPx * (lineIdx / 3f)
                                                drawLine(
                                                    color = Color(0xFF1A1A1A),
                                                    start = Offset(topLeft.x + 1.5f, ly),
                                                    end = Offset(topLeft.x + seatPx - 1.5f, ly),
                                                    strokeWidth = 1.dp.toPx()
                                                )
                                            }
                                        }
                                    }
                                    seatIndex++
                                }
                            }

                            // Crisp white MP / Speaker figure centered in the chamber floor
                            val figBaseY = originY - 12.dp.toPx()
                            val headR = 3.2.dp.toPx()
                            drawCircle(
                                color = Color.White,
                                radius = headR,
                                center = Offset(originX, figBaseY - 18.dp.toPx())
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(originX - 4.dp.toPx(), figBaseY - 14.dp.toPx()),
                                size = Size(8.dp.toPx(), 10.dp.toPx()),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(originX - 2.6.dp.toPx(), figBaseY - 4.5.dp.toPx()),
                                size = Size(2.1.dp.toPx(), 6.5.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(originX + 0.5.dp.toPx(), figBaseY - 4.5.dp.toPx()),
                                size = Size(2.1.dp.toPx(), 6.5.dp.toPx()),
                                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                            )
                        }

                        Text(
                            text = "$totalSeats SEATS · MAJ $majority",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = world.parties.joinToString("  ·  ") { "${it.tag}: ${it.seats}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Parliament Selector Dropdown Bar ("State parliament: Punjab" / "Autonomy parliament: ...")
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    val regionLabel = activeRegion?.name?.substringBefore(" - ") ?: "Punjab"
                    val parlKind = if (activeRegion?.autonomy == true) "Autonomy parliament" else "State parliament"

                    Surface(
                        color = ChamberHeaderBg,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, HairlineBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { showRegionDropdown = true }
                            .testTag("parliament_region_selector")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                                    .border(1.dp, TextSecondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "$parlKind: $regionLabel",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Parliament Region",
                                tint = TextPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showRegionDropdown,
                        onDismissRequest = { showRegionDropdown = false },
                        modifier = Modifier
                            .background(SurfacePanel)
                            .width(290.dp)
                    ) {
                        val sortedRegions = remember(world.regions, world.profile.currentRegionId) {
                            world.regions.sortedByDescending { it.id == world.profile.currentRegionId }.take(15)
                        }
                        sortedRegions.forEach { reg ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${reg.name} (${reg.controller})",
                                        color = if (reg.id == activeRegion?.id) PakistanBright else TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedChamberRegionId = reg.id
                                    showRegionDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Rival Regions Full-Width Red "Cancel my law" or Green "Propose new law" Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (myPendingLaw != null) {
                        Button(
                            onClick = { onCancelLaw(myPendingLaw) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RivalCancelRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("cancel_my_law_btn")
                        ) {
                            Text(
                                text = "Cancel my law",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RrActionButton(
                                onClick = { showProposeDialog = true },
                                containerColor = PakistanEmerald,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("propose_law_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Propose bill", fontWeight = FontWeight.Bold)
                            }
                            RrActionButton(
                                onClick = onWinnerTakesAll,
                                containerColor = SurfaceElevated,
                                contentColor = HoverYellow,
                                accentBorder = HoverYellow,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("winner_takes_all_election_btn")
                            ) {
                                Icon(Icons.Default.HowToVote, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Election ($totalSeats)", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { showProposeDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PakistanEmerald,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("propose_law_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Propose new law",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        RrActionButton(
                            onClick = onWinnerTakesAll,
                            containerColor = SurfaceElevated,
                            contentColor = HoverYellow,
                            accentBorder = HoverYellow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("winner_takes_all_election_btn")
                        ) {
                            Icon(Icons.Default.HowToVote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hold Election: Winner Takes ALL $totalSeats Seats", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // "Pending laws" Box matching screenshot layout
            item {
                Surface(
                    color = LawPanelBg,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFF3A3A3A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Pending laws",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )

                        if (pendingLaws.isEmpty()) {
                            Text(
                                text = "No pending bills in parliament. Tap \"Propose new law\" to submit a bill.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        } else {
                            pendingLaws.forEachIndexed { idx, law ->
                                if (idx > 0) {
                                    HorizontalDivider(color = HairlineBorder, thickness = 1.dp)
                                }
                                PendingLawItem(
                                    law = law,
                                    totalSeats = totalSeats,
                                    kingMode = world.profile.kingMode,
                                    nowMs = nowMs,
                                    onVote = onVote,
                                    onCancel = onCancelLaw
                                )
                            }
                        }
                    }
                }
            }

            // "Laws history log" Full-Width Button + Expandable Passed Laws
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = LogButtonBg,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFF3E3E3E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { showHistoryLog = !showHistoryLog }
                            .testTag("laws_history_log_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Laws history log" + if (passedLaws.isNotEmpty()) " (${passedLaws.size})" else "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (showHistoryLog) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showHistoryLog) {
                        LawArchiveList(
                            laws = passedLaws,
                            totalSeats = totalSeats,
                            emptyText = "No passed laws yet."
                        )
                    }

                    // "Unaccepted laws" Full-Width Button + Expandable Rejected Laws
                    Surface(
                        color = LogButtonBg,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFF3E3E3E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { showUnacceptedLog = !showUnacceptedLog }
                            .testTag("unaccepted_laws_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Unaccepted laws" + if (rejectedLaws.isNotEmpty()) " (${rejectedLaws.size})" else "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (showUnacceptedLog) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showUnacceptedLog) {
                        LawArchiveList(
                            laws = rejectedLaws,
                            totalSeats = totalSeats,
                            emptyText = "No rejected or unaccepted laws."
                        )
                    }
                }
            }
        }
    }

    if (showProposeDialog) {
        ProposeLawDialog(
            currentSeats = totalSeats.toLong(),
            regionName = activeRegion?.name?.substringBefore(" - ") ?: "Punjab",
            onDismiss = { showProposeDialog = false },
            onConfirm = { title, category, targetSeats ->
                showProposeDialog = false
                onProposeLaw(title, category, targetSeats)
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = SurfacePanel,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("PARLIAMENT CHAMBER", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "• Each square in the hemicycle represents 1 parliamentary seat colored by party.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Passing a bill requires a majority ($majority/$totalSeats YES votes). Bills vote for 24h unless resolved early.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• State Leaders & Owners can propose bills, pass constitutional seat reforms (40–100 seats), or issue Royal Decrees.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it", color = PakistanBright, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun PendingLawItem(
    law: ParliamentLaw,
    totalSeats: Int,
    kingMode: Boolean,
    nowMs: Long,
    onVote: (law: ParliamentLaw, voteYes: Boolean, royalDecree: Boolean) -> Unit,
    onCancel: (law: ParliamentLaw) -> Unit
) {
    val yesRatio = (law.yesVotes.toFloat() / totalSeats.toFloat()).coerceIn(0f, 1f)
    val createdMs = (law.createdAt?.seconds ?: (nowMs / 1000L)) * 1000L
    val remainingSec = (((createdMs + 24L * 3600L * 1000L) - nowMs) / 1000L).coerceAtLeast(0L)
    val hh = remainingSec / 3600L
    val mm = (remainingSec % 3600L) / 60L
    val ss = remainingSec % 60L
    val timerLabel = String.format("%02d:%02d:%02d", hh, mm, ss)

    val lawIcon = when {
        law.category.contains("Const", ignoreCase = true) -> Icons.Default.Gavel
        law.category.contains("Econ", ignoreCase = true) -> Icons.Default.AttachMoney
        else -> Icons.Default.ViewInAr
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("law_card_${law.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = lawIcon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier
                    .padding(top = 2.dp, end = 10.dp)
                    .size(30.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${law.title}:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (law.targetSeatCount > 0L) {
                    Text(
                        text = "Constitutional Reform → ${law.targetSeatCount} seats",
                        style = MaterialTheme.typography.labelSmall,
                        color = HoverYellow
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Rival Regions style resource/vote summary row with colored checkmarks
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResourceCheckBadge(text = "${law.yesVotes} YES", color = TextPrimary, checkColor = TextPrimary)
                    ResourceCheckBadge(text = "100 G", color = HoverYellow, checkColor = PakistanBright)
                    ResourceCheckBadge(text = "[${law.proposerParty}]", color = TextSecondary, checkColor = PakistanBright)
                    ResourceCheckBadge(text = "${law.noVotes} NO", color = RivalCancelRed, checkColor = RivalCancelRed)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timerLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel law",
                        tint = TextPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onCancel(law) }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${law.yesVotes + law.noVotes}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { yesRatio },
            color = PakistanBright,
            trackColor = WarCrimson.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )

        val alreadyVoted = law.userVoted != UserVote.NONE.name
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            RrActionButton(
                onClick = { onVote(law, true, false) },
                enabled = !alreadyVoted,
                containerColor = PakistanBright,
                contentColor = TopBarBg,
                modifier = Modifier
                    .weight(1f)
                    .testTag("vote_yes_${law.id}")
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (law.userVoted == UserVote.YES.name) "Voted YES" else "Vote YES", fontWeight = FontWeight.Bold)
            }
            RrActionButton(
                onClick = { onVote(law, false, false) },
                enabled = !alreadyVoted,
                containerColor = WarCrimson,
                contentColor = TextPrimary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("vote_no_${law.id}")
            ) {
                Icon(Icons.Default.ThumbDown, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (law.userVoted == UserVote.NO.name) "Voted NO" else "Vote NO", fontWeight = FontWeight.Bold)
            }
        }

        if (kingMode) {
            RrActionButton(
                onClick = { onVote(law, true, true) },
                containerColor = KingGold,
                contentColor = TopBarBg,
                accentBorder = HoverYellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("royal_decree_${law.id}")
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Royal Decree: Instant Pass", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ResourceCheckBadge(
    text: String,
    color: Color,
    checkColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = checkColor,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun LawArchiveList(
    laws: List<ParliamentLaw>,
    totalSeats: Int,
    emptyText: String
) {
    Surface(
        color = LawPanelBg,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, HairlineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (laws.isEmpty()) {
                Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            } else {
                laws.forEach { law ->
                    val statusColor = if (law.status == LawStatus.PASSED.name) PakistanBright else WarCrimson
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("law_card_${law.id}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(law.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text(
                                text = "${law.category} · [${law.proposerParty}] · YES ${law.yesVotes}/$totalSeats · NO ${law.noVotes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = law.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProposeLawDialog(
    currentSeats: Long,
    regionName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("House fund, $regionName (+1)") }
    var category by remember { mutableStateOf("Buildings") }
    var targetSeatCount by remember { mutableLongStateOf(0L) }
    val seatOptions = listOf(0L to "Keep $currentSeats", 40L to "40", 60L to "60", 80L to "80", 100L to "100")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePanel,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("PROPOSE PARLIAMENT BILL", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Law Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HoverYellow,
                        unfocusedBorderColor = HairlineBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Buildings / Economy / Constitution)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HoverYellow,
                        unfocusedBorderColor = HairlineBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Text("Optional: Change Parliament Seats by Law", style = MaterialTheme.typography.labelSmall, color = HoverYellow)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    seatOptions.forEach { (value, label) ->
                        FilterChip(
                            selected = targetSeatCount == value,
                            onClick = { targetSeatCount = value },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakistanBright.copy(alpha = 0.25f),
                                selectedLabelColor = PakistanBright
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            RrActionButton(
                onClick = { onConfirm(title, category, targetSeatCount) },
                enabled = title.trim().length >= 3,
                containerColor = PakistanBright,
                contentColor = TopBarBg
            ) {
                Text("Submit to $currentSeats Seats", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
