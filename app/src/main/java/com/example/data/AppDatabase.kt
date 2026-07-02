package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The single Room database instance is created and scoped as a @Singleton by
 * Hilt (see di/DatabaseModule.kt), so there's no manual getInstance()
 * companion here — that would risk creating a second, competing instance.
 */
@Database(entities = [HistoryItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
