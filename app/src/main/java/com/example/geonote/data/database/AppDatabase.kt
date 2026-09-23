package com.example.geonote.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.geonote.data.dao.TravelEntryDao
import com.example.geonote.data.entity.TravelEntry

@Database(entities = [TravelEntry::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun travelEntryDao(): TravelEntryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geonote_database"
                )
                    .fallbackToDestructiveMigration() // Supprime l'ancienne base si version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}