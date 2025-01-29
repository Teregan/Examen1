package com.example.examen1.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.examen1.utils.image.ImageManager
import com.example.examen1.utils.permissions.PermissionManager
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.examen1.ui.theme.MainGreen

private var temporaryImageUri: Uri? = null

@Composable
fun ImagePicker(
    onImageSelected: (Uri) -> Unit,
    currentImagesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var showOptions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    val imageManager = remember { ImageManager(context) }

    val canAddMoreImages = currentImagesCount < 3

    // Lanzador para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            temporaryImageUri?.let {
                onImageSelected(it)
                temporaryImageUri = null
            }
        }
    }

    // Lanzador para la galería con manejo de errores
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("ImagePicker", "Imagen seleccionada: $uri")
            onImageSelected(it)
        } ?: run {
            Log.e("ImagePicker", "No se seleccionó ninguna imagen")
        }
    }

    // Permisos launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            showOptions = true
        } else {
            // Manejar caso de permisos denegados
            Log.e("ImagePicker", "Permisos denegados")
        }
    }

    Column(modifier = modifier) {

        Button(
            onClick = {
                if (permissionManager.checkPermissions()) {
                    showOptions = true
                } else {
                    // Solicitar permisos si no están concedidos
                    permissionsLauncher.launch(permissionManager.requiredPermissions())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = canAddMoreImages,
            colors = ButtonDefaults.buttonColors(
                containerColor = MainGreen,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.AddAPhoto, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (canAddMoreImages)
                "Añadir Foto (${currentImagesCount}/3)"
            else "Límite de fotos alcanzado"
            )
        }

        if (showOptions && canAddMoreImages) {
            AlertDialog(
                onDismissRequest = { showOptions = false },
                title = { Text("Seleccionar imagen") },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                showOptions = false
                                if (permissionManager.checkPermissions()) {
                                    temporaryImageUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        imageManager.createTempFile()
                                    )
                                    cameraLauncher.launch(temporaryImageUri)
                                } else {
                                    permissionsLauncher.launch(permissionManager.requiredPermissions())
                                }
                            }
                        ) {
                            Text("Tomar foto")
                        }

                        TextButton(
                            onClick = {
                                showOptions = false
                                if (permissionManager.checkPermissions()) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    permissionsLauncher.launch(permissionManager.requiredPermissions())
                                }
                            }
                        ) {
                            Text("Seleccionar de la galería")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showOptions = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}