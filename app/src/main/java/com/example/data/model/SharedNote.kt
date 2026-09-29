package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shared_notes")
data class SharedNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val colorHex: String = "#FFF9C4",
    val isPinned: Boolean = false,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val createdBy: String = "user_me",
    val isChecklist: Boolean = false
)
