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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GameWorldSeed
import com.example.ui.components.RrActionButton
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepCanvasBg
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.KingGold
import com.example.ui.theme.OilAmber
import com.example.ui.theme.OreSlate
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg

@Composable
fun NewGameSetupScreen(
    initialCommanderName: String,
    initialStateName: String,
    initialPartyName: String,
    initialRegionId: String,
    isOwner: Boolean,
    canCancel: Boolean,
    onStartNewGame: (commanderName: String, stateName: String, regionId: String, partyName: String) -> Unit,
    onCancel: () -> Unit
) {
    var commanderName by remember {
        mutableStateOf(initialCommanderName.ifBlank { if (isOwner) "State Owner" else "Player One" })
    }
    var stateName by remember {
        mutableStateOf(initialStateName.ifBlank { "Pakistan" })
    }
    var partyName by remember {
        mutableStateOf(initialPartyName.ifBlank { "National Reform Party" })
    }
    var selectedRegionId by remember {
        mutableStateOf(
            GameWorldSeed.mapNodes.firstOrNull { it.id == initialRegionId }?.id ?: "punjab"
        )
    }
    var searchQuery by remember { mutableStateOf("") }

    BackHandler(enabled = canCancel) {
        onCancel()
    }

    val filteredNodes = remember(searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            GameWorldSeed.mapNodes
        } else {
            GameWorldSeed.mapNodes.filter {
                it.fullName.lowercase().contains(q) || it.country.lowercase().contains(q)
            }
        }
    }
    val selectedNode = remember(selectedRegionId) {
        GameWorldSeed.mapNodes.firstOrNull { it.id == selectedRegionId } ?: GameWorldSeed.mapNodes.first()
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HoverYellow,
        unfocusedBorderColor = HairlineBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = HoverYellow,
        unfocusedLabelColor = TextSecondary
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCanvasBg)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NEW GAME · CLEAN WORLD",
                            style = MaterialTheme.typography.headlineMedium,
                            color = HoverYellow
                        )
                        Text(
                            text = "40 Real Countries World Map · Zero Legacy Players",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    if (canCancel) {
                        TextButton(onClick = onCancel) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                }
            }

            if (isOwner) {
                item {
                    Surface(
                        color = KingGold.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, KingGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Owner",
                                tint = KingGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OWNER ACCOUNT ACTIVE: Unlimited Troops, War Gadgets, Wood, Gold & Resources.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KingGold
                            )
                        }
                    }
                }
            }

            // Player Inputs Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "COMMANDER SETUP",
                            style = MaterialTheme.typography.labelMedium,
                            color = PakistanBright
                        )
                        OutlinedTextField(
                            value = commanderName,
                            onValueChange = { commanderName = it },
                            label = { Text("Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PakistanBright) },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_name_input")
                        )
                        OutlinedTextField(
                            value = stateName,
                            onValueChange = { stateName = it },
                            label = { Text("State Name") },
                            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = HoverYellow) },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_state_input")
                        )
                        OutlinedTextField(
                            value = partyName,
                            onValueChange = { partyName = it },
                            label = { Text("Party Name") },
                            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = CyanAccent) },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_party_input")
                        )
                    }
                }
            }

            // Fresh Start Stats Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CLEAN START STATS",
                            style = MaterialTheme.typography.labelMedium,
                            color = HoverYellow
                        )
                        Text(
                            text = "Lvl 1 · \$1B · 100 Gold · 100 E · 5 Tickets · 100 Wood",
                            style = MaterialTheme.typography.titleMedium,
                            color = PakistanBright
                        )
                        Text(
                            text = "Edu 10 (EduLvl 1) · Str 10 (StrLvl 1) · End 100 (EndLvl 1) · WorkExp 0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Selected Parliament: ${selectedNode.shortName} Parl - Real Country - Clean",
                            style = MaterialTheme.typography.labelSmall,
                            color = KingGold
                        )
                    }
                }
            }

            // Region Selector Header + Search
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = PakistanBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SELECT REAL COUNTRY REGION (${GameWorldSeed.mapNodes.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Filter 40 Real Countries (e.g. Pakistan, USA, UK, Japan)") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 40 Real Countries Selectable List
            items(filteredNodes, key = { it.id }) { node ->
                val isSelected = node.id == selectedRegionId
                Surface(
                    color = if (isSelected) SurfaceElevated else SurfacePanel,
                    border = BorderStroke(1.dp, if (isSelected) HoverYellow else HairlineBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedRegionId = node.id }
                        .testTag("setup_region_${node.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = node.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) HoverYellow else TextPrimary
                            )
                            Text(
                                text = "${node.shortName} Parl - Real Country - Clean",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Gold ${node.gold}", style = MaterialTheme.typography.labelSmall, color = KingGold)
                                Text("Oil ${node.oil}", style = MaterialTheme.typography.labelSmall, color = OilAmber)
                                Text("Ore ${node.ore}", style = MaterialTheme.typography.labelSmall, color = OreSlate)
                                Text("Wood ${node.wood}", style = MaterialTheme.typography.labelSmall, color = PakistanBright)
                            }
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = PakistanBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Start New Game CTA
            item {
                Spacer(modifier = Modifier.height(4.dp))
                RrActionButton(
                    onClick = {
                        onStartNewGame(
                            commanderName,
                            stateName,
                            selectedRegionId,
                            partyName
                        )
                    },
                    enabled = commanderName.trim().isNotEmpty() && stateName.trim().isNotEmpty() && partyName.trim().isNotEmpty(),
                    containerColor = PakistanBright,
                    contentColor = TopBarBg,
                    accentBorder = HoverYellow,
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_new_game_btn")
                ) {
                    Text(
                        text = "START NEW GAME",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
