package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billing.BillingManager
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var billingManager: BillingManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QrViewModel = hiltViewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            // BillingManager is the single, trusted source that flips
            // premium on/off — it re-verifies against Play Billing itself
            // rather than accepting a value from the UI.
            val manager = remember {
                BillingManager(applicationContext) { hasPremium ->
                    viewModel.setPremiumFromEntitlement(hasPremium)
                }.also {
                    billingManager = it
                    it.startConnection()
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val userEmail by viewModel.userEmail.collectAsState()

                if (userEmail == null) {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            // Successfully logged in
                        }
                    )
                } else {
                    MainAppScaffold(viewModel = viewModel, billingManager = manager)
                }
            }
        }
    }

    override fun onDestroy() {
        billingManager?.endConnection()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: QrViewModel, billingManager: BillingManager) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Scanner, 2: Generator
    var isShowingPremiumScreen by remember { mutableStateOf(false) }

    if (isShowingPremiumScreen) {
        PremiumScreen(
            viewModel = viewModel,
            billingManager = billingManager,
            onBack = { isShowingPremiumScreen = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                0 -> "QR PULSE DASHBOARD"
                                1 -> "LIVE QR SCANNER"
                                else -> "MULTI-FORMAT GENERATOR"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    actions = {
                        // Theme mode toggle action button
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            val isDark by viewModel.isDarkTheme.collectAsState()
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }

                        // Logout button
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Sign Out")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Dashboard") },
                        icon = { Icon(Icons.Default.SpaceDashboard, contentDescription = "Dashboard") },
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Scan QR") },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                        modifier = Modifier.testTag("nav_item_scanner")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Create QR") },
                        icon = { Icon(Icons.Default.AddBox, contentDescription = "Create") },
                        modifier = Modifier.testTag("nav_item_generator")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> DashboardView(
                        viewModel = viewModel,
                        onNavigateToPremium = { isShowingPremiumScreen = true }
                    )
                    1 -> ScannerView(
                        viewModel = viewModel
                    )
                    2 -> GeneratorView(
                        viewModel = viewModel,
                        onNavigateToPremium = { isShowingPremiumScreen = true }
                    )
                }
            }
        }
    }
}
