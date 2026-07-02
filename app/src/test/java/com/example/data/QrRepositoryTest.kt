package com.example.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: QrRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = QrRepository(database.historyDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert then read returns the item, newest first`() = runBlocking {
        repository.insert(HistoryItem(type = "GENERATED", qrType = "TEXT", title = "First", content = "one"))
        repository.insert(HistoryItem(type = "GENERATED", qrType = "TEXT", title = "Second", content = "two"))

        val history = repository.allHistory.first()

        assertEquals(2, history.size)
        assertEquals("two", history.first().content) // most recent insert first
    }

    @Test
    fun `deleteById removes only the matching row`() = runBlocking {
        repository.insert(HistoryItem(id = 1, type = "SCANNED", qrType = "URL", title = "a", content = "a"))
        repository.insert(HistoryItem(id = 2, type = "SCANNED", qrType = "URL", title = "b", content = "b"))

        repository.deleteById(1)

        val history = repository.allHistory.first()
        assertEquals(1, history.size)
        assertEquals(2, history.first().id)
    }

    @Test
    fun `clearHistory empties the table`() = runBlocking {
        repository.insert(HistoryItem(type = "SCANNED", qrType = "URL", title = "a", content = "a"))
        repository.clearHistory()

        val history = repository.allHistory.first()
        assertTrue(history.isEmpty())
    }
}
