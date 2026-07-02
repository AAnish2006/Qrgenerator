package com.example.ui

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.BuildConfig
import com.example.data.HistoryItem
import com.example.data.QrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class QrViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: QrRepository
) : ViewModel() {

    // Local state (theme, cached entitlement, guest profile) is stored in
    // EncryptedSharedPreferences (AES-256-GCM, key held in the Android
    // Keystore) rather than plaintext prefs, so it can't be read or edited by
    // other apps or via a simple file pull on a rooted device. This is
    // defense-in-depth only: premium entitlement itself is always verified
    // against Play Billing (see BillingManager.refreshEntitlements), never
    // trusted from this cache alone.
    private val sharedPrefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            "qr_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.w("QrViewModel", "EncryptedSharedPreferences unavailable, falling back to standard prefs", e)
        appContext.getSharedPreferences("qr_prefs", Context.MODE_PRIVATE)
    }

    // UI States
    val allHistory: StateFlow<List<HistoryItem>>

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val _freeGenerationsLeft = MutableStateFlow(5)
    val freeGenerationsLeft = _freeGenerationsLeft.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl = _userPhotoUrl.asStateFlow()

    init {
        // Reactive Flow of history from database
        allHistory = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load saved preferences
        _isDarkTheme.value = sharedPrefs.getBoolean("is_dark_theme", false)
        _isPremium.value = sharedPrefs.getBoolean("is_premium", false)
        _userEmail.value = sharedPrefs.getString("user_email", null)
        _userName.value = sharedPrefs.getString("user_name", null)
        _userPhotoUrl.value = sharedPrefs.getString("user_photo", null)

        checkDailyLimits()
    }

    /**
     * Checks daily limit reset. Free users get 5 generations per day.
     */
    fun checkDailyLimits() {
        if (_isPremium.value) {
            _freeGenerationsLeft.value = 99999 // Unlimited for premium
            return
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastReset = sharedPrefs.getString("last_reset_date", "")
        
        if (lastReset != todayStr) {
            // New day! Reset to 5
            sharedPrefs.edit()
                .putString("last_reset_date", todayStr)
                .putInt("free_generations_count", 0)
                .apply()
            _freeGenerationsLeft.value = 5
        } else {
            val countUsed = sharedPrefs.getInt("free_generations_count", 0)
            _freeGenerationsLeft.value = (5 - countUsed).coerceAtLeast(0)
        }
    }

    /**
     * Deducts a free generation. Returns true if successful, false if limit reached.
     */
    fun useGeneration(): Boolean {
        if (_isPremium.value) return true

        checkDailyLimits()
        val currentLeft = _freeGenerationsLeft.value
        if (currentLeft <= 0) {
            return false
        }

        val newCount = 5 - currentLeft + 1
        sharedPrefs.edit().putInt("free_generations_count", newCount).apply()
        _freeGenerationsLeft.value = 5 - newCount
        return true
    }

    /**
     * Simulates Google Login
     */
    fun loginWithGoogle(email: String, name: String, photoUrl: String?) {
        sharedPrefs.edit()
            .putString("user_email", email)
            .putString("user_name", name)
            .putString("user_photo", photoUrl)
            .apply()

        _userEmail.value = email
        _userName.value = name
        _userPhotoUrl.value = photoUrl
    }

    /**
     * Logs out
     */
    fun logout() {
        sharedPrefs.edit()
            .remove("user_email")
            .remove("user_name")
            .remove("user_photo")
            .apply()

        _userEmail.value = null
        _userName.value = null
        _userPhotoUrl.value = null
    }

    /**
     * Sets premium status from a VERIFIED source only: BillingManager's
     * `refreshEntitlements()`, which re-queries Google Play directly. Do not
     * call this from UI code in response to a button tap — that would let
     * anyone grant themselves premium without paying.
     */
    fun setPremiumFromEntitlement(premium: Boolean) {
        sharedPrefs.edit().putBoolean("is_premium", premium).apply()
        _isPremium.value = premium
        checkDailyLimits()
    }

    /**
     * Debug-only manual toggle for local testing without hitting Play
     * Billing. Compiled out of behavior in release builds via
     * BuildConfig.ENABLE_DEBUG_PREMIUM_TOGGLE (false in release), so it can't
     * be used to bypass payment in a shipped APK.
     */
    fun setPremiumDebugOnly(premium: Boolean) {
        if (!BuildConfig.ENABLE_DEBUG_PREMIUM_TOGGLE) {
            Log.w("QrViewModel", "Ignoring setPremiumDebugOnly: disabled in this build")
            return
        }
        setPremiumFromEntitlement(premium)
    }

    /**
     * Toggles App theme
     */
    fun toggleTheme() {
        val next = !_isDarkTheme.value
        sharedPrefs.edit().putBoolean("is_dark_theme", next).apply()
        _isDarkTheme.value = next
    }

    /**
     * DB insert operation
     */
    fun addHistoryItem(type: String, qrType: String, title: String, content: String, metadata: String? = null) {
        viewModelScope.launch {
            repository.insert(
                HistoryItem(
                    type = type,
                    qrType = qrType,
                    title = title,
                    content = content,
                    metadata = metadata
                )
            )
        }
    }

    /**
     * DB delete operation
     */
    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    /**
     * DB clear operation
     */
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
