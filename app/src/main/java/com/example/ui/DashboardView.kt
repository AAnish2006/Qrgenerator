package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(
    viewModel: QrViewModel,
    onNavigateToPremium: () -> Unit
) {
    val history by viewModel.allHistory.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val freeGenerationsLeft by viewModel.freeGenerationsLeft.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, SCANNED, GENERATED

    // For selected item dynamic analytics
    var selectedAnalyticsItem by remember { mutableStateOf<HistoryItem?>(null) }

    val filteredHistory = history.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                item.content.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "SCANNED" -> item.type == "SCANNED"
            "GENERATED" -> item.type == "GENERATED"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and User Profile
        item {
            Spacer(modifier = Modifier.height(16.dp))
            UserProfileHeader(viewModel, onNavigateToPremium)
        }

        // Limit & Premium Indicator Cards
        item {
            LimitStatusCard(
                isPremium = isPremium,
                freeGenerationsLeft = freeGenerationsLeft,
                onNavigateToPremium = onNavigateToPremium
            )
        }

        // Search & Filter Block
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search scan history...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Filter tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("ALL", "SCANNED", "GENERATED")
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            modifier = Modifier.testTag("filter_chip_$filter")
                        )
                    }
                }
            }
        }

        // Selected Dynamic Analytics Details Pane (Premium only)
        if (selectedAnalyticsItem != null) {
            item {
                DynamicAnalyticsPane(
                    item = selectedAnalyticsItem!!,
                    isPremium = isPremium,
                    onClose = { selectedAnalyticsItem = null },
                    onNavigateToPremium = onNavigateToPremium
                )
            }
        }

        // History list title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Logs (${filteredHistory.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearHistory() },
                        colors = ButtonDefaults.textButtonColors(contentColor = CrimsonRed)
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }

        if (filteredHistory.isEmpty()) {
            item {
                EmptyStateView()
            }
        } else {
            items(filteredHistory, key = { it.id }) { item ->
                HistoryItemRow(
                    item = item,
                    onDelete = { viewModel.deleteHistoryItem(item.id) },
                    onViewAnalytics = {
                        selectedAnalyticsItem = item
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun UserProfileHeader(
    viewModel: QrViewModel,
    onNavigateToPremium: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userName ?: "Explorer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (isPremium) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium Tier",
                        tint = GoldPremium,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        IconButton(
            onClick = onNavigateToPremium,
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (isPremium) GoldPremium.copy(alpha = 0.2f) 
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
        ) {
            Icon(
                imageVector = if (isPremium) Icons.Default.Star else Icons.Default.Stars,
                contentDescription = "Subscription",
                tint = if (isPremium) GoldPremium else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LimitStatusCard(
    isPremium: Boolean,
    freeGenerationsLeft: Int,
    onNavigateToPremium: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("limit_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) Slate800 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPremium) "PRO PLAN UNLOCKED" else "FREE DAILY QR PLAN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPremium) GoldPremium else MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPremium) "Unlimited Dynamic Code Generation" else "$freeGenerationsLeft / 5 generations left today",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                if (!isPremium) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { freeGenerationsLeft / 5f },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (freeGenerationsLeft == 0) CrimsonRed else ElectricBlue,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }

            if (!isPremium) {
                Button(
                    onClick = onNavigateToPremium,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPremium),
                    modifier = Modifier.testTag("upgrade_button")
                ) {
                    Text("Upgrade", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                }
            } else {
                Icon(
                    imageVector = Icons.Default.AutoGraph,
                    contentDescription = "Active Analytics",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryItemRow(
    item: HistoryItem,
    onDelete: () -> Unit,
    onViewAnalytics: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(item.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewAnalytics() }
            .testTag("history_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Left Icon indicating scanned vs generated
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (item.type == "SCANNED") ElectricBlue.copy(alpha = 0.1f)
                            else EmeraldGreen.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.type == "SCANNED") Icons.Default.QrCodeScanner else Icons.Default.Create,
                        contentDescription = item.type,
                        tint = if (item.type == "SCANNED") ElectricBlue else EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$dateStr • ${item.qrType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tracking Indicator (dynamic redirect tracking for generated codes)
                if (item.type == "GENERATED") {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = "Dynamic Analytics",
                        tint = EmeraldGreen,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = CrimsonRed.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicAnalyticsPane(
    item: HistoryItem,
    isPremium: Boolean,
    onClose: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("analytics_pane"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate800
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = "Insights",
                        tint = GoldPremium,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Dynamic Analytics Panel",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Check Premium permission
            if (!isPremium) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = GoldPremium, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tracking Analytics is locked.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Premium users can track scans, geolocations, and device models for dynamic codes.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToPremium,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPremium)
                    ) {
                        Text("Unlock Tracking Analytics", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                    }
                }
            } else {
                // Render gorgeous analytics!
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Real-time logs for: ${item.title}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnalyticsMetric("Total Redirects", "42 scans")
                        AnalyticsMetric("Daily Scans Avg", "4.8 / day")
                        AnalyticsMetric("Dynamic Target", if (item.content.length > 15) item.content.take(15) + "..." else item.content)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Click Distribution by Device:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldPremium)
                    )

                    // Progress bar stats for devices
                    StatProgressBar(label = "Android Devices", percentage = 0.65f, value = "27 scans", color = EmeraldGreen)
                    StatProgressBar(label = "iOS Devices", percentage = 0.25f, value = "11 scans", color = ElectricBlueLight)
                    StatProgressBar(label = "Desktop/Others", percentage = 0.10f, value = "4 scans", color = Color.LightGray)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Geographic Leads:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldPremium)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🇺🇸 United States (48%)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text("🇮🇳 India (32%)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text("🇬🇧 United Kingdom (12%)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsMetric(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
        }
    }
}

@Composable
fun StatProgressBar(label: String, percentage: Float, value: String, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCode,
            contentDescription = "Empty",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No History Logs",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your scanned or generated codes will appear here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
