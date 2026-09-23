package com.example.geonote.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.geonote.data.entity.TravelEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelEntryDao {
    @Query("SELECT * FROM travel_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<TravelEntry>>

    @Query("SELECT * FROM travel_entries WHERE id = :id")
    suspend fun getEntryById(id: Int): TravelEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TravelEntry)

    @Delete
    suspend fun delete(entry: TravelEntry)
}