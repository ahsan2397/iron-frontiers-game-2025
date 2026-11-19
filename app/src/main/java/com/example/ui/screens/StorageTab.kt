package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.RivalRegionsRepository
import com.example.model.WeaponType
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

@Composable
fun StorageTab(
    world: WorldData,
    onUseEnergyDrink: () -> Unit,
    onBuyEnergyWithGold: (Long) -> Unit,
    onMintUnlimitedSupply: () -> Unit,
    onCraftWeapon: (WeaponType, Long) -> Unit,
    onMarketTrade: (key: String, label: String, amount: Long, cashDelta: Long, isBuy: Boolean) -> Unit
) {
    val profile = world.profile
    val lowEnergy = profile.energy <= 20L && !profile.kingMode
    val energyFraction = if (profile.kingMode) {
        1f
    } else {
        (profile.energy.toFloat() / profile.maxEnergy.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
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
                Text("STORAGE & RESOURCE TRACKER", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text(
                    text = "Track Gold, Oil, Ore, Energy, arsenal & live market",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        if (profile.kingMode) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, KingGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = KingGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("OWNER UNLIMITED EVERYTHING ACTIVE", style = MaterialTheme.typography.titleMedium, color = KingGold)
                        }
                        RrActionButton(
                            onClick = onMintUnlimitedSupply,
                            containerColor = KingGold,
                            contentColor = TopBarBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("storage_owner_supply_btn")
                        ) {
                            Text("REFILL +100K GOLD, OIL, ORE, WOOD & TROOPS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Core Resource Tracker: Gold, Oil, Ore & Energy
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, if (lowEnergy) WarCrimson else HairlineBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("storage_core_tracker_card")
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CORE RESERVES (GOLD · OIL · ORE · ENERGY)", style = MaterialTheme.typography.labelMedium, color = HoverYellow)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        CoreResourceTile(
                            label = "GOLD",
                            value = if (profile.kingMode) "∞ G" else "${RivalRegionsRepository.formatDots(profile.gold)} G",
                            icon = Icons.Default.MonetizationOn,
                            accent = KingGold,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("storage_gold_tile")
                        )
                        CoreResourceTile(
                            label = "OIL",
                            value = if (profile.kingMode) "∞ bbl" else "${RivalRegionsRepository.formatDots(profile.oil)} bbl",
                            icon = Icons.Default.LocalGasStation,
                            accent = OilAmber,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("storage_oil_tile")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        CoreResourceTile(
                            label = "ORE",
                            value = if (profile.kingMode) "∞ t" else "${RivalRegionsRepository.formatDots(profile.ore)} t",
                            icon = Icons.Default.Terrain,
                            accent = OreSlate,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("storage_ore_tile")
                        )
                        CoreResourceTile(
                            label = "ENERGY",
                            value = if (profile.kingMode) "∞E" else "${profile.energy}E / ${profile.maxEnergy}E",
                            icon = Icons.Default.Bolt,
                            accent = if (lowEnergy) WarCrimson else PakistanBright,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("storage_energy_tile")
                        )
                    }

                    // Live Energy Gauge + Refill Action
                    Surface(
                        color = SurfaceElevated,
                        border = BorderStroke(1.dp, if (lowEnergy) WarCrimson else HairlineBorder),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (profile.kingMode) "ENERGY RESERVE: ∞E" else "ENERGY: ${profile.energy}E / ${profile.maxEnergy}E",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (lowEnergy) WarCrimson else PakistanBright
                                    )
                                    Text(
                                        text = "Energy Drinks: ${if (profile.kingMode) "∞" else RivalRegionsRepository.formatDots(profile.energyDrinks)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                RrActionButton(
                                    onClick = onUseEnergyDrink,
                                    containerColor = if (lowEnergy) WarCrimson else PakistanBright,
                                    contentColor = if (lowEnergy) TextPrimary else TopBarBg,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("storage_drink_energy_btn")
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = "Drink Energy", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DRINK", fontWeight = FontWeight.Bold)
                                }
                            }
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
                    }
                }
            }
        }

        // Secondary Stockpiles & Military Hardware
        item {
            Text("STRATEGIC MATERIALS & ARSENAL", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StorageTile("TANKS", if (profile.kingMode) "∞ pcs" else "${RivalRegionsRepository.formatDots(profile.tanks)} pcs", PakistanBright, Modifier.weight(1f))
                    StorageTile("WOOD", if (profile.kingMode) "∞ pcs" else "${RivalRegionsRepository.formatDots(profile.wood)} pcs", PakistanBright, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StorageTile("URANIUM", if (profile.kingMode) "∞" else RivalRegionsRepository.formatDots(profile.uranium), UraniumPurple, Modifier.weight(1f))
                    StorageTile("DIAMONDS", if (profile.kingMode) "∞" else String.format("%.2f", profile.diamonds / 100.0), CyanAccent, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StorageTile("BOMBERS", if (profile.kingMode) "∞ pcs" else "${profile.bombers} pcs", SteelBlue, Modifier.weight(1f))
                    StorageTile("MISSILES", if (profile.kingMode) "∞ pcs" else "${profile.missiles} pcs", WarCrimson, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StorageTile("ENERGY DRINK", if (profile.kingMode) "∞" else RivalRegionsRepository.formatDots(profile.energyDrinks), HoverYellow, Modifier.weight(1f))
                    StorageTile("TICKETS", if (profile.kingMode) "∞" else "${profile.tickets} pcs", CyanAccent, Modifier.weight(1f))
                }
            }
        }

        // Buy Energy with Gold (10 Energy = 1 Gold)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, KingGold.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("storage_buy_energy_gold_card")
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ENERGY EXCHANGE · 10 ENERGY = 1 GOLD",
                                style = MaterialTheme.typography.titleMedium,
                                color = KingGold
                            )
                            Text(
                                text = "Buy instant Energy directly from Storage Market using Gold",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            color = SurfaceElevated,
                            border = BorderStroke(1.dp, KingGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (profile.kingMode) "∞ G" else "${RivalRegionsRepository.formatDots(profile.gold)} G",
                                style = MaterialTheme.typography.labelSmall,
                                color = KingGold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            1L to "+10 E\n(-1 G)",
                            10L to "+100 E\n(-10 G)",
                            30L to "+300 E\n(-30 G)",
                            100L to "+1.000 E\n(-100 G)"
                        ).forEach { (goldCost, label) ->
                            RrActionButton(
                                onClick = { onBuyEnergyWithGold(goldCost) },
                                containerColor = SurfaceElevated,
                                contentColor = PakistanBright,
                                accentBorder = KingGold,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("buy_energy_${goldCost}g_btn")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Market: Buy AND Sell ALL resources including Gold, Oil, Ore, Wood & Tickets
        item {
            Text("COMMODITY MARKET (BUY & SELL)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MarketTradeRow("Gold", "50 G", "50.000.000 $",
                    onBuy = { onMarketTrade("gold", "Gold", 50L, 50_000_000L, true) },
                    onSell = { onMarketTrade("gold", "Gold", 50L, 45_000_000L, false) }
                )
                MarketTradeRow("Oil", "100 bbl", "1.500.000 $",
                    onBuy = { onMarketTrade("oil", "Oil", 100L, 1_500_000L, true) },
                    onSell = { onMarketTrade("oil", "Oil", 100L, 1_350_000L, false) }
                )
                MarketTradeRow("Ore", "100 t", "1.400.000 $",
                    onBuy = { onMarketTrade("ore", "Ore", 100L, 1_400_000L, true) },
                    onSell = { onMarketTrade("ore", "Ore", 100L, 1_250_000L, false) }
                )
                MarketTradeRow("Wood", "100 pcs", "1.200.000 $",
                    onBuy = { onMarketTrade("wood", "Wood", 100L, 1_200_000L, true) },
                    onSell = { onMarketTrade("wood", "Wood", 100L, 1_050_000L, false) }
                )
                MarketTradeRow("Uranium", "50 u", "3.200.000 $",
                    onBuy = { onMarketTrade("uranium", "Uranium", 50L, 3_200_000L, true) },
                    onSell = { onMarketTrade("uranium", "Uranium", 50L, 2_900_000L, false) }
                )
                MarketTradeRow("Diamonds", "1.00", "4.500.000 $",
                    onBuy = { onMarketTrade("diamonds", "Diamonds", 100L, 4_500_000L, true) },
                    onSell = { onMarketTrade("diamonds", "Diamonds", 100L, 4_100_000L, false) }
                )
                MarketTradeRow("Tanks", "100 pcs", "12.000.000 $",
                    onBuy = { onMarketTrade("tanks", "Tanks", 100L, 12_000_000L, true) },
                    onSell = { onMarketTrade("tanks", "Tanks", 100L, 10_800_000L, false) }
                )
                MarketTradeRow("Energy Drink", "50 pcs", "900.000 $",
                    onBuy = { onMarketTrade("energyDrinks", "Energy Drinks", 50L, 900_000L, true) },
                    onSell = { onMarketTrade("energyDrinks", "Energy Drinks", 50L, 800_000L, false) }
                )
                MarketTradeRow("Travel Tickets", "5 pcs", "1.250.000 $",
                    onBuy = { onMarketTrade("tickets", "Tickets", 5L, 1_250_000L, true) },
                    onSell = { onMarketTrade("tickets", "Tickets", 5L, 1_000_000L, false) }
                )
            }
        }

        // Craft Weapons (uses Oil, Ore & Wood)
        item {
            Text("CRAFT MILITARY HARDWARE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        items(WeaponType.entries) { weapon ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(weapon.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            text = "DMG ${RivalRegionsRepository.formatDots(weapon.damage)} · ${weapon.oilCost} Oil · ${weapon.oreCost} Ore · ${weapon.woodCost} Wood",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    RrActionButton(
                        onClick = { onCraftWeapon(weapon, 5L) },
                        accentBorder = HoverYellow,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Craft ${weapon.title}", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Craft +5", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreResourceTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceElevated,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent, maxLines = 1)
        }
    }
}

@Composable
private fun StorageTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfacePanel),
        border = BorderStroke(1.dp, HairlineBorder),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(3.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent, maxLines = 1)
        }
    }
}

@Composable
private fun MarketTradeRow(
    name: String,
    qtyLabel: String,
    priceLabel: String,
    onBuy: () -> Unit,
    onSell: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfacePanel),
        border = BorderStroke(1.dp, HairlineBorder),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("$name ($qtyLabel)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(priceLabel, style = MaterialTheme.typography.labelSmall, color = KingGold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RrActionButton(
                    onClick = onBuy,
                    containerColor = PakistanBright,
                    contentColor = TopBarBg,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("BUY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                RrActionButton(
                    onClick = onSell,
                    containerColor = SurfaceElevated,
                    contentColor = HoverYellow,
                    accentBorder = HoverYellow,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("SELL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
