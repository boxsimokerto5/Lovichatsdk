package com.example.data.model

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MessageType {
    TEXT,
    PHOTO
}

data class User(
    val id: String,
    val name: String,
    val username: String,
    val email: String = "",
    val avatarUrl: String,
    val bio: String,
    val gender: String, // "Wanita", "Pria"
    val isOnline: Boolean = true,
    val distanceMeters: Int = 0,
    val safetyNumber: String = "",
    val isGhostMode: Boolean = false
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val isMine: Boolean,
    val plainText: String,
    val cipherText: String,
    val iv: String,
    val timestamp: Long,
    val formattedTime: String,
    val status: MessageStatus = MessageStatus.READ,
    val type: MessageType = MessageType.TEXT,
    val photoUrl: String? = null,
    val photoBase64: String? = null,
    val isE2EVerified: Boolean = true
)

data class Chat(
    val id: String,
    val title: String,
    val avatarUrl: String,
    val isGroup: Boolean = false,
    val participantIds: List<String> = emptyList(),
    val participantCount: Int = 2,
    val lastMessage: String = "",
    val lastTimestamp: String = "",
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isE2EEncrypted: Boolean = true,
    val partnerId: String = ""
)

data class NearbyUser(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val bio: String,
    val avatarUrl: String,
    val distanceMeters: Int,
    val isOnline: Boolean,
    val mutualInterests: List<String>,
    val lastActive: String,
    val isSayHiSent: Boolean = false
)

data class GroupCommunity(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val avatarUrl: String,
    val memberCount: Int,
    val isJoined: Boolean = false,
    val tags: List<String>
)
