package com.example.examen1.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.examen1.ui.theme.LightGreen
import com.example.examen1.ui.theme.MediumGreen
import androidx.compose.ui.Modifier
import com.example.examen1.ui.theme.DarkGreen

@Composable
fun DecorativeCircles(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            // Círculo grande superior izquierdo
            drawCircle(
                color = LightGreen.copy(alpha = 0.2f),
                radius = 180.dp.toPx(),
                center = Offset(-50.dp.toPx(), -50.dp.toPx())
            )

            // Círculo mediano superior derecho
            drawCircle(
                color = DarkGreen.copy(alpha = 0.15f),
                radius = 150.dp.toPx(),
                center = Offset(size.width + 50.dp.toPx(), -30.dp.toPx())
            )
        }
    }
}