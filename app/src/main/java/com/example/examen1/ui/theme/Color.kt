package com.example.examen1.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Colores principales refinados
val MainGreen = Color(0xFF4CAF50)
val MainGreenDark = Color(0xFF2E7D32)
val MainGreenLight = Color(0xFFE8F5E9)

// Tema Claro
val LightColors = lightColorScheme(
    primary = MainGreen,
    onPrimary = Color.White,
    primaryContainer = MainGreenLight,
    secondary = MainGreenDark,
    background = Color(0xFFF5F7F5),
    surface = Color.White,
    onBackground = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242)
)

// Tema Oscuro
val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF121212),
    surface = Color(0xFF1A1C19),
    onBackground = Color(0xFFE1E3E1),
    surfaceVariant = Color(0xFF2A2B2A),
    onSurfaceVariant = Color(0xFFDDDDDD)
)