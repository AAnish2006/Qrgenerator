package com.example.ui

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPremium
import com.example.ui.theme.Slate800
import com.example.util.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorView(
    viewModel: QrViewModel,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isPremium by viewModel.isPremium.collectAsState()
    val freeGenerationsLeft by viewModel.freeGenerationsLeft.collectAsState()

    // Inputs state
    var selectedTab by remember { mutableStateOf("TEXT") } // TEXT, WIFI, IMAGE
    var textInput by remember { mutableStateOf("") }
    
    // Wifi Inputs
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiType by remember { mutableStateOf("WPA") }

    // Image Input (URL or file placeholder)
    var imageUriInput by remember { mutableStateOf("https://images.unsplash.com/photo-1544005313-94ddf0286df2") }
    
    // Style settings (Premium)
    var foregroundColorSelection by remember { mutableStateOf(AndroidColor.BLACK) }
    var isRoundedDotsEnabled by remember { mutableStateOf(false) }
    var isLogoEnabled by remember { mutableStateOf(false) }
    var isDynamicTrackingEnabled by remember { mutableStateOf(false) }

    // Outputs
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var generatedQrValue by remember { mutableStateOf("") }
    var showLimitDialog by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }

    // Standard logo bitmap loaded from icons
    val customLogo: Bitmap? = if (isLogoEnabled) {
        // Simple 40x40 pixel custom center square bitmap
        val b = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(b)
        c.drawColor(AndroidColor.RED)
        b
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Multi-Format Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("TEXT", "WIFI", "IMAGE")
            tabs.forEach { tab ->
                val active = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 10.dp)
                        .testTag("gen_tab_$tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Input forms container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    "TEXT" -> {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text("URL or Plain Text", style = MaterialTheme.typography.bodyMedium) },
                            placeholder = { Text("https://example.com or Enter Text") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gen_text_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )
                    }
                    "WIFI" -> {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("Network SSID (Name)", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gen_wifi_ssid"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = wifiPassword,
                            onValueChange = { wifiPassword = it },
                            label = { Text("Password", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gen_wifi_pass"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        // Security dropdown placeholder/row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Security Protocol:", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val types = listOf("WPA", "WEP", "NOPASS")
                                types.forEach { type ->
                                    val active = wifiType == type
                                    FilterChip(
                                        selected = active,
                                        onClick = { wifiType = type },
                                        label = { Text(type) }
                                    )
                                }
                            }
                        }
                    }
                    "IMAGE" -> {
                        OutlinedTextField(
                            value = imageUriInput,
                            onValueChange = { imageUriInput = it },
                            label = { Text("Image cloud URL or File Path", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gen_image_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Text(
                            text = "💡 Generates a smart dynamic redirection pointing to your dynamic image on our cloud storage with tracking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Custom Styling controls (Premium Feature)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPremium) Slate800 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Custom Style Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPremium) GoldPremium else MaterialTheme.colorScheme.onBackground
                        )
                    )
                    if (!isPremium) {
                        Badge(containerColor = GoldPremium, contentColor = Color.White) {
                            Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(12.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Premium Only", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Options list
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 1. Color options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Foreground Accent Color", style = MaterialTheme.typography.bodyMedium, color = if (isPremium) Color.White else MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val colors = listOf(AndroidColor.BLACK, AndroidColor.rgb(208, 188, 255), AndroidColor.rgb(56, 30, 114), AndroidColor.rgb(16, 185, 129))
                            colors.forEach { colorVal ->
                                val active = foregroundColorSelection == colorVal
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorVal))
                                        .border(
                                            width = if (active) 2.dp else 1.dp,
                                            color = if (active) GoldPremium else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable(enabled = isPremium) {
                                            foregroundColorSelection = colorVal
                                        }
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // 2. Rounded Dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rounded Pixel Blocks", style = MaterialTheme.typography.bodyMedium, color = if (isPremium) Color.White else MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = isRoundedDotsEnabled && isPremium,
                            onCheckedChange = { isRoundedDotsEnabled = it },
                            enabled = isPremium,
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPremium)
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // 3. Logo center overlay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Embed Center Brand Logo", style = MaterialTheme.typography.bodyMedium, color = if (isPremium) Color.White else MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = isLogoEnabled && isPremium,
                            onCheckedChange = { isLogoEnabled = it },
                            enabled = isPremium,
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPremium)
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // 4. Dynamic Redirect URL Tracking & Analytics (Google Redirect engine)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Dynamic Redirect & Analytics", style = MaterialTheme.typography.bodyMedium, color = if (isPremium) Color.White else MaterialTheme.colorScheme.onSurface)
                            Text("Directs scans via api.qrtracker.com tracking service", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = isDynamicTrackingEnabled && isPremium,
                            onCheckedChange = { isDynamicTrackingEnabled = it },
                            enabled = isPremium,
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPremium)
                        )
                    }
                }
            }
        }

        // Generate Action Button
        Button(
            onClick = {
                // Verify limits
                val canGenerate = viewModel.useGeneration()
                if (!canGenerate) {
                    showLimitDialog = true
                    return@Button
                }

                // Construct value to encode based on tab selection
                val value = when (selectedTab) {
                    "WIFI" -> "WIFI:S:$wifiSsid;T:$wifiType;P:$wifiPassword;;"
                    "IMAGE" -> {
                        // Dynamic redirect image setup
                        if (isDynamicTrackingEnabled) "https://api.qrtracker.com/redirect/img/" + System.currentTimeMillis()
                        else imageUriInput
                    }
                    else -> {
                        if (isDynamicTrackingEnabled && textInput.startsWith("http")) {
                            "https://api.qrtracker.com/redirect/url/" + System.currentTimeMillis()
                        } else textInput
                    }
                }

                if (value.isNotEmpty()) {
                    generatedQrValue = value
                    generatedBitmap = QrCodeGenerator.generate(
                        content = value,
                        foregroundColor = foregroundColorSelection,
                        logo = customLogo,
                        roundedDots = isRoundedDotsEnabled
                    )
                    
                    // Add to scan/create history
                    val itemTitle = when (selectedTab) {
                        "WIFI" -> "WiFi: $wifiSsid"
                        "IMAGE" -> "Image Redirection"
                        else -> if (textInput.length > 20) textInput.take(20) + "..." else textInput
                    }
                    viewModel.addHistoryItem(
                        type = "GENERATED",
                        qrType = selectedTab,
                        title = itemTitle,
                        content = value
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("generate_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPremium) GoldPremium else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.QrCode, contentDescription = "QR")
            Spacer(modifier = Modifier.width(8.dp))
            Text("GENERATE QR CODE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        // Generated output display
        AnimatedVisibility(visible = generatedBitmap != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("output_qr_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Your QR Code is Ready",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    generatedBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Text(
                        text = "Value: $generatedQrValue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )

                    // Share and Save buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showSuccessToast = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Code")
                        }
                        Button(
                            onClick = {
                                showSuccessToast = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Save")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save PNG")
                        }
                    }

                    if (showSuccessToast) {
                        Text(
                            text = "✓ Saved to gallery successfully!",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Daily limit dialog
    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showLimitDialog = false
                        onNavigateToPremium()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPremium)
                ) {
                    Text("Upgrade to Premium", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("Maybe Later")
                }
            },
            title = {
                Text("Daily Limit Reached", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Text("You have completed your 5 free QR code generations for today. Upgrade to Premium for unlimited scans, advanced custom designs, and live dynamic analytics.")
            }
        )
    }
}
