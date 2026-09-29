package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE,
    FILE,
    GIF
}

enum class DeliveryStatus {
    SENDING,
    SENT,
    DELIVERED,
    SEEN
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderId: String, // "user_me" or "user_friend"
    val senderName: String,
    val receiverId: String,
    val content: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val mediaUri: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0L,
    val voiceDurationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val replyToMessageId: Long? = null,
    val replyToContent: String? = null,
    val replyToSender: String? = null,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,
    val seenTimestamp: Long? = null,
    val reactions: String = "" // format: "userId:emoji;userId2:emoji"
) {
    fun parseReactions(): Map<String, String> {
        if (reactions.isBlank()) return emptyMap()
        return reactions.split(";")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
    }
}
