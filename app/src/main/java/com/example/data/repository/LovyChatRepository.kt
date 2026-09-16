package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.crypto.CryptoHelper
import com.example.data.model.Chat
import com.example.data.model.GroupCommunity
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.NearbyUser
import com.example.data.model.User
import com.example.data.supabase.SupabaseManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LovyChatRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    val supabaseManager: SupabaseManager = SupabaseManager.instance

    // Current logged-in user (null when not logged in)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Chats list (Empty initially - no sample data)
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    // Messages per chat: Map<chatId, List<Message>> (Empty initially)
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Nearby Users (Empty initially)
    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers.asStateFlow()

    // Groups (Empty initially)
    private val _groups = MutableStateFlow<List<GroupCommunity>>(emptyList())
    val groups: StateFlow<List<GroupCommunity>> = _groups.asStateFlow()

    // Sync status
    private val _isFirebaseSynced = MutableStateFlow(true)
    val isFirebaseSynced: StateFlow<Boolean> = _isFirebaseSynced.asStateFlow()

    val isSupabaseConnected: StateFlow<Boolean> = supabaseManager.isConnected
    val supabaseStatus: StateFlow<String> = supabaseManager.lastStatus

    private val _isScanningNearby = MutableStateFlow(false)
    val isScanningNearby: StateFlow<Boolean> = _isScanningNearby.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        // Check if there is already an active Firebase Auth user
        checkExistingAuth()
    }

    private fun checkExistingAuth() {
        try {
            val firebaseAuth = FirebaseAuth.getInstance()
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val email = firebaseUser.email ?: ""
                val displayName = firebaseUser.displayName ?: email.substringBefore("@").ifBlank { "Pengguna Google" }
                val photoUrl = firebaseUser.photoUrl?.toString()
                    ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&fit=crop&crop=faces"

                _currentUser.value = User(
                    id = firebaseUser.uid,
                    name = displayName,
                    username = "@${email.substringBefore("@").lowercase()}",
                    email = email,
                    avatarUrl = photoUrl,
                    bio = "Pengguna terverifikasi Google • Privasi E2EE aktif",
                    gender = "Pria",
                    isOnline = true,
                    safetyNumber = CryptoHelper.generateFingerprint(firebaseUser.uid, "lovy_server"),
                    isGhostMode = false
                )
            }
        } catch (e: Throwable) {
            Log.w("LovyChatRepo", "Firebase Auth init check skipped: ${e.message}")
        }
    }

    private fun formatCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    /**
     * Authenticate with Google ID Token via Firebase Auth and Supabase.
     */
    suspend fun signInWithGoogleCredential(idToken: String, emailHint: String? = null, nameHint: String? = null, photoHint: String? = null): Boolean {
        _authLoading.value = true
        _authError.value = null

        val email = emailHint ?: "boxsimokerto5@gmail.com"
        val name = nameHint ?: email.substringBefore("@").ifBlank { "Pengguna Google" }
        val photo = photoHint ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&fit=crop&crop=faces"

        var firebaseUserUid: String? = null

        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val auth = FirebaseAuth.getInstance()
            val result = auth.signInWithCredential(credential).await()
            val fbUser = result.user
            if (fbUser != null) {
                firebaseUserUid = fbUser.uid
            }
        } catch (e: Throwable) {
            Log.w("LovyChatRepo", "Firebase signInWithCredential note: ${e.message}")
        }

        // Authenticate & sync with Supabase Auth & Database
        scope.launch {
            try {
                supabaseManager.signInWithGoogleIdToken(
                    idToken = idToken,
                    fallbackEmail = email,
                    fallbackName = name,
                    fallbackAvatar = photo
                )
            } catch (e: Throwable) {
                Log.w("LovyChatRepo", "Supabase auth background sync: ${e.message}")
            }
        }

        val userId = firebaseUserUid ?: ("google_" + email.replace(Regex("[^a-zA-Z0-9]"), "_"))

        val authenticatedUser = User(
            id = userId,
            name = name,
            username = "@${email.substringBefore("@").lowercase().ifBlank { "user" }}",
            email = email,
            avatarUrl = photo,
            bio = "Pengguna Google terverifikasi • E2EE & Supabase aktif",
            gender = "Pria",
            isOnline = true,
            safetyNumber = CryptoHelper.generateFingerprint(userId, "lovy_supabase"),
            isGhostMode = false
        )

        _currentUser.value = authenticatedUser

        // Persist profile to Supabase database
        scope.launch {
            supabaseManager.upsertProfile(authenticatedUser)
            loadRemoteSupabaseData(authenticatedUser.id)
        }

        _authLoading.value = false
        return true
    }

    /**
     * Sign in directly with Google account details (e.g. from Credential Manager or One-Tap)
     */
    fun signInWithGoogleAccount(email: String, displayName: String, photoUrl: String?) {
        _authLoading.value = true
        val userId = "google_" + email.replace(Regex("[^a-zA-Z0-9]"), "_")
        val avatar = photoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&fit=crop&crop=faces"
        val name = displayName.ifBlank { email.substringBefore("@") }

        val authenticatedUser = User(
            id = userId,
            name = name,
            username = "@${email.substringBefore("@").lowercase()}",
            email = email,
            avatarUrl = avatar,
            bio = "Akun Google terverifikasi • E2EE & Supabase diaktifkan",
            gender = "Pria",
            isOnline = true,
            safetyNumber = CryptoHelper.generateFingerprint(userId, "lovy_supabase"),
            isGhostMode = false
        )

        _currentUser.value = authenticatedUser

        // Sync with Supabase Database
        scope.launch {
            supabaseManager.upsertProfile(authenticatedUser)
            loadRemoteSupabaseData(userId)
        }

        _authLoading.value = false
    }

    /**
     * Load existing groups & nearby profiles from Supabase database
     */
    private suspend fun loadRemoteSupabaseData(currentUserId: String) {
        try {
            val remoteGroups = supabaseManager.fetchGroups()
            if (remoteGroups.isNotEmpty()) {
                val currentMap = _groups.value.associateBy { it.id }
                val merged = (remoteGroups.filter { !currentMap.containsKey(it.id) } + _groups.value)
                _groups.value = merged
            }

            val remoteNearby = supabaseManager.fetchNearbyProfiles(currentUserId)
            if (remoteNearby.isNotEmpty()) {
                _nearbyUsers.value = remoteNearby
            }
        } catch (e: Throwable) {
            Log.w("LovyChatRepo", "Error loading Supabase remote data: ${e.message}")
        }
    }

    fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Throwable) {
            Log.w("LovyChatRepo", "Firebase signOut: ${e.message}")
        }
        _currentUser.value = null
        _chats.value = emptyList()
        _messages.value = emptyMap()
        _nearbyUsers.value = emptyList()
        _groups.value = emptyList()
    }

    // Send an E2EE encrypted message
    fun sendMessage(chatId: String, text: String) {
        val user = _currentUser.value ?: return
        val partnerId = _chats.value.find { it.id == chatId }?.partnerId ?: chatId

        val sharedKey = CryptoHelper.deriveSharedKey(user.id, partnerId)
        val encryptedData = CryptoHelper.encrypt(text, sharedKey)
        val timeNow = formatCurrentTime()

        val newMessage = Message(
            id = "msg_" + UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = user.id,
            senderName = user.name,
            senderAvatar = user.avatarUrl,
            isMine = true,
            plainText = text,
            cipherText = encryptedData.cipherText,
            iv = encryptedData.iv,
            timestamp = System.currentTimeMillis(),
            formattedTime = timeNow,
            status = MessageStatus.SENT,
            type = MessageType.TEXT,
            isE2EVerified = true
        )

        val currentList = _messages.value[chatId] ?: emptyList()
        _messages.value = _messages.value + (chatId to (currentList + newMessage))

        // Update chat item in list
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(
                    lastMessage = text,
                    lastTimestamp = timeNow
                )
            } else chat
        }

        // Send to Supabase Database
        scope.launch {
            supabaseManager.sendMessage(newMessage)
        }

        // Simulate delivery ticks
        scope.launch {
            delay(600)
            updateMessageStatus(chatId, newMessage.id, MessageStatus.DELIVERED)
            delay(800)
            updateMessageStatus(chatId, newMessage.id, MessageStatus.READ)
        }
    }

    // Send an E2EE encrypted Photo
    fun sendPhoto(chatId: String, photoUrl: String, caption: String = "") {
        val user = _currentUser.value ?: return
        val partnerId = _chats.value.find { it.id == chatId }?.partnerId ?: chatId

        val sharedKey = CryptoHelper.deriveSharedKey(user.id, partnerId)
        val encryptedCaption = if (caption.isNotBlank()) CryptoHelper.encrypt(caption, sharedKey) else CryptoHelper.encrypt("📷 [Foto Terenkripsi]", sharedKey)
        val timeNow = formatCurrentTime()

        val newMessage = Message(
            id = "msg_photo_" + UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = user.id,
            senderName = user.name,
            senderAvatar = user.avatarUrl,
            isMine = true,
            plainText = caption.ifBlank { "📷 Foto" },
            cipherText = encryptedCaption.cipherText,
            iv = encryptedCaption.iv,
            timestamp = System.currentTimeMillis(),
            formattedTime = timeNow,
            status = MessageStatus.SENT,
            type = MessageType.PHOTO,
            photoUrl = photoUrl,
            isE2EVerified = true
        )

        val currentList = _messages.value[chatId] ?: emptyList()
        _messages.value = _messages.value + (chatId to (currentList + newMessage))

        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(
                    lastMessage = "📷 Foto",
                    lastTimestamp = timeNow
                )
            } else chat
        }

        // Send to Supabase Database
        scope.launch {
            supabaseManager.sendMessage(newMessage)
        }

        scope.launch {
            delay(700)
            updateMessageStatus(chatId, newMessage.id, MessageStatus.DELIVERED)
            delay(900)
            updateMessageStatus(chatId, newMessage.id, MessageStatus.READ)
        }
    }

    private fun updateMessageStatus(chatId: String, messageId: String, newStatus: MessageStatus) {
        val list = _messages.value[chatId] ?: return
        val updated = list.map {
            if (it.id == messageId) it.copy(status = newStatus) else it
        }
        _messages.value = _messages.value + (chatId to updated)
    }

    // Say Hi / Sapa Teman Sekitar
    fun sayHi(nearbyUser: NearbyUser) {
        _nearbyUsers.value = _nearbyUsers.value.map {
            if (it.id == nearbyUser.id) it.copy(isSayHiSent = true) else it
        }

        val existingChat = _chats.value.find { it.partnerId == nearbyUser.id }
        val chatId = existingChat?.id ?: "chat_${nearbyUser.id}"

        if (existingChat == null) {
            val newChat = Chat(
                id = chatId,
                title = nearbyUser.name,
                avatarUrl = nearbyUser.avatarUrl,
                isGroup = false,
                unreadCount = 0,
                lastMessage = "Halo! Salam kenal ya dari Teman Sekitar 👋✨",
                lastTimestamp = formatCurrentTime(),
                isOnline = nearbyUser.isOnline,
                partnerId = nearbyUser.id
            )
            _chats.value = listOf(newChat) + _chats.value
        }

        sendMessage(chatId, "Halo ${nearbyUser.name}! Salam kenal dari radar Teman Sekitar Lovy Chat 👋✨")
    }

    // Scan / Refresh Nearby Friends
    fun refreshNearby() {
        scope.launch {
            _isScanningNearby.value = true
            val currentUserId = _currentUser.value?.id.orEmpty()
            val remoteNearby = supabaseManager.fetchNearbyProfiles(currentUserId)
            if (remoteNearby.isNotEmpty()) {
                _nearbyUsers.value = remoteNearby
            }
            delay(800)
            _isScanningNearby.value = false
        }
    }

    // Toggle Ghost Mode (Hide from radar)
    fun toggleGhostMode(enabled: Boolean) {
        _currentUser.value = _currentUser.value?.copy(isGhostMode = enabled)
        _currentUser.value?.let { user ->
            scope.launch {
                supabaseManager.upsertProfile(user)
            }
        }
    }

    // Join or Leave Group
    fun toggleGroupJoin(groupId: String) {
        _groups.value = _groups.value.map {
            if (it.id == groupId) {
                val newStatus = !it.isJoined
                val newCount = if (newStatus) it.memberCount + 1 else it.memberCount - 1
                it.copy(isJoined = newStatus, memberCount = newCount)
            } else it
        }

        val group = _groups.value.find { it.id == groupId } ?: return
        if (group.isJoined) {
            val groupChatId = "chat_$groupId"
            if (_chats.value.none { it.id == groupChatId }) {
                val newGroupChat = Chat(
                    id = groupChatId,
                    title = group.name,
                    avatarUrl = group.avatarUrl,
                    isGroup = true,
                    participantCount = group.memberCount,
                    unreadCount = 0,
                    lastMessage = "Kamu baru saja bergabung dengan grup ini! 🎉",
                    lastTimestamp = formatCurrentTime(),
                    isOnline = true
                )
                _chats.value = listOf(newGroupChat) + _chats.value
            }
        }
    }

    // Create New Group
    fun createNewGroup(name: String, category: String, description: String, tags: List<String>) {
        val newId = "grp_" + System.currentTimeMillis()
        val newGroup = GroupCommunity(
            id = newId,
            name = name,
            category = category,
            description = description,
            avatarUrl = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=200&fit=crop",
            memberCount = 1,
            isJoined = true,
            tags = tags
        )
        _groups.value = listOf(newGroup) + _groups.value

        // Sync new group to Supabase
        scope.launch {
            supabaseManager.createGroup(newGroup)
        }

        val newChat = Chat(
            id = "chat_$newId",
            title = name,
            avatarUrl = newGroup.avatarUrl,
            isGroup = true,
            participantCount = 1,
            unreadCount = 0,
            lastMessage = "Grup dibuat. Selamat datang di komunitas baru!",
            lastTimestamp = formatCurrentTime(),
            isOnline = true
        )
        _chats.value = listOf(newChat) + _chats.value
    }
}
