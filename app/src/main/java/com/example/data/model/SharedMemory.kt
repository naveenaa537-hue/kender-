package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shared_memories")
data class SharedMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val imageUri: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val location: String = "",
    val tag: String = "Memory",
    val heartCount: Int = 1,
    val isFavorite: Boolean = false,
    val createdBy: String = "user_me"
)
