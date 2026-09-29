package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessage
import com.example.data.model.DeliveryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): ChatMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET isDeleted = 1, content = 'This message was deleted' WHERE id = :id")
    suspend fun softDeleteMessage(id: Long)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun hardDeleteMessage(id: Long)

    @Query("UPDATE chat_messages SET content = :newContent, isEdited = 1 WHERE id = :id")
    suspend fun editMessageContent(id: Long, newContent: String)

    @Query("UPDATE chat_messages SET reactions = :reactions WHERE id = :id")
    suspend fun updateReactions(id: Long, reactions: String)

    @Query("UPDATE chat_messages SET deliveryStatus = :status WHERE id = :id")
    suspend fun updateDeliveryStatus(id: Long, status: DeliveryStatus)

    @Query("UPDATE chat_messages SET deliveryStatus = 'SEEN', seenTimestamp = :seenTime WHERE receiverId = :receiverId AND deliveryStatus != 'SEEN'")
    suspend fun markMessagesAsSeen(receiverId: String, seenTime: Long)

    @Query("SELECT * FROM chat_messages WHERE content LIKE '%' || :query || '%' OR fileName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<ChatMessage>>

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
}
