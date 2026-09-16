package com.example.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.Chat
import com.example.data.repository.LovyChatRepository
import com.example.ui.components.LovyAvatar
import com.example.ui.theme.LovyPrimary
import com.example.ui.theme.LovySecondary
import com.example.ui.theme.OnlineGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    repository: LovyChatRepository,
    onChatClick: (Chat) -> Unit,
    onNavigateToNearby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chats by repository.chats.collectAsState()
    val nearbyUsers by repository.nearbyUsers.collectAsState()
    val isSynced by repository.isFirebaseSynced.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("Semua", "Pribadi", "Grup", "Belum Dibaca")

    val filteredChats = chats.filter { chat ->
        val matchesSearch = chat.title.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilterIndex) {
            1 -> !chat.isGroup
            2 -> chat.isGroup
            3 -> chat.unreadCount > 0
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header: Title and Firebase sync pill
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lovy Chat",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = LovyPrimary
                        )
                        Text(
                            text = "Pesan Instan & E2EE Terproteksi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Firebase Real-time sync chip
                    Surface(
                        color = if (isSynced) OnlineGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("firebase_sync_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isSynced) OnlineGreen else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSynced) "Firebase Live" else "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSynced) OnlineGreen else Color.Gray
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .testTag("chat_search_bar"),
                    placeholder = { Text("Cari pesan, kontak, atau grup...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LovyPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }

            // Active / Nearby Stories Row
            item {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Teman Aktif Sekitar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lihat Radar →",
                            style = MaterialTheme.typography.labelMedium,
                            color = LovyPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToNearby() }
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(nearbyUsers) { user ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .clickable {
                                        repository.sayHi(user)
                                        chats.find { it.partnerId == user.id }?.let { onChatClick(it) }
                                    }
                            ) {
                                LovyAvatar(
                                    imageUrl = user.avatarUrl,
                                    size = 56.dp,
                                    isOnline = user.isOnline,
                                    hasStoryRing = true,
                                    contentDescription = user.name
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = user.name.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${user.distanceMeters}m",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = LovyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedFilterIndex,
                    edgePadding = 20.dp,
                    divider = {},
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    filterTabs.forEachIndexed { index, title ->
                        val isSelected = selectedFilterIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedFilterIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) LovyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // Chat Items
            if (filteredChats.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 30.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(LovyPrimary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddComment,
                                    contentDescription = null,
                                    tint = LovyPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Belum Ada Obrolan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Mulai obrolan baru dengan mencari teman di radar sekitar atau bergabung dengan komunitas grup.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToNearby,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LovyPrimary),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Cari Teman Sekitar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredChats, key = { it.id }) { chat ->
                    ChatItemRow(
                        chat = chat,
                        onClick = { onChatClick(chat) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToNearby,
            containerColor = LovyPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_new_chat")
        ) {
            Icon(Icons.Default.AddComment, contentDescription = "Cari Teman Sekitar")
        }
    }
}

@Composable
fun ChatItemRow(
    chat: Chat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
            .testTag("chat_item_${chat.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LovyAvatar(
                imageUrl = chat.avatarUrl,
                size = 52.dp,
                isOnline = chat.isOnline,
                contentDescription = chat.title
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chat.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chat.isGroup) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Grup",
                                tint = LovySecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = chat.lastTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (chat.unreadCount > 0) LovyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.isE2EEncrypted) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "E2EE",
                            tint = LovyPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = LovyPrimary,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
