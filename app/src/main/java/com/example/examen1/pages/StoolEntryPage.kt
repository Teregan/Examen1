package com.example.examen1.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.examen1.components.ActionButton
import com.example.examen1.components.ImagePicker
import com.example.examen1.models.FoodEntryState
import com.example.examen1.models.StoolColor
import com.example.examen1.models.StoolEntryState
import com.example.examen1.models.StoolType
import com.example.examen1.ui.theme.MainGreen
import com.example.examen1.viewmodels.StoolEntryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoolEntryPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: StoolEntryViewModel,
    entryId: String? = null,
    profileId: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTime by remember { mutableStateOf(timeFormatter.format(Date())) }
    var selectedStoolType by remember { mutableStateOf(StoolType.NORMAL) }
    var selectedColor by remember { mutableStateOf(StoolColor.BROWN) }
    var notes by remember { mutableStateOf("") }
    var expandedType by remember { mutableStateOf(false) }
    var expandedColor by remember { mutableStateOf(false) }

    val stoolEntryState = viewModel.stoolEntryState.observeAsState()
    val currentEntry = viewModel.currentEntry.observeAsState()
    val selectedImages = viewModel.selectedImages.collectAsState()
    // Efecto para limpiar datos cuando es un nuevo registro
    LaunchedEffect(Unit) {
        if (entryId == null) {
            viewModel.resetState()
        }
    }

    // Efecto para cargar datos cuando es una edición
    LaunchedEffect(currentEntry.value) {
        currentEntry.value?.let { entry ->
            selectedDate = entry.date
            selectedTime = entry.time
            selectedStoolType = entry.stoolType
            selectedColor = entry.color
            notes = entry.notes
        }
    }

    // Efecto para manejar los estados y la navegación
    LaunchedEffect(stoolEntryState.value) {
        when (val state = stoolEntryState.value) {
            is StoolEntryState.Success.Save -> {
                Toast.makeText(
                    context,
                    if (entryId != null) "Registro actualizado" else "Registro guardado",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigateUp()
            }
            is StoolEntryState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    LaunchedEffect(entryId) {
        entryId?.let {
            viewModel.loadStoolEntry(it)  // o loadStoolEntry(it)
        }
    }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (entryId != null) "Editar Deposición" else "Registrar Deposición",
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.primary
            )
            // Tipo de deposición
            Text(
                text = "Selección Tipo Deposición",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )

            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType },
            ) {
                OutlinedTextField(
                    value = when (selectedStoolType) {
                        StoolType.LIQUID -> "Líquida"
                        StoolType.HARD -> "Dura"
                        StoolType.PELLETS -> "Pelotitas"
                        StoolType.NORMAL -> "Normal"
                    },
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    StoolType.values().forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (type) {
                                        StoolType.LIQUID -> "Líquida"
                                        StoolType.HARD -> "Dura"
                                        StoolType.PELLETS -> "Pelotitas"
                                        StoolType.NORMAL -> "Normal"
                                    },
                                    color = colorScheme.onSurface
                                )
                            },
                            onClick = {
                                selectedStoolType = type
                                expandedType = false
                            }
                        )
                    }
                }
            }

            // Color
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )

            ExposedDropdownMenuBox(
                expanded = expandedColor,
                onExpandedChange = { expandedColor = !expandedColor }
            ) {
                OutlinedTextField(
                    value = when (selectedColor) {
                        StoolColor.BROWN -> "Café"
                        StoolColor.BLACK -> "Negro"
                        StoolColor.GREEN -> "Verde"
                        StoolColor.YELLOW -> "Amarillo"
                        StoolColor.RED -> "Rojo"
                        StoolColor.WHITE -> "Blanco"
                    },
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandedColor,
                    onDismissRequest = { expandedColor = false }
                ) {
                    StoolColor.values().forEach { color ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (color) {
                                        StoolColor.BROWN -> "Café"
                                        StoolColor.BLACK -> "Negro"
                                        StoolColor.GREEN -> "Verde"
                                        StoolColor.YELLOW -> "Amarillo"
                                        StoolColor.RED -> "Rojo"
                                        StoolColor.WHITE -> "Blanco"
                                    },
                                    color = colorScheme.onSurface
                                )
                            },
                            onClick = {
                                selectedColor = color
                                expandedColor = false
                            }
                        )
                    }
                }
            }

            // Fecha y Hora
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

            // Notas adicionales
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
                        onImageSelected = { uri ->
                            viewModel.addImage(uri, entryId ?: "")
                        },
                        currentImagesCount = selectedImages.value.size
                    )

                    // Mostrar imágenes seleccionadas
                    if (selectedImages.value.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedImages.value) { imagePath ->
                                Box {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(File(imagePath))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )

                                    // Botón de eliminar
                                    IconButton(
                                        onClick = { viewModel.removeImage(imagePath) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                color = colorScheme.surface.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar imagen",
                                            tint = colorScheme.error,
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

            // Botón guardar
            ActionButton(
                text = if (entryId != null) "Actualizar Registro" else "Guardar Registro",
                isNavigationArrowVisible = false,
                onClicked = {
                    if (entryId != null) {
                        viewModel.updateStoolEntry(
                            entryId = entryId,
                            date = selectedDate,
                            time = selectedTime,
                            stoolType = selectedStoolType,
                            color = selectedColor,
                            notes = notes,
                            profileId = profileId
                        )
                    } else {
                        viewModel.addStoolEntry(
                            date = selectedDate,
                            time = selectedTime,
                            stoolType = selectedStoolType,
                            color = selectedColor,
                            notes = notes,
                            profileId = profileId
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                shadowColor = colorScheme.primary,
                enabled = stoolEntryState.value != StoolEntryState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
        }
}