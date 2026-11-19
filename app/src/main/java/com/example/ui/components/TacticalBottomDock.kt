package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameTab
import com.example.model.SubPanel
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.SidebarBg
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private fun GameTab.icon(selected: Boolean): ImageVector = when (this) {
    GameTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    GameTab.ARTICLES -> if (selected) Icons.Filled.Article else Icons.Outlined.Article
    GameTab.MAPS -> if (selected) Icons.Filled.Map else Icons.Outlined.Map
    GameTab.STORAGE -> if (selected) Icons.Filled.Warehouse else Icons.Outlined.Warehouse
    GameTab.WORK -> if (selected) Icons.Filled.Build else Icons.Outlined.Build
    GameTab.WARS -> if (selected) Icons.Filled.GpsFixed else Icons.Outlined.GpsFixed
    GameTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
}

@Composable
fun RrLeftSidebar(
    selectedTab: GameTab,
    subPanel: SubPanel,
    onSelectTab: (GameTab) -> Unit,
    onSelectSubPanel: (SubPanel) -> Unit
) {
    Row(modifier = Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(54.dp)
                .fillMaxHeight()
                .background(SidebarBg)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SidebarIconBtn(
                icon = Icons.Default.Map,
                label = "Map",
                selected = selectedTab == GameTab.MAPS && subPanel == SubPanel.NONE,
                onClick = { onSelectTab(GameTab.MAPS) },
                testTag = "sidebar_map"
            )
            SidebarIconBtn(
                icon = Icons.Default.AccountBalance,
                label = "Parl",
                selected = subPanel == SubPanel.PARLIAMENT,
                onClick = { onSelectSubPanel(SubPanel.PARLIAMENT) },
                testTag = "sidebar_parliament"
            )
            SidebarIconBtn(
                icon = Icons.Default.Flag,
                label = "Wars",
                selected = selectedTab == GameTab.WARS && subPanel == SubPanel.NONE,
                onClick = { onSelectTab(GameTab.WARS) },
                testTag = "sidebar_wars"
            )
            SidebarIconBtn(
                icon = Icons.Default.Store,
                label = "Mkt",
                selected = selectedTab == GameTab.STORAGE && subPanel == SubPanel.NONE,
                onClick = { onSelectTab(GameTab.STORAGE) },
                testTag = "sidebar_market"
            )
            SidebarIconBtn(
                icon = Icons.Default.Build,
                label = "Work",
                selected = selectedTab == GameTab.WORK && subPanel == SubPanel.NONE,
                onClick = { onSelectTab(GameTab.WORK) },
                testTag = "sidebar_work"
            )
            SidebarIconBtn(
                icon = Icons.Default.Groups,
                label = "Party",
                selected = subPanel == SubPanel.PARTY,
                onClick = { onSelectSubPanel(SubPanel.PARTY) },
                testTag = "sidebar_party"
            )
            SidebarIconBtn(
                icon = Icons.Default.Chat,
                label = "Bots",
                selected = subPanel == SubPanel.GLOBAL_CHAT,
                onClick = { onSelectSubPanel(SubPanel.GLOBAL_CHAT) },
                testTag = "sidebar_chat"
            )
            SidebarIconBtn(
                icon = Icons.Default.Person,
                label = "Prof",
                selected = selectedTab == GameTab.PROFILE && subPanel == SubPanel.NONE,
                onClick = { onSelectTab(GameTab.PROFILE) },
                testTag = "sidebar_profile"
            )
        }
        VerticalDivider(color = HairlineBorder, thickness = 1.dp)
    }
}

@Composable
private fun SidebarIconBtn(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (selected) SurfacePanel else SidebarBg,
        border = BorderStroke(1.dp, if (selected) HoverYellow else HairlineBorder.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(width = 44.dp, height = 46.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) HoverYellow else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) HoverYellow else TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TacticalBottomDock(
    selectedTab: GameTab,
    subPanelActive: Boolean,
    onSelectTab: (GameTab) -> Unit
) {
    Surface(
        color = Color(0xFF212121),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = HairlineBorder.copy(alpha = 0.6f), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameTab.entries.forEach { tab ->
                    val selected = tab == selectedTab && !subPanelActive
                    val tint = if (selected) TextPrimary else Color(0xFF8C8C8C)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 4.dp)
                            .testTag("tab_${tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon(selected),
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(21.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            fontFamily = InterFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = tint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
