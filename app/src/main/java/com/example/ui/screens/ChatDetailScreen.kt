package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.crypto.CryptoHelper
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.repository.LovyChatRepository
import com.example.ui.components.CiphertextInspectorDialog
import com.example.ui.components.E2EEBanner
import com.example.ui.components.LovyAvatar
import com.example.ui.components.PhotoLightboxDialog
import com.example.ui.components.SafetyNumberDialog
import com.example.ui.theme.LovyAccent
import com.example.ui.theme.LovyPrimary
import com.example.ui.theme.LovySecondary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SecurityShieldBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    repository: LovyChatRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messagesMap by repository.messages.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val chatMessages = messagesMap[chat.id] ?: emptyList()

    var inputText by remember { mutableStateOf("") }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }
    var selectedMessageForCrypto by remember { mutableStateOf<Message?>(null) }
    var selectedPhotoForLightbox by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var showSafetyDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom on new message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showSafetyDialog = true }
                    ) {
                        LovyAvatar(
                            imageUrl = chat.avatarUrl,
                            size = 40.dp,
                            isOnline = chat.isOnline,
                            contentDescription = chat.title
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = chat.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Terenkripsi E2EE",
                                    tint = LovyPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = if (chat.isGroup) "${chat.participantCount} Anggota • E2EE Aktif" else if (chat.isOnline) "Online • E2EE Aktif" else "Offline • E2EE Aktif",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (chat.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showSafetyDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Nomor Keamanan E2EE",
                            tint = SecurityShieldBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Chat Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo attach button
                    IconButton(
                        onClick = { showPhotoPickerSheet = true },
                        modifier = Modifier
                            .background(LovyPrimary.copy(alpha = 0.12f), CircleShape)
                            .size(42.dp)
                            .testTag("btn_attach_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Kirim Foto Real-Time",
                            tint = LovyPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ketik pesan terenkripsi...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LovyPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                repository.sendMessage(chat.id, inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .background(
                                if (inputText.isNotBlank()) LovyPrimary else Color.Gray.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .size(44.dp)
                            .testTag("btn_send_message")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim Pesan",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // E2EE Banner
            E2EEBanner(
                onClick = { showSafetyDialog = true },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Message History List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(chatMessages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isGroup = chat.isGroup,
                        onInspectCrypto = { selectedMessageForCrypto = msg },
                        onPhotoClick = { url -> selectedPhotoForLightbox = Pair(url, msg.plainText) }
                    )
                }
            }
        }
    }

    // Photo Picker Bottom Sheet
    if (showPhotoPickerSheet) {
        PhotoShareBottomSheet(
            onDismiss = { showPhotoPickerSheet = false },
            onSendPhoto = { url, caption ->
                repository.sendPhoto(chat.id, url, caption)
                showPhotoPickerSheet = false
            }
        )
    }

    // Ciphertext Inspector Modal
    selectedMessageForCrypto?.let { msg ->
        CiphertextInspectorDialog(
            plainText = msg.plainText,
            cipherText = msg.cipherText,
            iv = msg.iv,
            onDismiss = { selectedMessageForCrypto = null }
        )
    }

    // Safety Number Dialog
    if (showSafetyDialog) {
        val myId = currentUser?.id ?: "me"
        val safetyFingerprint = CryptoHelper.generateFingerprint(myId, chat.partnerId.ifBlank { "group_${chat.id}" })
        SafetyNumberDialog(
            userName = chat.title,
            safetyNumber = safetyFingerprint,
            onDismiss = { showSafetyDialog = false }
        )
    }

    // Photo Lightbox
    selectedPhotoForLightbox?.let { (url, caption) ->
        PhotoLightboxDialog(
            photoUrl = url,
            caption = caption,
            onDismiss = { selectedPhotoForLightbox = null }
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isGroup: Boolean,
    onInspectCrypto: () -> Unit,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMine = message.isMine
    val bubbleColor = if (isMine) LovyPrimary else MaterialTheme.colorScheme.surface
    val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
    val timeColor = if (isMine) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
    val borderStroke = if (isMine) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))

    val alignment = if (isMine) Alignment.End else Alignment.Start
    val shape = if (isMine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("message_bubble_${message.id}"),
        horizontalAlignment = alignment
    ) {
        // Group sender name if incoming
        if (!isMine && isGroup) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = LovySecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )
        }

        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            border = borderStroke,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Photo Attachment if present
                if (message.type == MessageType.PHOTO && !message.photoUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPhotoClick(message.photoUrl) }
                    ) {
                        AsyncImage(
                            model = message.photoUrl,
                            contentDescription = message.plainText,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // E2EE watermark badge
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = OnlineGreen, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("E2EE Photo", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Text Content
                if (message.plainText.isNotBlank()) {
                    Text(
                        text = message.plainText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer: Timestamp, Read status ticks, and Ciphertext inspect button
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small lock badge to inspect ciphertext
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Inspeksi Ciphertext",
                        tint = if (isMine) Color.White.copy(alpha = 0.7f) else LovyPrimary.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(13.dp)
                            .clickable { onInspectCrypto() }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = message.formattedTime,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = timeColor
                    )

                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.SENDING -> {
                                Text("...", color = timeColor, style = MaterialTheme.typography.labelSmall)
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Terkirim",
                                    tint = timeColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Diterima",
                                    tint = timeColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            MessageStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Dibaca",
                                    tint = LovyAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Bottom Sheet for Real-time Photo Sharing
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoShareBottomSheet(
    onDismiss: () -> Unit,
    onSendPhoto: (url: String, caption: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedPresetUrl by remember {
        mutableStateOf("https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=500&fit=crop")
    }
    var customUrlInput by remember { mutableStateOf("") }
    var captionText by remember { mutableStateOf("") }

    val presetPhotos = listOf(
        "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=500&fit=crop" to "Kopi & Kafe ☕",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&fit=crop" to "Pantai Sunset 🌅",
        "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=500&fit=crop" to "Makan Malam 🍜",
        "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=500&fit=crop" to "Olahraga 🏃",
        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&fit=crop" to "Musik Akustik 🎸"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = LovyPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Berbagi Foto Real-Time (E2EE)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Foto dienkripsi di perangkat kamu sebelum dikirim ke Firebase Storage.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text("Pilih Foto Preset Cepat:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                items(presetPhotos) { (url, label) ->
                    val isSelected = selectedPresetUrl == url && customUrlInput.isBlank()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(88.dp)
                            .clickable {
                                selectedPresetUrl = url
                                customUrlInput = ""
                            }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = label,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) Modifier.background(LovyPrimary).padding(3.dp).clip(RoundedCornerShape(10.dp)) else Modifier
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = captionText,
                onValueChange = { captionText = it },
                label = { Text("Keterangan Foto (Opsional)") },
                placeholder = { Text("Contoh: Lagi asik nongkrong nih!") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("input_photo_caption")
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val finalUrl = customUrlInput.ifBlank { selectedPresetUrl }
                    onSendPhoto(finalUrl, captionText.ifBlank { "📷 Berbagi foto real-time" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = LovyPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_confirm_send_photo")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enkripsi & Kirim Foto", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
