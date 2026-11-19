package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ArticlePost
import com.example.ui.components.RrActionButton
import com.example.ui.theme.DeepCanvasBg
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TopBarBg
import com.example.ui.theme.WarCrimson
import com.example.viewmodel.WorldData

@Composable
fun ArticlesTab(
    world: WorldData,
    onPublishArticle: (title: String, body: String, newspaper: String) -> Unit,
    onVoteArticle: (ArticlePost, Long) -> Unit,
    onOpenBotChat: () -> Unit
) {
    var showPublishDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCanvasBg),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ARTICLES & PRESS", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text("State newspapers & human-like bot dispatches", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RrActionButton(
                        onClick = onOpenBotChat,
                        accentBorder = HoverYellow,
                        modifier = Modifier.testTag("articles_bot_chat_btn")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Bot Chat", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat", fontWeight = FontWeight.Bold)
                    }
                    RrActionButton(
                        onClick = { showPublishDialog = true },
                        containerColor = PakistanBright,
                        contentColor = TopBarBg,
                        modifier = Modifier.testTag("publish_article_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Write", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Write", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(world.articles, key = { it.id }) { article ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfacePanel),
                border = BorderStroke(1.dp, HairlineBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Newspaper, contentDescription = null, tint = HoverYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = article.newspaper.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = HoverYellow
                            )
                        }
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, HairlineBorder)
                        ) {
                            Text(
                                text = if (article.botGenerated) "BOT PRESS · ${article.authorName}" else article.authorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(article.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(article.body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RrActionButton(
                            onClick = { onVoteArticle(article, 1L) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Upvote", tint = PakistanBright, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+1", color = PakistanBright, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            text = "${article.votes} Votes",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                        RrActionButton(
                            onClick = { onVoteArticle(article, -1L) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Downvote", tint = WarCrimson, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("-1", color = WarCrimson, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    if (showPublishDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var paper by remember { mutableStateOf("Islamabad Herald") }

        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            containerColor = SurfacePanel,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("PUBLISH ARTICLE", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = paper,
                        onValueChange = { paper = it },
                        label = { Text("Newspaper") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HoverYellow, unfocusedBorderColor = HairlineBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Headline") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HoverYellow, unfocusedBorderColor = HairlineBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Dispatch Body") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HoverYellow, unfocusedBorderColor = HairlineBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            },
            confirmButton = {
                RrActionButton(
                    onClick = {
                        showPublishDialog = false
                        onPublishArticle(title, body, paper)
                    },
                    enabled = title.trim().length >= 2 && body.trim().length >= 2,
                    containerColor = PakistanBright,
                    contentColor = TopBarBg
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
