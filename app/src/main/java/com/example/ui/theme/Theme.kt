package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GoldPremium,       // #D0BCFF Lavender
    onPrimary = ElectricBlue,    // #381E72 Deep Purple
    secondary = EmeraldGreen,    // Emerald Green
    tertiary = ElectricBlueLight, // #4A4458 Muted Dark Purple
    background = Slate900,       // #0F0F0F Pitch Black
    surface = Slate800,          // #1C1B1F Slate Card
    onPrimaryContainer = GoldPremium,
    onSecondary = Color.White,
    onBackground = Slate100,     // #E1E1E1 Light Gray text
    onSurface = Slate100,
    surfaceVariant = Slate700,   // #2D2C31 Borders/Dividers
    onSurfaceVariant = Slate600  // #9A9A9A Muted labels
)

private val LightColorScheme = darkColorScheme(
    primary = GoldPremium,       // Maintain the gorgeous dark aesthetic in all modes
    onPrimary = ElectricBlue,
    secondary = EmeraldGreen,
    tertiary = ElectricBlueLight,
    background = Slate900,
    surface = Slate800,
    onPrimaryContainer = GoldPremium,
    onSecondary = Color.White,
    onBackground = Slate100,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate600
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to ensure our gorgeous Slate/Serif identity remains intact!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
