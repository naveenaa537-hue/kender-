package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_questions")
data class DailyQuestion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateKey: String, // e.g. "2026-09-28"
    val questionText: String,
    val category: String = "Friendship Bond",
    val friend1Answer: String? = null,
    val friend2Answer: String? = null,
    val friend1AnswerTime: Long? = null,
    val friend2AnswerTime: Long? = null,
    val isRevealed: Boolean = false
) {
    fun areBothAnswered(): Boolean = !friend1Answer.isNullOrBlank() && !friend2Answer.isNullOrBlank()
}
