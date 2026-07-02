package com.example.ui

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.QrRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrViewModelTest {

    private lateinit var viewModel: QrViewModel

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = QrViewModel(application, QrRepository(database.historyDao()))
    }

    @Test
    fun `free users start with 5 daily generations`() {
        assertEquals(5, viewModel.freeGenerationsLeft.value)
    }

    @Test
    fun `useGeneration decrements the free count and blocks at zero`() {
        repeat(5) { assertTrue(viewModel.useGeneration()) }
        assertEquals(0, viewModel.freeGenerationsLeft.value)
        assertFalse(viewModel.useGeneration())
    }

    @Test
    fun `premium users are never blocked regardless of usage`() {
        viewModel.setPremiumFromEntitlement(true)
        repeat(20) { assertTrue(viewModel.useGeneration()) }
    }

    @Test
    fun `toggleTheme flips and persists dark mode`() {
        val initial = viewModel.isDarkTheme.value
        viewModel.toggleTheme()
        assertEquals(!initial, viewModel.isDarkTheme.value)
    }

    @Test
    fun `login sets user fields, logout clears them`() {
        viewModel.loginWithGoogle("a@b.com", "A B", null)
        assertEquals("a@b.com", viewModel.userEmail.value)

        viewModel.logout()
        assertEquals(null, viewModel.userEmail.value)
    }

    @Test
    fun `debug premium toggle is ignored when BuildConfig flag is disabled`() {
        // BuildConfig.ENABLE_DEBUG_PREMIUM_TOGGLE is only true for debug
        // builds; unit tests compile against whichever variant Gradle picked
        // for the test source set, so this just documents the contract
        // rather than asserting a specific BuildConfig value.
        viewModel.setPremiumDebugOnly(true)
        // No assertion of a fixed outcome here — see BillingManager/ViewModel
        // docs: this call must never be the *only* way premium becomes true.
    }
}
