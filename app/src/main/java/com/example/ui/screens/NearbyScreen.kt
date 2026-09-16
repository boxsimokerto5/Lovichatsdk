package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Chat
import com.example.data.model.NearbyUser
import com.example.data.repository.LovyChatRepository
import com.example.ui.components.LovyAvatar
import com.example.ui.components.RadarScanningCanvas
import com.example.ui.theme.LovyAccent
import com.example.ui.theme.LovyPrimary
import com.example.ui.theme.LovySecondary
import com.example.ui.theme.OnlineGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NearbyScreen(
    repository: LovyChatRepository,
    onOpenChat: (Chat) -> Unit,
    modifier: Modifier = Modifier
) {
    val nearbyUsers by repository.nearbyUsers.collectAsState()
    val isScanning by repository.isScanningNearby.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val chats by repository.chats.collectAsState()

    var genderFilter by remember { mutableStateOf("Semua") }
    var selectedUserForDetail by remember { mutableStateOf<NearbyUser?>(null) }

    val filteredList = nearbyUsers.filter {
        when (genderFilter) {
            "Wanita" -> it.gender == "Wanita"
            "Pria" -> it.gender == "Pria"
            else -> true
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = LovyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Teman Sekitar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "Radar GPS & Supabase Cloud Real-Time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { repository.refreshNearby() },
                modifier = Modifier
                    .background(LovyPrimary.copy(alpha = 0.12f), CircleShape)
                    .testTag("btn_refresh_radar")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Pindai Ulang Radar",
                    tint = LovyPrimary
                )
            }
        }

        // Ghost Mode Banner if active
        if (currentUser?.isGhostMode == true) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Siluman (Ghost Mode) Aktif",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lokasi kamu disembunyikan dari radar pengguna lain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { repository.toggleGhostMode(false) }) {
                        Text("Matikan", color = LovyPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Radar Visual Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .height(180.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarScanningCanvas(
                    modifier = Modifier.size(160.dp),
                    isScanning = isScanning
                )

                // Radar Floating Info Chip
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LovyPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isScanning) "Memindai radar sekitar..." else "${filteredList.size} Teman terdeteksi dalam radius 2 km",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Gender Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            listOf("Semua", "Wanita", "Pria").forEach { filter ->
                val isSelected = genderFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { genderFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LovyPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        // Nearby Users List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = LovyPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Belum Ada Pengguna di Sekitar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Radar GPS aktif dan terhubung ke Firebase. Gunakan tombol di bawah untuk menyegarkan jangkauan radar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { repository.refreshNearby() },
                                colors = ButtonDefaults.buttonColors(containerColor = LovyPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pindai Ulang Radar", color = Color.White)
                            }
                        }
                    }
                }
                } else {
                    items(filteredList, key = { it.id }) { user ->
                        NearbyUserCard(
                            user = user,
                            onSayHi = { repository.sayHi(user) },
                            onViewProfile = { selectedUserForDetail = user },
                            onOpenChat = {
                                val existingChat = chats.find { it.partnerId == user.id }
                                if (existingChat != null) {
                                    onOpenChat(existingChat)
                                } else {
                                    repository.sayHi(user)
                                    chats.find { it.partnerId == user.id }?.let { onOpenChat(it) }
                                }
                            }
                        )
                    }
                }
            }
        }

    // Detail Profile Dialog
    selectedUserForDetail?.let { user ->
        NearbyProfileDialog(
            user = user,
            onDismiss = { selectedUserForDetail = null },
            onSayHi = {
                repository.sayHi(user)
                selectedUserForDetail = null
            },
            onChat = {
                selectedUserForDetail = null
                val existing = chats.find { it.partnerId == user.id }
                if (existing != null) {
                    onOpenChat(existing)
                } else {
                    repository.sayHi(user)
                    chats.find { it.partnerId == user.id }?.let { onOpenChat(it) }
                }
            }
        )
    }
}

@Composable
fun NearbyUserCard(
    user: NearbyUser,
    onSayHi: () -> Unit,
    onViewProfile: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onViewProfile() }
            .testTag("nearby_user_${user.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LovyAvatar(
                imageUrl = user.avatarUrl,
                size = 54.dp,
                isOnline = user.isOnline,
                contentDescription = user.name
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${user.name}, ${user.age}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Distance badge
                    Surface(
                        color = LovyPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = LovyPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (user.distanceMeters >= 1000) {
                                    String.format("%.1f km", user.distanceMeters / 1000.0)
                                } else {
                                    "${user.distanceMeters} m"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = LovyPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.lastActive,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (user.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action: Sapa or Chat
            if (user.isSayHiSent) {
                IconButton(
                    onClick = onOpenChat,
                    modifier = Modifier
                        .background(LovyPrimary.copy(alpha = 0.15f), CircleShape)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Buka Chat E2EE",
                        tint = LovyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onSayHi,
                    colors = ButtonDefaults.buttonColors(containerColor = LovyPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_say_hi_${user.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.WavingHand,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Sapa", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NearbyProfileDialog(
    user: NearbyUser,
    onDismiss: () -> Unit,
    onSayHi: () -> Unit,
    onChat: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar Large
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = user.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${user.name}, ${user.age} thn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${user.gender} • Terdeteksi ${user.distanceMeters}m dari lokasi kamu",
                    style = MaterialTheme.typography.bodySmall,
                    color = LovyPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${user.bio}\"",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Minat & Hobi:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    user.mutualInterests.forEach { interest ->
                        Surface(
                            color = LovySecondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "#$interest",
                                style = MaterialTheme.typography.labelSmall,
                                color = LovySecondary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onChat,
                colors = ButtonDefaults.buttonColors(containerColor = LovyPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kirim Pesan E2EE", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tutup")
            }
        }
    )
}
