package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SharedNote
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedNoteDao {
    @Query("SELECT * FROM shared_notes ORDER BY isPinned DESC, lastUpdatedMillis DESC")
    fun getAllNotesFlow(): Flow<List<SharedNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SharedNote): Long

    @Update
    suspend fun updateNote(note: SharedNote)

    @Query("DELETE FROM shared_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("UPDATE shared_notes SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePin(id: Long)
}
