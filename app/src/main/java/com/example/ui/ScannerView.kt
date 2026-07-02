package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.util.QrCodeParser

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerView(
    viewModel: QrViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Handle camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Scanner outputs state
    var scanResultText by remember { mutableStateOf<String?>(null) }
    var showManualInput by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf("") }
    
    // UI state for connect actions
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    LaunchedEffect(key1 = true) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper scanner viewport / Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (cameraPermissionState.status.isGranted) {
                // Real Live CameraX View finder
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().apply {
                                surfaceProvider = previewView.surfaceProvider
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Scanner target frame overlays
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .border(3.dp, EmeraldGreen, RoundedCornerShape(12.dp))
                )
            } else {
                // Denied or loading state - guide user
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = "No Camera",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enable camera in settings to scan QR codes physically.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Grant Permission", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                    }
                }
            }
        }

        // Quick Input Actions (Alternative inputs for emulator/virtual testing)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    showManualInput = true
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("paste_code_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Input, contentDescription = "Manual")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Paste / Code Input")
            }

            Button(
                onClick = {
                    // Simulate selecting a beautiful WiFi QR image from gallery
                    val simulatedWifi = "WIFI:S:Guest_Vip_Lounge;T:WPA;P:GuestLounge2026;;"
                    scanResultText = simulatedWifi
                    viewModel.addHistoryItem(
                        type = "SCANNED",
                        qrType = "WIFI",
                        title = "Scanned WiFi: Guest_Vip_Lounge",
                        content = simulatedWifi
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("upload_gallery_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan Image File")
            }
        }

        // Active Scan Result details Card
        scanResultText?.let { result ->
            val parsed = QrCodeParser.parse(result)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scan_result_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scanned Code Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { scanResultText = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }

                    // Content details
                    when (parsed) {
                        is QrCodeParser.ParsedResult.Url -> {
                            ResultItem(label = "Link URL", value = parsed.url, icon = Icons.Default.Language)
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(parsed.url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = "Open")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Website")
                            }
                        }
                        is QrCodeParser.ParsedResult.Wifi -> {
                            ResultItem(label = "WiFi Network SSID", value = parsed.ssid, icon = Icons.Default.Wifi)
                            ResultItem(label = "Password", value = parsed.pass, icon = Icons.Default.VpnKey)
                            ResultItem(label = "Security Protocol", value = parsed.type, icon = Icons.Default.Security)
                            
                            Button(
                                onClick = {
                                    val clip = android.content.ClipData.newPlainText("WiFi Password", parsed.pass)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied WiFi password to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Password & Connect")
                            }
                        }
                        is QrCodeParser.ParsedResult.Email -> {
                            ResultItem(label = "Send To Email", value = parsed.email, icon = Icons.Default.Email)
                            parsed.subject?.let { ResultItem(label = "Subject Line", value = it, icon = Icons.Default.Subject) }
                            
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:${parsed.email}")
                                        putExtra(Intent.EXTRA_SUBJECT, parsed.subject ?: "")
                                        putExtra(Intent.EXTRA_TEXT, parsed.body ?: "")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Compose Email")
                            }
                        }
                        is QrCodeParser.ParsedResult.Text -> {
                            ResultItem(label = "Plain Text Content", value = parsed.text, icon = Icons.Default.Notes)
                            Button(
                                onClick = {
                                    val clip = android.content.ClipData.newPlainText("Scanned Text", parsed.text)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied text content to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Text to Clipboard")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Paste code input dialog
    if (showManualInput) {
        AlertDialog(
            onDismissRequest = { showManualInput = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualInputText.isNotEmpty()) {
                            scanResultText = manualInputText
                            
                            val titleStr = if (manualInputText.startsWith("http")) manualInputText else "Pasted Text Log"
                            viewModel.addHistoryItem(
                                type = "SCANNED",
                                qrType = "TEXT",
                                title = titleStr,
                                content = manualInputText
                            )
                        }
                        showManualInput = false
                        manualInputText = ""
                    }
                ) {
                    Text("Decode & Parse")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualInput = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Paste Scanned QR Code", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Manually paste any QR encoded string or link value here to parse its triggers.")
                    OutlinedTextField(
                        value = manualInputText,
                        onValueChange = { manualInputText = it },
                        placeholder = { Text("Enter text, http link or WIFI config...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_decode_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )
                }
            }
        )
    }
}

@Composable
fun ResultItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
