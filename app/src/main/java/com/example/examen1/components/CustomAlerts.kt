package com.example.examen1.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AlertType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

@Composable
fun SweetAlert(
    show: Boolean,
    type: AlertType,
    title: String,
    message: String,
    confirmText: String = "Aceptar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = onConfirm,
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    autoDismissTime: Long? = null // Tiempo en milisegundos para cerrar automáticamente
) {
    var visible by remember { mutableStateOf(show) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(show) {
        visible = show
        if (show && autoDismissTime != null) {
            delay(autoDismissTime)
            visible = false
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Dialog(
            onDismissRequest = {
                visible = false
                onDismiss()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icono animado
                    Box(contentAlignment = Alignment.Center) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        val icon = when (type) {
                            AlertType.SUCCESS -> Icons.Default.CheckCircle
                            AlertType.ERROR -> Icons.Default.Error
                            AlertType.WARNING -> Icons.Default.Warning
                            AlertType.INFO -> Icons.Default.Info
                        }

                        val iconColor = when (type) {
                            AlertType.SUCCESS -> Color(0xFF4CAF50) // Verde
                            AlertType.ERROR -> Color(0xFFE53935) // Rojo
                            AlertType.WARNING -> Color(0xFFFFA000) // Ámbar
                            AlertType.INFO -> Color(0xFF2196F3) // Azul
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = type.name,
                            tint = iconColor,
                            modifier = Modifier
                                .size(64.dp)
                                .scale(scale)
                        )
                    }

                    // Título
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Mensaje
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Botón
                    Button(
                        onClick = {
                            visible = false
                            onConfirm()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmButtonColor
                        )
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

// Toast mejorado que se muestra brevemente y desaparece
@Composable
fun SweetToast(
    message: String,
    show: Boolean,
    type: AlertType = AlertType.INFO,
    duration: Long = 2000,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(show) }

    LaunchedEffect(show) {
        if (show) {
            visible = true
            delay(duration)
            visible = false
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> fullHeight })
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = when (type) {
                        AlertType.SUCCESS -> Color(0xFF4CAF50).copy(alpha = 0.9f)
                        AlertType.ERROR -> Color(0xFFE53935).copy(alpha = 0.9f)
                        AlertType.WARNING -> Color(0xFFFFA000).copy(alpha = 0.9f)
                        AlertType.INFO -> Color(0xFF2196F3).copy(alpha = 0.9f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val icon = when (type) {
                        AlertType.SUCCESS -> Icons.Default.CheckCircle
                        AlertType.ERROR -> Icons.Default.Error
                        AlertType.WARNING -> Icons.Default.Warning
                        AlertType.INFO -> Icons.Default.Info
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Text(
                        text = message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Componente para mostrar mensajes de confirmación
@Composable
fun ConfirmationDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    cancelText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector = Icons.Default.Warning,
    iconTint: Color = MaterialTheme.colorScheme.error
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss
                ) {
                    Text(cancelText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}