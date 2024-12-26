package com.example.examen1.components

import android.app.TimePickerDialog
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.examen1.services.NotificationReceiver

@Composable
fun NotificationConfigDialog(
    allergenName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var selectedTime by remember { mutableStateOf("09:00") }
    var isEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Notificaciones") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Configurar notificaciones para el control de $allergenName")

                // Time Selector
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val currentTime = selectedTime.split(":")
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    selectedTime = String.format("%02d:%02d", hour, minute)
                                },
                                currentTime[0].toInt(),
                                currentTime[1].toInt(),
                                true
                            ).show()
                        }
                ) {
                    Text(
                        text = "Hora de notificación: $selectedTime",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Enable/Disable Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activar notificaciones")
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }


            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedTime, isEnabled) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}