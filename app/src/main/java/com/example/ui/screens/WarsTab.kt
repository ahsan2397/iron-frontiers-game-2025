package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RivalRegionsRepository
import com.example.model.RegionState
import com.example.model.WarFront
import com.example.model.WarStatus
import com.example.model.WarType
import com.example.model.WeaponType
import com.example.ui.components.PakistanFlagBadge
import com.example.ui.components.RrActionButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepCanvasBg
import com.example.ui.theme.FightOrange
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

@Composable
fun WarsTab(
    world: WorldData,
    onStartCampaign: (RegionState, WarType) -> Unit,
    onStartTrainingWar: (RegionState, (String) -> Unit) -> Unit,
    onStrike: (WarFront, RegionState?, WeaponType?, Long, Boolean, Long) -> Unit,
    onUseEnergyDrink: () -> Unit,
    onActivatePremium: () -> Unit
) {
    val profile = world.profile
    val regionsById = remember(world.regions) { world.regions.associateBy { it.id } }
    val currentRegion = regionsById[profile.currentRegionId] ?: world.regions.firstOrNull()
    val conquerableTargets = remember(world.regions) {
        world.regions.filter { !it.isSovereignProtected }
    }
    val activeRegionWarIds = remember(world.wars) {
        world.wars.filter { it.status == WarStatus.ACTIVE.name && it.warType != WarType.TRAINING.name }
            .map { it.targetRegionId }
            .toSet()
    }
    val availableRevolutionTargets = remember(conquerableTargets, activeRegionWarIds) {
        conquerableTargets.filter { it.id !in activeRegionWarIds }
    }

    var selectedWarId by remember { mutableStateOf<String?>(null) }
    var selectedRevRegionId by remember(availableRevolutionTargets) {
        mutableStateOf(availableRevolutionTargets.firstOrNull()?.id ?: "delhi")
    }
    var showCoupPicker by remember { mutableStateOf(false) }

    val activeWarRoom = remember(selectedWarId, world.wars) {
        world.wars.firstOrNull { it.id == selectedWarId }
    }

    if (activeWarRoom != null) {
        BackHandler { selectedWarId = null }
        WarRoomTrainingScreen(
            world = world,
            war = activeWarRoom,
            targetRegion = regionsById[activeWarRoom.targetRegionId],
            currentRegion = currentRegion,
            onBack = { selectedWarId = null },
            onStrike = onStrike,
            onUseEnergyDrink = onUseEnergyDrink,
            onActivatePremium = onActivatePremium
        )
        return
    }

    val activeWarsCount = world.wars.count { it.status == WarStatus.ACTIVE.name }
    val activeStateWars = world.wars.count {
        it.status == WarStatus.ACTIVE.name &&
            it.warType != WarType.TRAINING.name &&
            regionsById[it.targetRegionId]?.controller.equals(profile.stateName, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCanvasBg),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. State War Status Banner (matching screenshot 1 top dove row)
        item {
            Surface(
                color = SurfacePanel,
                border = BorderStroke(1.dp, if (activeStateWars > 0) WarCrimson else HairlineBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (activeStateWars > 0) Icons.Default.MilitaryTech else Icons.Default.Shield,
                        contentDescription = "State War Status",
                        tint = if (activeStateWars > 0) WarCrimson else TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (activeStateWars > 0) {
                            "$activeStateWars active war(s) in ${profile.stateName}!"
                        } else {
                            "No active wars in your state!"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        // 2. Revolution Card + "Join, -300 G" Blue Button
        item {
            val targetRev = regionsById[selectedRevRegionId]
                ?: availableRevolutionTargets.firstOrNull()
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Revolution",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start a revolution in a real country region (${targetRev?.name ?: "None"}) to fight for independence!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (availableRevolutionTargets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(availableRevolutionTargets.take(12), key = { it.id }) { reg ->
                                val isSelected = reg.id == selectedRevRegionId
                                Surface(
                                    color = if (isSelected) SteelBlue.copy(alpha = 0.25f) else SurfaceElevated,
                                    border = BorderStroke(1.dp, if (isSelected) SteelBlue else HairlineBorder),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { selectedRevRegionId = reg.id }
                                ) {
                                    Text(
                                        text = reg.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (targetRev != null) {
                                onStartCampaign(targetRev, WarType.REVOLUTION)
                            }
                        },
                        enabled = targetRev != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2D6AE0),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("revolution_join_button")
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("Join, ")
                                withStyle(SpanStyle(color = HoverYellow, fontWeight = FontWeight.Bold)) {
                                    append("-300 G")
                                }
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Coup Card + Flame Icon + "Coup regions" Red-Orange Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Coup",
                            tint = TextPrimary,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Coup",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Start a coup in any non-protected country region and fight for independence!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showCoupPicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD93F1E),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("coup_regions_button")
                    ) {
                        Text(
                            text = "Coup regions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 4. Full-width "Military training" Blue Button
        item {
            Button(
                onClick = {
                    val existingTraining = world.wars.firstOrNull {
                        it.warType == WarType.TRAINING.name && it.status == WarStatus.ACTIVE.name
                    }
                    if (existingTraining != null) {
                        selectedWarId = existingTraining.id
                    } else if (currentRegion != null) {
                        onStartTrainingWar(currentRegion) { createdId ->
                            selectedWarId = createdId
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D6AE0),
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("military_training_button")
            ) {
                Text(
                    text = "Military training",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 5. "All the wars of the world (N)" Header + Premium Status Hint
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All the wars of the world ($activeWarsCount)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                val hasPremium = profile.kingMode || profile.premiumActive
                Surface(
                    color = if (hasPremium) PakistanEmerald.copy(alpha = 0.2f) else SurfaceElevated,
                    border = BorderStroke(1.dp, if (hasPremium) PakistanBright else HairlineBorder),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { if (!hasPremium) onActivatePremium() }
                ) {
                    Text(
                        text = if (hasPremium) "Auto War: ON" else "Manual Fight Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasPremium) PakistanBright else HoverYellow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 6. 3-Column RR War Rows (Clean real countries, zero Syria/Turan names)
        items(world.wars, key = { it.id }) { war ->
            RrThreeColumnWarRow(
                war = war,
                targetRegion = regionsById[war.targetRegionId],
                stateName = profile.stateName,
                onFightClick = { selectedWarId = war.id }
            )
            HorizontalDivider(color = HairlineBorder.copy(alpha = 0.45f), thickness = 1.dp)
        }
    }

    if (showCoupPicker) {
        AlertDialog(
            onDismissRequest = { showCoupPicker = false },
            containerColor = SurfacePanel,
            titleContentColor = TextPrimary,
            title = {
                Text("Select Region for Coup", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(conquerableTargets, key = { it.id }) { reg ->
                        val alreadyAtWar = reg.id in activeRegionWarIds
                        Surface(
                            color = SurfaceElevated,
                            border = BorderStroke(1.dp, if (alreadyAtWar) TextSecondary else WarCrimson),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(enabled = !alreadyAtWar) {
                                    showCoupPicker = false
                                    onStartCampaign(reg, WarType.COUP)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reg.name, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                                    Text("State: ${reg.controller}", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                }
                                Text(
                                    text = if (alreadyAtWar) "AT WAR" else "COUP",
                                    color = if (alreadyAtWar) TextSecondary else FightOrange,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCoupPicker = false }) {
                    Text("Close", color = TextPrimary)
                }
            }
        )
    }
}

@Composable
private fun RrThreeColumnWarRow(
    war: WarFront,
    targetRegion: RegionState?,
    stateName: String,
    onFightClick: () -> Unit
) {
    val warType = WarType.fromRaw(war.warType)
    val isActive = war.status == WarStatus.ACTIVE.name
    val diff = war.attackerDamage - war.defenderDamage
    val total = (war.attackerDamage + war.defenderDamage).coerceAtLeast(1L)
    val redFraction = (war.attackerDamage.toFloat() / total.toFloat()).coerceIn(0.12f, 0.88f)

    val leftIcon = when (warType) {
        WarType.COUP -> Icons.Default.LocalFireDepartment
        WarType.REVOLUTION -> Icons.Default.PanTool
        WarType.TRAINING -> Icons.Default.MilitaryTech
        WarType.WAR -> Icons.Default.Flag
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left Column: Attacker Icon + Name + Damage
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = war.attackerName,
                    tint = if (warType == WarType.WAR) PakistanBright else TextPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = war.attackerName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Damage: ${RivalRegionsRepository.formatDots(war.attackerDamage)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Center Column: Diff, Tug-of-War Bar, Red-Orange Fight Button, Timer / Target
        Column(
            modifier = Modifier.weight(1.1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (warType == WarType.TRAINING) "TRAINING" else war.targetRegionName,
                style = MaterialTheme.typography.labelSmall,
                color = HoverYellow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = RivalRegionsRepository.formatDots(diff),
                fontFamily = PixelFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(redFraction)
                        .fillMaxHeight()
                        .background(Color(0xFFDC2626))
                )
                Box(
                    modifier = Modifier
                        .weight((1f - redFraction).coerceAtLeast(0.08f))
                        .fillMaxHeight()
                        .background(Color(0xFF2563EB))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onFightClick,
                enabled = isActive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD93F1E),
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("open_war_room_${war.id}")
            ) {
                Icon(leftIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isActive) "Fight" else war.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (warType == WarType.TRAINING) "Training live" else "HP: ${RivalRegionsRepository.formatCompact(targetRegion?.hp ?: 200_000L)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // Right Column: Defender Flag/Crest + Name + Damage
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (targetRegion?.controller.equals(stateName, ignoreCase = true) ||
                    targetRegion?.country == "Pakistan"
                ) {
                    PakistanFlagBadge(isPakistan = true, width = 36.dp, height = 22.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = war.defenderName,
                        tint = CyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = war.defenderName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Damage: ${RivalRegionsRepository.formatDots(war.defenderDamage)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WarRoomTrainingScreen(
    world: WorldData,
    war: WarFront,
    targetRegion: RegionState?,
    currentRegion: RegionState?,
    onBack: () -> Unit,
    onStrike: (WarFront, RegionState?, WeaponType?, Long, Boolean, Long) -> Unit,
    onUseEnergyDrink: () -> Unit,
    onActivatePremium: () -> Unit
) {
    val profile = world.profile
    val isTraining = war.warType == WarType.TRAINING.name
    val hasPremium = profile.kingMode || profile.premiumActive

    var attackingSide by remember(war.id) { mutableStateOf(true) }
    var selectedWeapon by remember { mutableStateOf<WeaponType?>(WeaponType.TANK) }
    var autoHourEnabled by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val availableSelectedWeaponCount = when (selectedWeapon) {
        WeaponType.TANK -> profile.tanks
        WeaponType.BOMBER -> profile.bombers
        WeaponType.MISSILE -> profile.missiles
        null -> 999_999L
    }
    // Use infantry automatically if player has 0 of the chosen weapon and is not owner
    val effectiveWeapon = if (profile.kingMode || selectedWeapon == null || availableSelectedWeaponCount >= 1L) {
        selectedWeapon
    } else {
        null
    }

    val singleHitDamage = remember(profile.strength, profile.education, profile.endurance, profile.kingMode, effectiveWeapon) {
        RivalRegionsRepository.computeAlphaDamage(profile, effectiveWeapon, 1L)
    }
    val maxAutoMultiplier = if (profile.kingMode) {
        30L
    } else {
        (profile.energy / 10L).coerceIn(1L, 30L)
    }

    LaunchedEffect(autoHourEnabled, hasPremium, profile.energy, war.status) {
        if (!autoHourEnabled || !hasPremium || war.status != WarStatus.ACTIVE.name) return@LaunchedEffect
        while (autoHourEnabled && hasPremium && war.status == WarStatus.ACTIVE.name) {
            if (profile.kingMode || profile.energy >= 10L) {
                onStrike(war, targetRegion, effectiveWeapon, 1L, attackingSide, 1L)
            } else {
                autoHourEnabled = false
                break
            }
            delay(10_000L)
        }
    }

    val totalDmg = (war.attackerDamage + war.defenderDamage).coerceAtLeast(1L)
    val redRatio = (war.attackerDamage.toFloat() / totalDmg.toFloat()).coerceIn(0.1f, 0.9f)
    val diff = war.defenderDamage - war.attackerDamage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCanvasBg),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Header: ←  MILITARY TRAINING / WAR  [?] [🔗]
        item {
            Column(modifier = Modifier.fillMaxWidth().background(SurfacePanel)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("war_room_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Wars",
                            tint = TextPrimary
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTraining) "MILITARY TRAINING" else war.targetRegionName.uppercase(),
                            fontFamily = PixelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isTraining) {
                                "Real Training · No regions lost"
                            } else {
                                "${war.warType} · HP ${RivalRegionsRepository.formatCompact(targetRegion?.hp ?: 200_000L)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "War Help", tint = TextPrimary)
                    }
                    IconButton(onClick = onActivatePremium) {
                        Icon(Icons.Default.Link, contentDescription = "Premium Status", tint = if (hasPremium) KingGold else TextPrimary)
                    }
                }

                // 2. Full-Width Red/Blue Damage Header Bar (matching screenshot 2)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(redRatio)
                                .fillMaxHeight()
                                .background(Color(0xFFDF3E36))
                        )
                        Box(
                            modifier = Modifier
                                .weight((1f - redRatio).coerceAtLeast(0.08f))
                                .fillMaxHeight()
                                .background(Color(0xFF2D6AE0))
                        )
                    }
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DAMAGE:",
                            fontFamily = PixelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                        Text(
                            text = RivalRegionsRepository.formatDots(diff),
                            fontFamily = PixelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 3. Attacker vs Defender Kettlebell Cards + Send my troops buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Attacker column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Damage:\n${RivalRegionsRepository.formatDots(war.attackerDamage)}",
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Attack Troops",
                        tint = if (attackingSide) TextPrimary else TextSecondary,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Attacker:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = if (isTraining) "Attack troops" else war.attackerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { attackingSide = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (attackingSide) Color(0xFF707070) else Color(0xFF2D6AE0),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("select_attack_side_btn")
                    ) {
                        Text(
                            text = "Send my troops",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                // Center status column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(68.dp)
                ) {
                    Text(
                        text = "War ends in:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isTraining) "20:33:42" else war.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Defender column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Damage:\n${RivalRegionsRepository.formatDots(war.defenderDamage)}",
                        fontFamily = PixelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Defense Troops",
                        tint = if (!attackingSide) TextPrimary else TextSecondary,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Defender:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = if (isTraining) "Defense troops" else war.defenderName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { attackingSide = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!attackingSide) Color(0xFF707070) else Color(0xFF2D6AE0),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("select_defense_side_btn")
                    ) {
                        Text(
                            text = "Send my troops",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 4. Formula status line + Yellow Stock Count + Weapon Selector + α-damage pill
        item {
            val sideName = if (attackingSide) "Attack troops" else "Defense troops"
            val regionLabel = currentRegion?.name ?: "Punjab - Pakistan"
            val maxPossibleDmg = singleHitDamage * maxAutoMultiplier

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$regionLabel, $sideName, maximum α-damage for your energy (${if (profile.kingMode) "∞" else "${profile.energy}E"}): ${RivalRegionsRepository.formatDots(maxPossibleDmg)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Gold/yellow stock readout like screenshot 2 ("465780")
                val stockText = when {
                    profile.kingMode -> "∞"
                    effectiveWeapon == WeaponType.TANK -> RivalRegionsRepository.formatDots(profile.tanks)
                    effectiveWeapon == WeaponType.BOMBER -> RivalRegionsRepository.formatDots(profile.bombers)
                    effectiveWeapon == WeaponType.MISSILE -> RivalRegionsRepository.formatDots(profile.missiles)
                    else -> "INFANTRY"
                }
                Text(
                    text = stockText,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = HoverYellow
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Weapon Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        WeaponChip(
                            label = "Infantry (Free)",
                            selected = effectiveWeapon == null,
                            onClick = { selectedWeapon = null }
                        )
                    }
                    items(WeaponType.entries, key = { it.name }) { weapon ->
                        val count = profile.machineCount(weapon)
                        WeaponChip(
                            label = "${weapon.title.substringBefore(" ")} (${if (profile.kingMode) "∞" else count})",
                            selected = effectiveWeapon == weapon,
                            onClick = { selectedWeapon = weapon }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = SurfacePanel,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = RivalRegionsRepository.formatDots(singleHitDamage),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 5. Fight! + Auto: maximum damage + Auto: once per hour Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Red Fight! button — always enabled for manual Fight!
                Button(
                    onClick = {
                        onStrike(war, targetRegion, effectiveWeapon, 1L, attackingSide, 1L)
                    },
                    enabled = war.status == WarStatus.ACTIVE.name,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD93F1E),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("war_room_manual_fight_btn")
                ) {
                    Text(
                        text = "Fight! α-damage: ${RivalRegionsRepository.formatDots(singleHitDamage)}, -10 E",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Blue Auto: maximum damage (Locked if !hasPremium)
                Button(
                    onClick = {
                        if (hasPremium) {
                            onStrike(war, targetRegion, effectiveWeapon, 1L, attackingSide, maxAutoMultiplier)
                        } else {
                            onActivatePremium()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasPremium) Color(0xFF2D6AE0) else SurfaceElevated,
                        contentColor = Color.White
                    ),
                    border = if (!hasPremium) BorderStroke(1.dp, KingGold) else null,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("war_room_auto_max_btn")
                ) {
                    if (!hasPremium) {
                        Icon(Icons.Default.Lock, contentDescription = "Premium Required", tint = KingGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (hasPremium) {
                            "Auto: maximum damage (${maxAutoMultiplier * 10L}E)"
                        } else {
                            "Auto: maximum damage (Premium)"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Green Auto: once per hour (Locked if !hasPremium)
                Button(
                    onClick = {
                        if (hasPremium) {
                            autoHourEnabled = !autoHourEnabled
                        } else {
                            onActivatePremium()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            !hasPremium -> SurfaceElevated
                            autoHourEnabled -> HoverYellow
                            else -> Color(0xFF55B647)
                        },
                        contentColor = if (autoHourEnabled && hasPremium) TopBarBg else Color.White
                    ),
                    border = if (!hasPremium) BorderStroke(1.dp, KingGold) else null,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("war_room_auto_hour_btn")
                ) {
                    if (!hasPremium) {
                        Icon(Icons.Default.Lock, contentDescription = "Premium Required", tint = KingGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = when {
                            !hasPremium -> "Auto: once per hour (Premium)"
                            autoHourEnabled -> "Auto: once per hour (RUNNING - Tap to Stop)"
                            else -> "Auto: once per hour"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 6. Energy Gauge + Green Refill Button (10.000 Energy Drinks + Buy 10E=1G in Storage)
        item {
            val energyProgress = if (profile.kingMode) 1f else {
                (profile.energy.toFloat() / profile.maxEnergy.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Energy",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (profile.kingMode) {
                                "Energy: ∞ E / ∞ E"
                            } else {
                                "Energy: ${profile.energy} E/${profile.maxEnergy} E"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { energyProgress },
                            color = Color(0xFF55B647),
                            trackColor = SurfacePanel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Drinks: ${if (profile.kingMode) "∞" else RivalRegionsRepository.formatDots(profile.energyDrinks)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = onUseEnergyDrink,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF55B647),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("war_room_refill_btn")
                    ) {
                        Text("Refill", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = SurfacePanel,
            titleContentColor = TextPrimary,
            title = { Text("War & Premium Rules") },
            text = {
                Text(
                    text = "• Everyone starts with 10.000 Energy Drinks for instant Refill.\n" +
                        "• Storage Market sells instant Energy at 10 Energy = 1 Gold.\n" +
                        "• With Premium ON: Auto: maximum damage & Auto: once per hour are unlocked.\n" +
                        "• Without Premium: tap Fight! manually to deal α-damage!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it", color = PakistanBright)
                }
            }
        )
    }
}

@Composable
private fun WeaponChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) SteelBlue.copy(alpha = 0.25f) else SurfacePanel,
        border = BorderStroke(1.dp, if (selected) HoverYellow else HairlineBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) HoverYellow else TextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
