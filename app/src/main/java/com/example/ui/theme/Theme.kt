package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentBlue,                // Light blue accent
    onPrimary = OnAccentBlue,            // Dark blue text for FABs/primary indicators
    primaryContainer = PrimaryContainerDark, // Deep Blue for balance banner card
    onPrimaryContainer = OnPrimaryContainerDark,
    background = BackgroundDark,         // #1A1C1E
    onBackground = TextPrimaryDark,      // #E2E2E6
    surface = SurfaceDark,               // #2E3033
    onSurface = TextPrimaryDark,         // #E2E2E6
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark, // #909194
    secondary = AccentBlue,
    onSecondary = OnAccentBlue,
    tertiary = SuccessGreen,
    error = ExpenseRose
  )

private val LightColorScheme = DarkColorScheme // Force elegant dark in both light and dark modes to match request

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme by default
  // Dynamic color disabled by default to show our brand colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      else -> DarkColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
