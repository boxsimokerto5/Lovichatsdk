package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Always-radiant bright light color scheme
private val BrightColorScheme =
  lightColorScheme(
    primary = LovyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE8EF),
    onPrimaryContainer = LovyPrimaryVariant,
    secondary = LovySecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = LovySecondaryVariant,
    tertiary = LovyTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4EB),
    background = LovyLightBackground,
    surface = LovyLightSurface,
    surfaceVariant = LovyLightSurfaceVariant,
    onBackground = LovyTextPrimary,
    onSurface = LovyTextPrimary,
    onSurfaceVariant = LovyTextSecondary,
    outline = LovyLightBorder,
    outlineVariant = Color(0xFFEFF1F5)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Locked to true bright light mode
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = BrightColorScheme,
    typography = Typography,
    content = content
  )
}

