package com.example.examen1.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

import androidx.compose.ui.Modifier
import com.example.examen1.ui.theme.MainGreenDark
import com.example.examen1.ui.theme.MainGreenLight


@Composable
fun DecorativeCircles(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()
    
    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)) {
            // Círculo grande superior izquierdo
            drawCircle(
                color = if (isDarkTheme)
                    colorScheme.primaryContainer
                else
                    MainGreenLight,
                radius = 180.dp.toPx(),
                center = Offset(-50.dp.toPx(), -50.dp.toPx())
            )

            // Círculo mediano superior derecho
            drawCircle(
                color = if (isDarkTheme)
                    colorScheme.primary
                else
                    MainGreenDark,
                radius = 150.dp.toPx(),
                center = Offset(size.width + 50.dp.toPx(), -30.dp.toPx())
            )
        }
    }
}