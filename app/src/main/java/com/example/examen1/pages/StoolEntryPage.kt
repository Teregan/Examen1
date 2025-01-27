package com.example.examen1.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.components.ActionButton
import com.example.examen1.models.StoolColor
import com.example.examen1.models.StoolEntryState
import com.example.examen1.models.StoolType
import com.example.examen1.ui.theme.MainGreen
import com.example.examen1.viewmodels.StoolEntryViewModel
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


        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (entryId != null) "Editar Deposición" else "Registrar Deposición",
                style = MaterialTheme.typography.headlineMedium,
                color = MainGreen
            )
            // Tipo de deposición
            Text(
                text = "Selección Tipo Deposición",
                style = MaterialTheme.typography.titleMedium
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
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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
                                    }
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
                style = MaterialTheme.typography.titleMedium
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
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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
                                    }
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
                style = MaterialTheme.typography.titleMedium
            )

            // Selector de fecha
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Selector de hora
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Notas adicionales
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Botón guardar
            ActionButton(
                text = if (entryId != null) "Actualizar" else "Guardar",
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
                    containerColor = MainGreen,
                    contentColor = Color.White
                ),
                shadowColor = MainGreen,
                enabled = stoolEntryState.value != StoolEntryState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
        }

}