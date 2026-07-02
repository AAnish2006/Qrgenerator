package com.example.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrRepository @Inject constructor(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()

    suspend fun insert(item: HistoryItem) {
        historyDao.insertHistoryItem(item)
    }

    suspend fun deleteById(id: Int) {
        historyDao.deleteHistoryItemById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }
}
