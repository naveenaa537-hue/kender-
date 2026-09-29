package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bucket_items")
data class BucketItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Adventure",
    val isCompleted: Boolean = false,
    val completedDateMillis: Long? = null,
    val targetDateMillis: Long? = null,
    val imageUri: String? = null,
    val createdBy: String = "user_me"
)
