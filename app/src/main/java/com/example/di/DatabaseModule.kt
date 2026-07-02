package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.HistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "qr_app_database"
        )
            // Bumping the schema without a real migration wipes local scan/generation
            // history, which is acceptable for this app (no server backup of history).
            // Replace with proper Migration objects if that stops being true.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase): HistoryDao = database.historyDao()
}
