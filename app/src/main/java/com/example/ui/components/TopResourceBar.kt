package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.RivalRegionsRepository
import com.example.model.CommanderProfile
import com.example.model.RegionState
import com.example.model.SubPanel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.KingGold
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.PixelFontFamily
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg
import com.example.ui.theme.WarCrimson

@Composable
fun TopResourceBar(
    profile: CommanderProfile,
    currentRegion: RegionState?,
    showCompactSubStrip: Boolean,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit,
    onEnergyClick: () -> Unit,
    onOpenSubPanel: (SubPanel) -> Unit,
    onMintGold: () -> Unit,
    onOpenNewGameSetup: () -> Unit,
    onSignOut: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF1B1B1B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Exact Rival Regions Header: [Search]  RIVAL REGIONS  [⋮ Menu]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("top_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Regions",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "RIVAL REGIONS",
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp,
                    color = TextPrimary,
                    maxLines = 1
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("top_more_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Command Menu",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SurfacePanel)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Parliament (${profile.parliamentSeats} Seats)", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = KingGold) },
                            onClick = {
                                menuExpanded = false
                                onOpenSubPanel(SubPanel.PARLIAMENT)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Party Wedge", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = PakistanBright) },
                            onClick = {
                                menuExpanded = false
                                onOpenSubPanel(SubPanel.PARTY)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bot & World Chat", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = CyanAccent) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSubPanel(SubPanel.GLOBAL_CHAT)
                                }
                        )
                        if (profile.kingMode) {
                            DropdownMenuItem(
                                text = { Text("Owner Unlimited Supply", color = KingGold, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = KingGold) },
                                onClick = {
                                    menuExpanded = false
                                    onMintGold()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("New Clean Game", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = TextSecondary) },
                            onClick = {
                                menuExpanded = false
                                onOpenNewGameSetup()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sign Out", color = WarCrimson) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = WarCrimson) },
                            onClick = {
                                menuExpanded = false
                                onSignOut()
                            }
                        )
                    }
                }
            }

            // Secondary compact strip shown when navigating inside sub-tabs (hidden on Home to match RR Home display)
            if (showCompactSubStrip) {
                val isHomeController = currentRegion?.country == "Pakistan" ||
                    currentRegion?.controller.equals(profile.stateName, ignoreCase = true)

                HorizontalDivider(color = HairlineBorder.copy(alpha = 0.5f), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TopBarBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onMapClick() }
                    ) {
                        PakistanFlagBadge(
                            isPakistan = isHomeController,
                            isAutonomy = currentRegion?.autonomy == true,
                            width = 24.dp,
                            height = 15.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentRegion?.name ?: "Punjab - Pakistan",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatBadge(
                            text = if (profile.kingMode) "∞ G" else "${RivalRegionsRepository.formatDots(profile.gold)} G",
                            color = KingGold,
                            onClick = onProfileClick,
                            testTag = "top_gold_stat"
                        )
                        StatBadge(
                            text = if (profile.kingMode) "∞ $" else "${RivalRegionsRepository.formatCompact(profile.money)} $",
                            color = PakistanBright,
                            onClick = onProfileClick,
                            testTag = "top_money_stat"
                        )
                        StatBadge(
                            text = if (profile.kingMode) "∞T" else "${profile.tickets}T",
                            color = CyanAccent,
                            onClick = onMapClick,
                            testTag = "top_tickets_stat"
                        )
                        Surface(
                            color = if (profile.energy <= 20L && !profile.kingMode) WarCrimson.copy(alpha = 0.22f) else SurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (profile.energy <= 20L && !profile.kingMode) WarCrimson else HairlineBorder
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onEnergyClick() }
                                .testTag("top_energy_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Energy",
                                    tint = if (profile.energy <= 20L && !profile.kingMode) WarCrimson else PakistanBright,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = if (profile.kingMode) "∞E" else "${profile.energy}E",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (profile.energy <= 20L && !profile.kingMode) WarCrimson else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = HairlineBorder.copy(alpha = 0.6f), thickness = 1.dp)
        }
    }
}

@Composable
private fun StatBadge(
    text: String,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = SurfacePanel,
        border = BorderStroke(1.dp, HairlineBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 1
            )
        }
    }
}
