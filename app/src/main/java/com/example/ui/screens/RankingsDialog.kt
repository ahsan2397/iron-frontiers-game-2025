package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RankedPlayer
import com.example.data.RivalRegionsRepository
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

@Composable
fun RankingsDialog(
    roster: List<RankedPlayer>,
    currentDay: Int,
    onSimulateNextDay: () -> Unit,
    onDismiss: () -> Unit
) {
    var modeTab by remember { mutableIntStateOf(0) } // 0 = Level, 1 = Rich List (Money masked)
    var inspectedPlayer by remember { mutableStateOf<RankedPlayer?>(null) }

    val sortedPlayers = remember(roster, modeTab) {
        if (modeTab == 0) {
            roster.sortedByDescending { it.level }
        } else {
            roster.sortedByDescending { it.hiddenWealthScore }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = DeepCanvasBg,
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 18.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = HoverYellow, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PLAYER RANKINGS",
                                fontFamily = PixelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Day $currentDay · ${roster.size} Active Players (New daily arrivals)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close Rankings", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RrActionButton(
                        onClick = { modeTab = 0 },
                        containerColor = if (modeTab == 0) SteelBlue else SurfacePanel,
                        contentColor = TextPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ranking_tab_level")
                    ) {
                        Text("By Level", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    RrActionButton(
                        onClick = { modeTab = 1 },
                        containerColor = if (modeTab == 1) PakistanEmerald else SurfacePanel,
                        contentColor = TextPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ranking_tab_money")
                    ) {
                        Text("By Money (Rich)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    RrActionButton(
                        onClick = onSimulateNextDay,
                        containerColor = SurfaceElevated,
                        contentColor = HoverYellow,
                        accentBorder = HoverYellow,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("simulate_new_day_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("New Day", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (modeTab == 1) {
                        "Ranked by wealth — exact money amounts stay secret. Tap any player to inspect Strength, Endurance, Damage & Level."
                    } else {
                        "Tap any player to inspect their Strength, Endurance, Damage & Level."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(sortedPlayers, key = { _, p -> p.id }) { idx, player ->
                        val rank = idx + 1
                        Surface(
                            color = if (player.isCurrentUser) PakistanEmerald.copy(alpha = 0.16f) else SurfacePanel,
                            border = BorderStroke(
                                1.dp,
                                when {
                                    player.isCurrentUser -> PakistanBright
                                    rank == 1 -> KingGold
                                    else -> HairlineBorder
                                }
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { inspectedPlayer = player }
                                .testTag("ranking_row_${player.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#$rank",
                                    fontFamily = PixelFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (rank <= 3) HoverYellow else TextSecondary,
                                    modifier = Modifier.width(34.dp)
                                )
                                PakistanFlagBadge(
                                    isPakistan = player.stateName.equals("Pakistan", ignoreCase = true) ||
                                        player.regionName.contains("Pakistan", ignoreCase = true),
                                    width = 24.dp,
                                    height = 15.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "[${player.partyTag}] ${player.name}${if (player.isCurrentUser) " (YOU)" else ""}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (player.isCurrentUser) PakistanBright else TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${player.regionName} · Day ${player.joinedDay}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (modeTab == 0) {
                                    Text(
                                        text = "LVL ${player.level}",
                                        fontFamily = PixelFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = HoverYellow
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = player.wealthTierLabel(rank),
                                            fontFamily = PixelFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (rank <= 3) KingGold else PakistanBright
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("$ Hidden", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val target = inspectedPlayer
    if (target != null) {
        AlertDialog(
            onDismissRequest = { inspectedPlayer = null },
            containerColor = SurfacePanel,
            titleContentColor = TextPrimary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PakistanFlagBadge(
                        isPakistan = target.regionName.contains("Pakistan", ignoreCase = true),
                        width = 28.dp,
                        height = 18.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("[${target.partyTag}] ${target.name}", style = MaterialTheme.typography.titleLarge)
                        Text("${target.regionName} (${target.stateName})", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InspectRow(Icons.Default.EmojiEvents, "Level", "LVL ${target.level}", HoverYellow)
                            HorizontalDivider(color = HairlineBorder.copy(alpha = 0.4f))
                            InspectRow(Icons.Default.FitnessCenter, "Strength", target.strength.toString(), WarCrimson)
                            HorizontalDivider(color = HairlineBorder.copy(alpha = 0.4f))
                            InspectRow(Icons.Default.WaterDrop, "Endurance", target.endurance.toString(), PakistanBright)
                            HorizontalDivider(color = HairlineBorder.copy(alpha = 0.4f))
                            InspectRow(Icons.Default.MilitaryTech, "Total Damage", RivalRegionsRepository.formatDots(target.totalDamage), CyanAccent)
                            HorizontalDivider(color = HairlineBorder.copy(alpha = 0.4f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Money Balance", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("Hidden (Wealthy)", style = MaterialTheme.typography.bodySmall, color = KingGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "Party: ${target.partyName} · Joined Day ${target.joinedDay}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedPlayer = null }) {
                    Text("Close", color = PakistanBright, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun InspectRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
        Text(
            text = value,
            fontFamily = PixelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = accent
        )
    }
}
