package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.data.GameWorldSeed
import com.example.data.RivalRegionsRepository
import com.example.model.RegionState
import com.example.model.StateBuildingType
import com.example.model.WarType
import com.example.ui.components.PakistanFlagBadge
import com.example.ui.components.RrActionButton
import com.example.ui.components.RrOceanBlue
import com.example.ui.components.WorldPoliticalMapCanvas
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepCanvasBg
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.KingGold
import com.example.ui.theme.OilAmber
import com.example.ui.theme.OreSlate
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.PixelFontFamily
import com.example.ui.theme.SteelBlue
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg
import com.example.ui.theme.UraniumPurple
import com.example.ui.theme.WarCrimson
import com.example.viewmodel.WorldData

private val RrZoomBlue = Color(0xFF4267D6)
private val RrFlightGreen = Color(0xFF54A847)

@Composable
fun MapTab(
    world: WorldData,
    selectedRegionId: String,
    onSelectRegion: (String) -> Unit,
    onRequestResidency: (RegionState) -> Unit,
    onFlyToRegion: (RegionState) -> Unit,
    onSetTax: (RegionState, Long) -> Unit,
    onDeclareWar: (RegionState, WarType) -> Unit,
    onUpgradeBuilding: (RegionState, StateBuildingType) -> Unit,
    onOpenParliament: () -> Unit,
    onGoWork: () -> Unit
) {
    val profile = world.profile
    val regionsById = world.regions.associateBy { it.id }
    val currentRegion = regionsById[profile.currentRegionId] ?: world.regions.firstOrNull()
    val selectedRegion = regionsById[selectedRegionId] ?: currentRegion

    var zoom by remember { mutableFloatStateOf(1.05f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showLabels by remember { mutableStateOf(true) }
    var hasTappedRegion by remember { mutableStateOf(false) }
    var showFullRegionScreen by remember { mutableStateOf(false) }

    BackHandler(enabled = showFullRegionScreen) {
        showFullRegionScreen = false
    }

    if (showFullRegionScreen && selectedRegion != null) {
        DoubleTapRegionDetailScreen(
            world = world,
            selectedRegion = selectedRegion,
            regionsById = regionsById,
            onBackToMap = { showFullRegionScreen = false },
            onSelectRegion = { nextId -> onSelectRegion(nextId) },
            onRequestResidency = onRequestResidency,
            onFlyToRegion = onFlyToRegion,
            onSetTax = onSetTax,
            onDeclareWar = onDeclareWar,
            onUpgradeBuilding = onUpgradeBuilding,
            onOpenParliament = {
                showFullRegionScreen = false
                onOpenParliament()
            },
            onGoWork = {
                showFullRegionScreen = false
                onGoWork()
            }
        )
        return
    }

    val filteredRegions = remember(world.regions, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) world.regions
        else world.regions.filter {
            it.name.lowercase().contains(q) || it.country.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RrOceanBlue)
    ) {
        // 1. Exact RR "MAPS" Header Bar (Search left, MAPS center, Menu right)
        Surface(color = Color(0xFF242424), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { showSearchBar = !showSearchBar },
                    modifier = Modifier.testTag("map_search_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search 40 Real Countries",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "MAPS",
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("map_more_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Map Options",
                            tint = TextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = SurfacePanel
                    ) {
                        DropdownMenuItem(
                            text = { Text("Inspect ${selectedRegion?.name ?: "Region"}", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                showFullRegionScreen = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (showLabels) "Hide Region Labels" else "Show Region Labels", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                showLabels = !showLabels
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reset Map Zoom", color = HoverYellow) },
                            onClick = {
                                showMenu = false
                                zoom = 1.05f
                                pan = Offset.Zero
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open Parliament", color = PakistanBright) },
                            onClick = {
                                showMenu = false
                                onOpenParliament()
                            }
                        )
                    }
                }
            }
        }

        // 2. Exact RR Sub-bar: Single-tap shows State, Region, Green "Start flight" + Tickets Needed & Leader Price!
        val bannerRegion = if (hasTappedRegion && selectedRegion != null) selectedRegion else currentRegion
        val isBannerPlayerSide = bannerRegion?.country == "Pakistan" ||
            bannerRegion?.controller.equals(profile.stateName, ignoreCase = true)
        Surface(color = Color(0xFF333333), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                bannerRegion?.let {
                                    onSelectRegion(it.id)
                                    showFullRegionScreen = true
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PakistanFlagBadge(
                            isPakistan = isBannerPlayerSide,
                            isAutonomy = bannerRegion?.autonomy == true,
                            width = 36.dp,
                            height = 22.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("State:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = bannerRegion?.controller ?: profile.stateName.ifBlank { "Pakistan" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                bannerRegion?.let {
                                    onSelectRegion(it.id)
                                    showFullRegionScreen = true
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(KingGold.copy(alpha = 0.16f))
                                .border(1.dp, KingGold, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Region Emblem",
                                tint = KingGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (hasTappedRegion) "Region:" else "Your region:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = bannerRegion?.name ?: "Punjab - Pakistan",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Single-tap Quick Flight Bar (Screenshot_20261119-113634.jpg exact layout)
                if (hasTappedRegion && bannerRegion != null) {
                    val isBirth = bannerRegion.id == profile.birthRegionId
                    val hasResidency = isBirth || bannerRegion.id in profile.residencies
                    val isStationed = profile.currentRegionId == bannerRegion.id
                    val ticketsNeeded = if (isBirth || profile.kingMode) 0L else 1L
                    val leaderPrice = if (isBirth || profile.kingMode) 0L else (bannerRegion.taxRate * 10_000L)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when {
                            isStationed -> {
                                RrActionButton(
                                    onClick = { showFullRegionScreen = true },
                                    containerColor = SteelBlue,
                                    contentColor = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("map_banner_inspect_btn")
                                ) {
                                    Text("Open Region", fontWeight = FontWeight.Bold)
                                }
                            }
                            !hasResidency && !profile.kingMode -> {
                                RrActionButton(
                                    onClick = { onRequestResidency(bannerRegion) },
                                    containerColor = HoverYellow,
                                    contentColor = TopBarBg,
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("map_banner_residency_btn")
                                ) {
                                    Text("Residency -25G", fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {
                                RrActionButton(
                                    onClick = { onFlyToRegion(bannerRegion) },
                                    containerColor = RrFlightGreen,
                                    contentColor = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("map_start_flight_btn")
                                ) {
                                    Text("Start flight", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showFullRegionScreen = true }
                        ) {
                            Text(
                                text = "Tickets needed: ${if (ticketsNeeded == 0L) "FREE" else "$ticketsNeeded Ticket"} (${if (profile.kingMode) "∞" else profile.tickets} left)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (ticketsNeeded == 0L || profile.tickets >= ticketsNeeded) TextPrimary else WarCrimson,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Leader price: ${if (leaderPrice == 0L) "FREE (0%)" else "${RivalRegionsRepository.formatDots(leaderPrice)} $ (${bannerRegion.taxRate}%)"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = KingGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Citizens: ${RivalRegionsRepository.formatCompact(bannerRegion.population)} · Double-tap region",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = HairlineBorder, thickness = 1.dp)

        // Optional Search + 40 Countries Quick Picker Drawer
        AnimatedVisibility(visible = showSearchBar) {
            Surface(color = SurfacePanel, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search 40 Real Countries (Punjab, Delhi, Tokyo, London...)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HoverYellow,
                            unfocusedBorderColor = HairlineBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = HoverYellow,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filteredRegions, key = { it.id }) { reg ->
                            val isSelected = reg.id == selectedRegionId
                            Surface(
                                color = if (isSelected) HoverYellow.copy(alpha = 0.22f) else SurfaceElevated,
                                border = BorderStroke(1.dp, if (isSelected) HoverYellow else HairlineBorder),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .clickable {
                                        onSelectRegion(reg.id)
                                        hasTappedRegion = true
                                        showFullRegionScreen = true
                                    }
                            ) {
                                Text(
                                    text = reg.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) HoverYellow else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Full-Screen Political World Map (Single tap -> flight strip; Double tap -> full Region Icons screen!)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("interactive_world_map")
        ) {
            WorldPoliticalMapCanvas(
                regionsById = regionsById,
                selectedRegionId = selectedRegionId,
                currentRegionId = profile.currentRegionId,
                playerStateName = profile.stateName,
                zoom = zoom,
                pan = pan,
                showLabels = showLabels,
                onTransform = { zoomDelta, panDelta, vw, vh ->
                    val nextZoom = (zoom * zoomDelta).coerceIn(1.0f, 4.5f)
                    val maxPanX = (vw * (nextZoom - 1f)) * 0.55f + vw * 0.15f
                    val maxPanY = (vh * (nextZoom - 1f)) * 0.55f + vh * 0.15f
                    zoom = nextZoom
                    pan = Offset(
                        x = (pan.x + panDelta.x).coerceIn(-maxPanX, maxPanX),
                        y = (pan.y + panDelta.y).coerceIn(-maxPanY, maxPanY)
                    )
                },
                onRegionTap = { tappedId ->
                    onSelectRegion(tappedId)
                    hasTappedRegion = true
                },
                onRegionDoubleTap = { tappedId ->
                    onSelectRegion(tappedId)
                    hasTappedRegion = true
                    showFullRegionScreen = true
                }
            )

            // Selected Region Quick Pill at Bottom-Left (also opens full double-tap screen cleanly)
            if (selectedRegion != null) {
                Surface(
                    color = SurfacePanel.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, if (selectedRegion.isSovereignProtected) KingGold else HoverYellow),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showFullRegionScreen = true }
                        .testTag("open_selected_region_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedRegion.isSovereignProtected) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = KingGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Column {
                            Text(
                                text = selectedRegion.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = HoverYellow
                            )
                            Text(
                                text = "Gold ${selectedRegion.goldIndex} · Oil ${selectedRegion.oilIndex} · Ore ${selectedRegion.oreIndex} · Double-tap",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Iconic Blue + / - Square Zoom Buttons in Bottom-Right
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = RrZoomBlue,
                    border = BorderStroke(1.5.dp, Color.White),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .clickable { zoom = (zoom * 1.35f).coerceIn(1.0f, 4.5f) }
                        .testTag("map_zoom_in_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Surface(
                    color = RrZoomBlue,
                    border = BorderStroke(1.5.dp, Color.White),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .clickable {
                            val nextZ = (zoom / 1.35f).coerceIn(1.0f, 4.5f)
                            zoom = nextZ
                            if (nextZ <= 1.05f) pan = Offset.Zero
                        }
                        .testTag("map_zoom_out_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoubleTapRegionDetailScreen(
    world: WorldData,
    selectedRegion: RegionState,
    regionsById: Map<String, RegionState>,
    onBackToMap: () -> Unit,
    onSelectRegion: (String) -> Unit,
    onRequestResidency: (RegionState) -> Unit,
    onFlyToRegion: (RegionState) -> Unit,
    onSetTax: (RegionState, Long) -> Unit,
    onDeclareWar: (RegionState, WarType) -> Unit,
    onUpgradeBuilding: (RegionState, StateBuildingType) -> Unit,
    onOpenParliament: () -> Unit,
    onGoWork: () -> Unit
) {
    val profile = world.profile
    val node = GameWorldSeed.mapNodes.firstOrNull { it.id == selectedRegion.id }
    val isBirth = selectedRegion.id == profile.birthRegionId
    val hasResidency = isBirth || selectedRegion.id in profile.residencies
    val isStationed = profile.currentRegionId == selectedRegion.id
    val isStateLeader = profile.kingMode || selectedRegion.controller.equals(profile.stateName, ignoreCase = true)
    val isPlayerSide = selectedRegion.country == "Pakistan" || selectedRegion.controller.equals(profile.stateName, ignoreCase = true)

    var actionsExpanded by remember { mutableStateOf(true) }
    var detailsExpanded by remember { mutableStateOf(true) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val coordLat = String.format("%.2f°", ((node?.yFraction ?: 0.43f) * -140f + 75f))
    val coordLon = String.format("%.2f°", ((node?.xFraction ?: 0.63f) * 360f - 180f))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCanvasBg)
    ) {
        // Exact Top Header: <-  REGION NAME  ?
        Surface(color = Color(0xFF222222), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackToMap,
                    modifier = Modifier.testTag("region_detail_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Map", tint = TextPrimary)
                }
                Text(
                    text = selectedRegion.name.uppercase(),
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(Icons.Default.HelpOutline, contentDescription = "Region Info Help", tint = TextPrimary)
                }
            }
        }
        HorizontalDivider(color = HairlineBorder, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Emblem + City Skyline Row + State & Capital/Autonomy Status
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.rival_crest_icon_1795066273368),
                                contentDescription = "${selectedRegion.name} Crest",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.5.dp, if (selectedRegion.isSovereignProtected) KingGold else HairlineBorder, RoundedCornerShape(6.dp))
                            )
                            Image(
                                painter = painterResource(id = R.drawable.img_command_hero_1795066318720),
                                contentDescription = "${selectedRegion.name} Skyline",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(86.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, HairlineBorder, RoundedCornerShape(6.dp))
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PakistanFlagBadge(
                                isPakistan = isPlayerSide,
                                isAutonomy = selectedRegion.autonomy,
                                width = 38.dp,
                                height = 24.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "State: ${selectedRegion.controller}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Leader Travel Fee: ${RivalRegionsRepository.formatDots(selectedRegion.taxRate * 10_000L)} $ · Tax: ${selectedRegion.taxRate}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedRegion.isSovereignProtected) Icons.Default.Lock else Icons.Default.Block,
                                contentDescription = null,
                                tint = if (selectedRegion.isSovereignProtected) KingGold else TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (selectedRegion.isSovereignProtected) "Sovereign Capital" else if (selectedRegion.autonomy) "Autonomy Region" else "Not an autonomy",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (selectedRegion.isSovereignProtected) KingGold else TextPrimary
                                )
                                Text(
                                    text = if (selectedRegion.isSovereignProtected) "Never conquerable · Protected Capital" else selectedRegion.parliamentTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Four Icon Metric Cards: Citizens / Residents / Parties / Factories (Screenshot_20261119-113759.jpg)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RegionMiniIconCard("Citizens", RivalRegionsRepository.formatCompact(selectedRegion.population), Icons.Default.Groups, Modifier.weight(1f))
                    RegionMiniIconCard("Residents", if (hasResidency) "1" else "0", Icons.Default.CheckCircle, Modifier.weight(1f))
                    RegionMiniIconCard("Parties", "${world.parties.size}", Icons.Default.AccountBalance, Modifier.weight(1f))
                    RegionMiniIconCard("Factories", "${profile.factoryLevel}", Icons.Default.Build, Modifier.weight(1f))
                }
            }

            // 3. Expandable "Actions" Card: Military academy, Residency region, On map, Flight / Work
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { actionsExpanded = !actionsExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Actions", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Icon(
                                imageVector = if (actionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Actions",
                                tint = TextSecondary
                            )
                        }

                        if (actionsExpanded) {
                            // Row A: Military academy (Upgrades Military Base if State Leader / Owner)
                            RegionActionPairRow(
                                buttonText = "Military academy",
                                buttonColor = RrFlightGreen,
                                textColor = Color.White,
                                description = if (isStateLeader) {
                                    "Upgrade Military Academy & State Buildings (State Leader)"
                                } else {
                                    "Build Military Academy (+15 Defense · State Leader only)"
                                },
                                enabled = isStateLeader,
                                onClick = { onUpgradeBuilding(selectedRegion, StateBuildingType.MILITARY_BASE) },
                                tag = "region_action_military_academy"
                            )

                            // Row B: Residency region
                            RegionActionPairRow(
                                buttonText = if (hasResidency) "Residency region" else "Request Residency",
                                buttonColor = if (hasResidency) Color(0xFF6B6B6B) else HoverYellow,
                                textColor = if (hasResidency) Color(0xFF2B2B2B) else TopBarBg,
                                description = when {
                                    isBirth -> "Birth region (FREE): you can vote, work & travel here"
                                    hasResidency -> "You have residency here: you can vote, work & travel here"
                                    else -> "Permit costs -25G. Request residency to work or move here"
                                },
                                enabled = !hasResidency && !profile.kingMode,
                                onClick = { onRequestResidency(selectedRegion) },
                                tag = "region_action_residency"
                            )

                            // Row C: On map / Coordinates
                            RegionActionPairRow(
                                buttonText = "On map",
                                buttonColor = RrFlightGreen,
                                textColor = Color.White,
                                description = "Geographic coordinates:\n$coordLat, $coordLon",
                                enabled = true,
                                onClick = onBackToMap,
                                tag = "region_action_on_map"
                            )

                            // Row D: Fly / Go Work
                            RegionActionPairRow(
                                buttonText = if (isStationed) "Go Work" else "Start flight",
                                buttonColor = SteelBlue,
                                textColor = Color.White,
                                description = if (isStationed) {
                                    "You are stationed in ${selectedRegion.name}. Open Work Factories."
                                } else {
                                    "Move here: ${if (isBirth || profile.kingMode) "FREE" else "1 Ticket + ${RivalRegionsRepository.formatDots(selectedRegion.taxRate * 10_000L)} $"}"
                                },
                                enabled = true,
                                onClick = { if (isStationed) onGoWork() else onFlyToRegion(selectedRegion) },
                                tag = "region_action_flight_or_work"
                            )
                        }
                    }
                }
            }

            // 4. Resource Strip: Gold / Oil / Ore / Uranium (Screenshot_20261119-113759.jpg)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RegionIndexTile("Gold", "${selectedRegion.goldIndex}/250", KingGold, Modifier.weight(1f))
                    RegionIndexTile("Oil", "${selectedRegion.oilIndex}/250", TextPrimary, Modifier.weight(1f))
                    RegionIndexTile("Ore", "${selectedRegion.oreIndex}/200", WarCrimson, Modifier.weight(1f))
                    RegionIndexTile("Uran", "${selectedRegion.uraniumIndex}", PakistanBright, Modifier.weight(1f))
                }
            }

            // 5. Development / Building Icons Row: Medicine / Military / Education / Power
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RegionBuildingIconTile("Medicine", "${selectedRegion.hospitalLevel}/100", Icons.Default.LocalHospital, Modifier.weight(1f))
                    RegionBuildingIconTile("Military", "${selectedRegion.militaryBaseLevel}/100", Icons.Default.MilitaryTech, Modifier.weight(1f))
                    RegionBuildingIconTile("Education", "${selectedRegion.schoolLevel}/100", Icons.Default.School, Modifier.weight(1f))
                    RegionBuildingIconTile("Power", "${selectedRegion.powerPlantLevel}/100", Icons.Default.Apartment, Modifier.weight(1f))
                }
            }

            // 6. Collapsible "Detailed information": State Buildings + Tax & Travel Price + Parliament + War
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { detailsExpanded = !detailsExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Detailed information", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Icon(
                                imageVector = if (detailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Details",
                                tint = TextSecondary
                            )
                        }

                        if (detailsExpanded) {
                            // Parliament Seats Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenParliament() },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("PARLIAMENT (${profile.parliamentSeats} SEATS)", style = MaterialTheme.typography.labelMedium, color = HoverYellow)
                                Icon(Icons.Default.Gavel, contentDescription = "Open Parliament", tint = HoverYellow, modifier = Modifier.size(16.dp))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            ) {
                                val total = world.parties.sumOf { it.seats }.coerceAtLeast(1L)
                                world.parties.filter { it.seats > 0 }.forEach { p ->
                                    Box(
                                        modifier = Modifier
                                            .weight(p.seats.toFloat() / total.toFloat())
                                            .height(10.dp)
                                            .background(parseHexColor(p.colorHex))
                                    )
                                }
                            }

                            // State Leader Building Upgrades
                            Text(
                                text = if (isStateLeader) "STATE LEADER BUILDING UPGRADES (UNLOCKED)" else "STATE BUILDINGS (CONTROLLED BY ${selectedRegion.controller.uppercase()})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isStateLeader) PakistanBright else TextSecondary
                            )
                            StateBuildingType.entries.forEach { building ->
                                val lvl = selectedRegion.buildingLevel(building)
                                val moneyCost = building.baseMoneyCost * lvl
                                val woodCost = building.baseWoodCost * lvl
                                val oreCost = building.baseOreCost * lvl
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(SurfaceElevated)
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${building.title} (Lvl $lvl/100)", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                        Text(building.effectSummary, style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                                        Text(
                                            text = if (profile.kingMode) "Cost: FREE (Owner Unlimited)" else "Cost: ${RivalRegionsRepository.formatCompact(moneyCost)} $ · $woodCost Wood · $oreCost Ore",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = KingGold
                                        )
                                    }
                                    RrActionButton(
                                        onClick = { onUpgradeBuilding(selectedRegion, building) },
                                        enabled = isStateLeader && lvl < 100L,
                                        containerColor = if (isStateLeader) SteelBlue else SurfacePanel,
                                        contentColor = TextPrimary,
                                        accentBorder = if (isStateLeader) HoverYellow else HairlineBorder,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("upgrade_building_${building.name.lowercase()}")
                                    ) {
                                        Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Upgrade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (isStateLeader) {
                                var taxSlider by remember(selectedRegion.id, selectedRegion.taxRate) {
                                    mutableFloatStateOf(selectedRegion.taxRate.toFloat())
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("STATE TAX & TRAVEL PRICE: ${taxSlider.toInt()}%", style = MaterialTheme.typography.labelMedium, color = KingGold)
                                        Text(
                                            text = "Ticket Flight Price: ${RivalRegionsRepository.formatDots(taxSlider.toLong() * 10_000L)} $",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                    RrActionButton(
                                        onClick = { onSetTax(selectedRegion, taxSlider.toLong()) },
                                        accentBorder = KingGold,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Apply Price", style = MaterialTheme.typography.labelSmall, color = KingGold)
                                    }
                                }
                                Slider(
                                    value = taxSlider,
                                    onValueChange = { taxSlider = it },
                                    valueRange = 0f..50f,
                                    colors = SliderDefaults.colors(thumbColor = KingGold, activeTrackColor = KingGold)
                                )
                            }

                            if (selectedRegion.isSovereignProtected) {
                                Surface(
                                    color = KingGold.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, KingGold),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = KingGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Punjab - Pakistan is NEVER conquerable.", style = MaterialTheme.typography.bodyMedium, color = KingGold)
                                    }
                                }
                            } else if (!selectedRegion.controller.equals(profile.stateName, ignoreCase = true)) {
                                RrActionButton(
                                    onClick = { onDeclareWar(selectedRegion, WarType.WAR) },
                                    containerColor = WarCrimson,
                                    contentColor = TextPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("DECLARE WAR ON ${selectedRegion.name.uppercase()}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 7. Border regions card (tappable neighbors from the 40 real countries!)
            if (node != null && node.neighbors.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Border regions", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            node.neighbors.forEach { nbId ->
                                val nb = regionsById[nbId]
                                val nbLabel = nb?.name ?: nbId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SurfaceElevated)
                                        .clickable { onSelectRegion(nbId) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PakistanFlagBadge(
                                            isPakistan = nb?.country == "Pakistan" ||
                                                nb?.controller.equals(profile.stateName, ignoreCase = true),
                                            width = 24.dp,
                                            height = 15.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(nbLabel, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                    Text(nb?.controller ?: "", style = MaterialTheme.typography.labelSmall, color = HoverYellow)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = SurfacePanel,
            title = { Text("Region Screen Guide", color = HoverYellow) },
            text = {
                Text(
                    "Single-tap any country on the World Map for quick Flight & Ticket Price info set by its State Leader.\n\nDouble-tap any region to open this full Region Screen with Citizens, Buildings, Resources, and Border Regions.",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
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
private fun RegionMiniIconCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfacePanel,
        border = BorderStroke(1.dp, HairlineBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp)) {
            Text(label, fontFamily = InterFontFamily, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = TextSecondary, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = value,
                    fontFamily = PixelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RegionActionPairRow(
    buttonText: String,
    buttonColor: Color,
    textColor: Color,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RrActionButton(
            onClick = onClick,
            enabled = enabled,
            containerColor = buttonColor,
            contentColor = textColor,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 46.dp)
                .testTag(tag)
        ) {
            Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            lineHeight = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RegionIndexTile(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = SurfacePanel,
        border = BorderStroke(1.dp, HairlineBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)) {
            Text(label, fontFamily = InterFontFamily, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(3.dp))
            Text(value, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = valueColor, maxLines = 1)
        }
    }
}

@Composable
private fun RegionBuildingIconTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfacePanel,
        border = BorderStroke(1.dp, HairlineBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)) {
            Text(label, fontFamily = InterFontFamily, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(value, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary, maxLines = 1)
            }
        }
    }
}
