package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,       // "SCANNED" or "GENERATED"
    val qrType: String,     // "TEXT", "WIFI", "IMAGE", "URL"
    val title: String,      // Visual title: e.g. "WiFi: Home_Network" or "https://google.com"
    val content: String,    // Exact encoded value
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null // Optional extra info (JSON or raw details)
)
