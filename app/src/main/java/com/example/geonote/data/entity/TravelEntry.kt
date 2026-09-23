package com.example.geonote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_entries")
data class TravelEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoUri: String? = null,
    val date: Long = System.currentTimeMillis(), // timestamp
    val createdAt: Long = System.currentTimeMillis(),
    val cityName: String? = null,
    val weatherInfo: String? = null
)