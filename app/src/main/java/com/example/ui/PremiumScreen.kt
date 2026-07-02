package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.billing.BillingManager
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    viewModel: QrViewModel,
    billingManager: BillingManager,
    onBack: () -> Unit
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val productDetailsMap by billingManager.productDetails.collectAsState()
    val billingReady by billingManager.billingReady.collectAsState()
    val scrollState = rememberScrollState()
    val activity = LocalContext.current as? android.app.Activity

    var selectedPlanIndex by remember { mutableStateOf(1) } // Default to yearly

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "PREMIUM SUITE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                GoldPremium,
                                ElectricBlue
                            )
                        )
                    )
                    .padding(28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium Star",
                        tint = ElectricBlue,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Unlock Unlimited Potential",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create dynamic tracked QR codes, design custom styles, and bypass limits.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ElectricBlue.copy(alpha = 0.9f)),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pricing Options. Prices come from Play Billing's ProductDetails
            // (localized, source of truth) once loaded; a static fallback
            // label is shown briefly while the connection is being made.
            fun formattedPrice(productId: String, fallback: String): String {
                val details = productDetailsMap[productId] ?: return fallback
                return details.subscriptionOfferDetails?.firstOrNull()
                    ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    ?: details.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: fallback
            }

            val plans = listOf(
                PlanOption(
                    "Monthly Pro",
                    formattedPrice(BillingManager.PRODUCT_MONTHLY, "$4.99"),
                    "Billed monthly. Cancel anytime.",
                    productId = BillingManager.PRODUCT_MONTHLY
                ),
                PlanOption(
                    "Yearly Elite",
                    formattedPrice(BillingManager.PRODUCT_YEARLY, "$29.99"),
                    "Billed annually. Save 50%!",
                    isBestValue = true,
                    productId = BillingManager.PRODUCT_YEARLY
                ),
                PlanOption(
                    "Lifetime King",
                    formattedPrice(BillingManager.PRODUCT_LIFETIME, "$49.99"),
                    "One-time payment. Forever.",
                    productId = BillingManager.PRODUCT_LIFETIME
                )
            )

            plans.forEachIndexed { index, plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            width = if (selectedPlanIndex == index) 2.dp else 0.dp,
                            color = if (selectedPlanIndex == index) GoldPremium else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedPlanIndex = index }
                        .testTag("premium_plan_$index"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPlanIndex == index) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (plan.isBestValue) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(GoldPremium)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "BEST VALUE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = plan.price,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedPlanIndex == index) GoldPremium else MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = plan.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium benefits checklist
            Text(
                text = "Premium Features Include:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(12.dp))

            val benefits = listOf(
                "Unlimited QR Code Generations (No 5/day limit)",
                "Dynamic QR Code Tracking & Analytics",
                "Advanced Custom Styling (Colors, Rounded Dots)",
                "Insert logos inside QR codes",
                "Ad-Free Premium Experience",
                "Priority cloud dynamic redirections"
            )

            benefits.forEach { benefit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check",
                        tint = GoldPremium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button — launches the real Google Play purchase sheet.
            // Entitlement is granted only after Play Billing confirms the
            // purchase (see BillingManager); this button never sets premium
            // directly.
            Button(
                onClick = {
                    if (isPremium) {
                        // Downgrading/cancelling a real subscription happens
                        // through Play's subscription management, not in-app.
                        activity?.let {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://play.google.com/store/account/subscriptions")
                            )
                            it.startActivity(intent)
                        }
                    } else {
                        activity?.let {
                            billingManager.launchPurchaseFlow(it, plans[selectedPlanIndex].productId)
                        }
                    }
                },
                enabled = isPremium || billingReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("subscribe_premium_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPremium,
                    contentColor = ElectricBlue
                )
            ) {
                Text(
                    text = if (isPremium) "Manage Subscription" else "Unlock Premium - ${plans[selectedPlanIndex].price}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { billingManager.refreshEntitlements() }) {
                Text(
                    "Restore Purchases",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(onClick = onBack) {
                Text(
                    "Back to Dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

data class PlanOption(
    val name: String,
    val price: String,
    val description: String,
    val isBestValue: Boolean = false,
    val productId: String = ""
)
