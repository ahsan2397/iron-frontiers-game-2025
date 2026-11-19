package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.RivalRegionsRepository
import com.example.model.FactoryType
import com.example.model.RegionState
import com.example.ui.components.RrActionButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepCanvasBg
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.KingGold
import com.example.ui.theme.OilAmber
import com.example.ui.theme.OreSlate
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg
import com.example.ui.theme.UraniumPurple
import com.example.ui.theme.WarCrimson
import com.example.viewmodel.WorldData
import kotlinx.coroutines.delay

@Composable
fun WorkTab(
    world: WorldData,
    currentRegion: RegionState,
    onExplore: () -> Unit,
    onWork: (FactoryType, Boolean) -> Unit,
    onToggleAutoWork: () -> Unit,
    onUpgradeYourFactory: () -> Unit,
    onUseEnergyDrink: () -> Unit
) {
    val profile = world.profile
    val hasPremium = profile.kingMode || profile.premiumActive
    var autoTargetFactory by remember { mutableStateOf(FactoryType.GOLD_MINE) }
    val lowEnergy = profile.energy <= 20L && !profile.kingMode
    val energyFraction = if (profile.kingMode) {
        1f
    } else {
        (profile.energy.toFloat() / profile.maxEnergy.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    }
    val latestEnergy by rememberUpdatedState(profile.energy)
    val latestKingMode by rememberUpdatedState(profile.kingMode)

    // Automatic Mode: runs ONLY when Premium is active (or Owner) & toggled ON
    LaunchedEffect(profile.autoWorkActive, hasPremium, currentRegion.id, autoTargetFactory) {
        if (profile.autoWorkActive && hasPremium) {
            while (true) {
                delay(4500L)
                if (latestKingMode || latestEnergy >= 10L) {
                    onWork(autoTargetFactory, false)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCanvasBg),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text("WORK · ${currentRegion.name.uppercase()}", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text(
                    text = "Track Gold, Oil, Ore & Energy · Manual & Automatic Modes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Live Stockpile & Energy Tracker on Work Screen
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, if (lowEnergy) WarCrimson else HairlineBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("work_stockpile_tracker")
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("YOUR STOCKPILE & ENERGY", style = MaterialTheme.typography.labelMedium, color = HoverYellow)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WorkTrackerPill(
                            label = "GOLD",
                            value = if (profile.kingMode) "∞" else RivalRegionsRepository.formatCompact(profile.gold),
                            icon = Icons.Default.MonetizationOn,
                            accent = KingGold,
                            modifier = Modifier.weight(1f).testTag("work_gold_stat")
                        )
                        WorkTrackerPill(
                            label = "OIL",
                            value = if (profile.kingMode) "∞" else RivalRegionsRepository.formatCompact(profile.oil),
                            icon = Icons.Default.LocalGasStation,
                            accent = OilAmber,
                            modifier = Modifier.weight(1f).testTag("work_oil_stat")
                        )
                        WorkTrackerPill(
                            label = "ORE",
                            value = if (profile.kingMode) "∞" else RivalRegionsRepository.formatCompact(profile.ore),
                            icon = Icons.Default.Terrain,
                            accent = OreSlate,
                            modifier = Modifier.weight(1f).testTag("work_ore_stat")
                        )
                        WorkTrackerPill(
                            label = "ENERGY",
                            value = if (profile.kingMode) "∞E" else "${profile.energy}E",
                            icon = Icons.Default.Bolt,
                            accent = if (lowEnergy) WarCrimson else PakistanBright,
                            modifier = Modifier.weight(1f).testTag("work_energy_stat")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (profile.kingMode) {
                                    "Energy: ∞E · Drinks: ∞"
                                } else {
                                    "Energy: ${profile.energy}E / ${profile.maxEnergy}E · Drinks: ${RivalRegionsRepository.formatDots(profile.energyDrinks)}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (lowEnergy) WarCrimson else TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { energyFraction },
                                color = if (lowEnergy) WarCrimson else PakistanBright,
                                trackColor = TopBarBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                        RrActionButton(
                            onClick = onUseEnergyDrink,
                            containerColor = if (lowEnergy) WarCrimson else PakistanBright,
                            contentColor = if (lowEnergy) TextPrimary else TopBarBg,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("work_refill_energy_btn")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = "Refill Energy", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Refill", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Region Resource Strip + Exploration Limit 0/1500
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("REGION RESERVES (${currentRegion.name.uppercase()})", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        MiniRes("GOLD", "${currentRegion.goldIndex}", KingGold, Modifier.weight(1f))
                        MiniRes("OIL", "${currentRegion.oilIndex}", OilAmber, Modifier.weight(1f))
                        MiniRes("ORE", "${currentRegion.oreIndex}", OreSlate, Modifier.weight(1f))
                        MiniRes("WOOD", "${currentRegion.woodIndex}", PakistanBright, Modifier.weight(1f))
                        MiniRes("URAN", "${currentRegion.uraniumIndex}", UraniumPurple, Modifier.weight(1f))
                        MiniRes("DIAM", String.format("%.2f", currentRegion.diamondIndex / 100.0), CyanAccent, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EXPLORATION LIMIT: ${profile.explorationUsed}/${profile.explorationLimit}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (profile.explorationUsed.toFloat() / profile.explorationLimit.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f) },
                                color = HoverYellow,
                                trackColor = TopBarBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        RrActionButton(
                            onClick = onExplore,
                            accentBorder = HoverYellow,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("explore_resources_btn")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Explore Region", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Explore", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Work Modes Card: Manual Quick Shift, Automatic Mode Toggle (Premium Only) + Auto Target, Your Factory Upgrade
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, if (profile.autoWorkActive && hasPremium) WarCrimson else HairlineBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WORK MODES", style = MaterialTheme.typography.labelMedium, color = HoverYellow)
                        Surface(
                            color = when {
                                !hasPremium -> SurfaceElevated
                                profile.autoWorkActive -> WarCrimson.copy(alpha = 0.22f)
                                else -> SurfaceElevated
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    !hasPremium -> TextSecondary
                                    profile.autoWorkActive -> WarCrimson
                                    else -> PakistanBright
                                }
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when {
                                    !hasPremium -> "NO PREMIUM · CLICK WORK"
                                    profile.autoWorkActive -> "AUTO RUNNING (${autoTargetFactory.resourceName.uppercase()})"
                                    else -> "PREMIUM AUTO READY"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    !hasPremium -> TextSecondary
                                    profile.autoWorkActive -> WarCrimson
                                    else -> PakistanBright
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Select which factory Automatic Mode farms
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(FactoryType.entries) { factory ->
                            val selected = factory == autoTargetFactory
                            Surface(
                                color = if (selected) HoverYellow.copy(alpha = 0.22f) else SurfaceElevated,
                                border = BorderStroke(1.dp, if (selected) HoverYellow else HairlineBorder),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { autoTargetFactory = factory }
                                    .testTag("auto_target_${factory.name.lowercase()}")
                            ) {
                                Text(
                                    text = "Target: ${factory.resourceName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) HoverYellow else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Manual Work Button (always active)
                        RrActionButton(
                            onClick = { onWork(autoTargetFactory, false) },
                            containerColor = PakistanBright,
                            contentColor = TopBarBg,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("manual_mode_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Manual Work", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Click Work (-10E)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Red Automatic Mode Toggle Button (Premium ON required!)
                        RrActionButton(
                            onClick = onToggleAutoWork,
                            containerColor = when {
                                !hasPremium -> SurfaceElevated
                                profile.autoWorkActive -> WarCrimson
                                else -> WarCrimson.copy(alpha = 0.85f)
                            },
                            contentColor = if (hasPremium) TextPrimary else TextSecondary,
                            accentBorder = when {
                                !hasPremium -> HairlineBorder
                                profile.autoWorkActive -> HoverYellow
                                else -> WarCrimson
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("automatic_mode_btn")
                        ) {
                            Icon(
                                imageVector = when {
                                    !hasPremium -> Icons.Default.Lock
                                    profile.autoWorkActive -> Icons.Default.Stop
                                    else -> Icons.Default.Autorenew
                                },
                                contentDescription = "Automatic Mode",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = when {
                                    !hasPremium -> "Auto (Premium)"
                                    profile.autoWorkActive -> "Stop Auto"
                                    else -> "Automatic mode"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Your Factory (-500G -10.000.000 $)
                    RrActionButton(
                        onClick = onUpgradeYourFactory,
                        containerColor = SurfaceElevated,
                        contentColor = KingGold,
                        accentBorder = KingGold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("your_factory_btn")
                    ) {
                        Text(
                            text = "Your Factory (Lvl ${profile.factoryLevel}) · Upgrade: -500G  -10.000.000 $",
                            style = MaterialTheme.typography.labelSmall,
                            color = KingGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Factory List with Manual Work & Boosted Work +95% +1G
        items(FactoryType.entries) { factory ->
            val currentStockpile = when (factory) {
                FactoryType.GOLD_MINE -> if (profile.kingMode) "∞ G" else "${RivalRegionsRepository.formatDots(profile.gold)} G"
                FactoryType.OIL_RIG -> if (profile.kingMode) "∞ bbl" else "${RivalRegionsRepository.formatDots(profile.oil)} bbl"
                FactoryType.ORE_QUARRY -> if (profile.kingMode) "∞ t" else "${RivalRegionsRepository.formatDots(profile.ore)} t"
                FactoryType.WOOD_MILL -> if (profile.kingMode) "∞ pcs" else "${RivalRegionsRepository.formatDots(profile.wood)} pcs"
                FactoryType.URANIUM_PLANT -> if (profile.kingMode) "∞" else RivalRegionsRepository.formatDots(profile.uranium)
                FactoryType.DIAMOND_WORKS -> if (profile.kingMode) "∞" else String.format("%.2f", profile.diamonds / 100.0)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, if (factory == autoTargetFactory && profile.autoWorkActive) HoverYellow else HairlineBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Factory, contentDescription = factory.title, tint = HoverYellow, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(factory.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(
                                    text = "Produces ${factory.resourceName} · Bonus x${1 + profile.factoryLevel * 0.25}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                        Surface(
                            color = SurfaceElevated,
                            border = BorderStroke(1.dp, HairlineBorder),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentStockpile,
                                style = MaterialTheme.typography.labelSmall,
                                color = KingGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RrActionButton(
                            onClick = { onWork(factory, false) },
                            containerColor = PakistanBright,
                            contentColor = TopBarBg,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("work_green_${factory.name.lowercase()}")
                        ) {
                            Text("Work (-10E)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }

                        RrActionButton(
                            onClick = { onWork(factory, true) },
                            containerColor = SteelBlue,
                            contentColor = TextPrimary,
                            modifier = Modifier
                                .weight(1.25f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("work_blue_${factory.name.lowercase()}")
                        ) {
                            Text("Work +95% +1G (-50E)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkTrackerPill(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceElevated,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, color = accent, maxLines = 1)
        }
    }
}

@Composable
private fun MiniRes(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceElevated,
        border = BorderStroke(1.dp, HairlineBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}
