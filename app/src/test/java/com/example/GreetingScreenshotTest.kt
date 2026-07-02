package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.QrRepository
import com.example.ui.LoginScreen
import com.example.ui.QrViewModel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    // QrViewModel is normally supplied by Hilt (hiltViewModel()); for this
    // plain Robolectric screenshot test we build its dependencies by hand
    // with an in-memory database instead of pulling in the full Hilt test
    // graph.
    val database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val viewModel = QrViewModel(application, QrRepository(database.historyDao()))

    composeTestRule.setContent { 
      MyApplicationTheme { 
        LoginScreen(viewModel = viewModel, onLoginSuccess = {}) 
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
