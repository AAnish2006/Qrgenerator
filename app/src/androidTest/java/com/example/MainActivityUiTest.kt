package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end smoke test for the logged-out state: a fresh install (no saved
 * user in EncryptedSharedPreferences) must land on LoginScreen, not crash or
 * show the main dashboard.
 */
@HiltAndroidTest
class MainActivityUiTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun loginScreen_isShownOnFirstLaunch() {
        composeRule.onNodeWithText("Sign in with Google", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }
}
