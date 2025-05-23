package com.example.examen1.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.examen1.components.ActionButton
import com.example.examen1.components.ImagePicker
import com.example.examen1.models.SymptomEntryState
import com.example.examen1.ui.theme.MainGreen
import com.example.examen1.viewmodels.SymptomEntryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.examen1.components.ThumbnailImage
import com.example.examen1.services.PDFGenerator
import com.example.examen1.services.sharePDF
import com.example.examen1.utils.LocalAlertsController
import com.example.examen1.viewmodels.FoodEntryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomEntryPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: SymptomEntryViewModel,
    foodEntryViewModel: FoodEntryViewModel,
    entryId: String? = null,
    profileId: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val alertsController = LocalAlertsController.current

    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTime by remember { mutableStateOf(timeFormatter.format(Date())) }
    var notes by remember { mutableStateOf("") }
    var selectedSymptoms by remember { mutableStateOf(viewModel.predefinedSymptoms.map { it.copy() }) }
    var customSymptoms by remember { mutableStateOf(listOf<String>()) }
    var showAddCustomSymptom by remember { mutableStateOf(false) }
    var newCustomSymptom by remember { mutableStateOf("") }

    val symptomEntryState = viewModel.symptomEntryState.observeAsState()
    val currentEntry = viewModel.currentEntry.observeAsState()
    val selectedImages = viewModel.selectedImages.collectAsState()

    // Efecto para limpiar datos cuando es un nuevo registro
    LaunchedEffect(Unit) {
        if (entryId == null) {
            viewModel.resetState()
            selectedDate = Date()
            selectedTime = timeFormatter.format(Date())
            notes = ""
            selectedSymptoms = viewModel.predefinedSymptoms.map { it.copy() }
            customSymptoms = emptyList()
        }
    }

    // Efecto para cargar datos cuando es una edición
    LaunchedEffect(currentEntry.value) {
        currentEntry.value?.let { entry ->
            selectedDate = entry.date
            selectedTime = entry.time
            notes = entry.notes
            selectedSymptoms = viewModel.predefinedSymptoms.map { symptom ->
                symptom.copy(isSelected = entry.symptoms.contains(symptom.id))
            }
            customSymptoms = entry.customSymptoms
        }
    }

    // Efecto para manejar los estados y la navegación
    LaunchedEffect(symptomEntryState.value) {
        when (val state = symptomEntryState.value) {
            is SymptomEntryState.Success.Save -> {
                alertsController.showSuccessAlert(
                    title = "¡Éxito!",
                    message = if (entryId != null) "Registro actualizado" else "Registro guardado",
                    onConfirm = {
                        navController.navigateUp()
                    }
                )
            }
            is SymptomEntryState.Error -> {
                alertsController.showErrorAlert(
                    title = "Error",
                    message = state.message
                )
            }
            else -> Unit
        }
    }

    LaunchedEffect(entryId) {
        entryId?.let {
            viewModel.loadSymptomEntry(it)  // o loadStoolEntry(it)
        }
    }


        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (entryId != null) "Editar Síntomas" else "Registrar Síntomas",
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.primary
            )

            Text(
                text = "Síntomas observados",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface

            )

            // Lista de síntomas predefinidos
            selectedSymptoms.forEach { symptom ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        selectedSymptoms = selectedSymptoms.map {
                            if (it.id == symptom.id) it.copy(isSelected = !it.isSelected)
                            else it
                        }
                    }
                ) {
                    ListItem(
                        leadingContent  = {
                            Checkbox(
                                checked = symptom.isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorScheme.primary,
                                    uncheckedColor = colorScheme.outline
                                )
                            )
                        },
                        headlineContent = {
                            Text(
                                text = symptom.name,
                                color = colorScheme.onSurface
                            )
                        }

                    )
                }
            }

            // Síntomas personalizados
            customSymptoms.forEach { symptom ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = symptom,
                                color = colorScheme.onSurface
                            )
                        },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    customSymptoms = customSymptoms.filter { it != symptom }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Eliminar síntoma",
                                    tint = colorScheme.error
                                )
                            }
                        }
                    )
                }
            }

            // Botón para agregar síntoma personalizado
            OutlinedButton(
                onClick = { showAddCustomSymptom = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorScheme.primary
                ),
                border = BorderStroke(1.dp, colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar otro síntoma")
            }

            Text(
                text = "Fecha y Hora",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )

            // Selector de fecha
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = colorScheme.surface
                ),
                border = BorderStroke(1.dp, colorScheme.outline),
                onClick = {
                    val calendar = Calendar.getInstance()
                    calendar.time = selectedDate
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(year, month, day)
                            selectedDate = calendar.time
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Text(
                    text = "Fecha: ${dateFormatter.format(selectedDate)}",
                    modifier = Modifier.padding(16.dp),
                    color = colorScheme.onSurface
                )
            }

            // Selector de hora
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = colorScheme.surface
                ),
                border = BorderStroke(1.dp, colorScheme.outline),
                onClick = {
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
                    text = "Hora: $selectedTime",
                    modifier = Modifier.padding(16.dp),
                    color = colorScheme.onSurface
                )
            }

            // Campo de notas
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedLabelColor = colorScheme.primary,
                    unfocusedLabelColor = colorScheme.onSurfaceVariant
                )
            )

            // Sección de imágenes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Imágenes",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface
                    )

                    ImagePicker(
                        onImageSelected = { uri -> viewModel.addImage(uri, entryId ?: "") },
                        currentImagesCount = selectedImages.value.size
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mostrar imágenes seleccionadas
                    if (selectedImages.value.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedImages.value) { imagePath ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(4.dp)
                                ) {
                                    ThumbnailImage(
                                        imagePath = imagePath,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Botón de eliminar
                                    IconButton(
                                        onClick = { viewModel.removeImage(imagePath) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar imagen",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Contador de imágenes
                        Text(
                            text = "${selectedImages.value.size}/3 imágenes",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Botón de guardar
            ActionButton(
                text = if (entryId != null) "Actualizar" else "Guardar",
                isNavigationArrowVisible = false,
                onClicked = {
                    val selectedSymptomIds = selectedSymptoms
                        .filter { it.isSelected }
                        .map { it.id }

                    if (entryId != null) {
                        viewModel.updateSymptomEntry(
                            entryId = entryId,
                            date = selectedDate,
                            time = selectedTime,
                            selectedSymptoms = selectedSymptomIds,
                            customSymptoms = customSymptoms,
                            notes = notes,
                            profileId = profileId
                        )
                    } else {
                        viewModel.addSymptomEntry(
                            date = selectedDate,
                            time = selectedTime,
                            selectedSymptoms = selectedSymptomIds,
                            customSymptoms = customSymptoms,
                            notes = notes,
                            profileId = profileId
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                shadowColor = MainGreen,
                enabled = symptomEntryState.value != SymptomEntryState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
            if (entryId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val pdfGenerator = PDFGenerator(
                            context = context,
                            foodEntryViewModel = foodEntryViewModel,
                            symptomEntryViewModel = viewModel
                        )
                        viewModel.viewModelScope.launch {
                            try {
                                val file = pdfGenerator.generateSymptomDetailReport(currentEntry.value!!)
                                sharePDF(context, file)
                                alertsController.showSuccessAlert(
                                    title = "Éxito",
                                    message = "Informe generado correctamente"
                                )
                            } catch (e: Exception) {
                                alertsController.showErrorAlert(
                                    title = "Error",
                                    message = "No se pudo generar el informe: ${e.message}"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, colorScheme.primary)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Exportar",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Exportar a PDF")
                    }
                }
            }
        }


    // Diálogo para agregar síntoma personalizado
    if (showAddCustomSymptom) {
        AlertDialog(
            onDismissRequest = { showAddCustomSymptom = false },
            containerColor = colorScheme.surface,
            title = { Text("Agregar síntoma") },
            text = {
                OutlinedTextField(
                    value = newCustomSymptom,
                    onValueChange = { newCustomSymptom = it },
                    label = { Text("Descripción del síntoma") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurfaceVariant
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCustomSymptom.isNotBlank()) {
                            customSymptoms = customSymptoms + newCustomSymptom
                            newCustomSymptom = ""
                        }
                        showAddCustomSymptom = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.primary
                    )
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddCustomSymptom = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}