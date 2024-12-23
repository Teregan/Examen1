package com.example.examen1.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.models.SymptomEntryState
import com.example.examen1.ui.theme.PrimaryPinkDark
import com.example.examen1.viewmodels.SymptomEntryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomEntryPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: SymptomEntryViewModel,
    entryId: String? = null,
    profileId: String
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTime by remember { mutableStateOf(timeFormatter.format(Date())) }
    var notes by remember { mutableStateOf("") }
    var selectedSymptoms by remember { mutableStateOf(viewModel.predefinedSymptoms.map { it.copy() }) }
    var customSymptoms by remember { mutableStateOf(listOf<String>()) }
    var showAddCustomSymptom by remember { mutableStateOf(false) }
    var newCustomSymptom by remember { mutableStateOf("") }

    val symptomEntryState = viewModel.symptomEntryState.observeAsState()
    val currentEntry = viewModel.currentEntry.observeAsState()

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
                Toast.makeText(
                    context,
                    if (entryId != null) "Registro actualizado" else "Registro guardado",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigateUp()
            }
            is SymptomEntryState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(if (entryId != null) "Editar Síntomas" else "Registrar Síntomas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = PrimaryPinkDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Síntomas observados",
                style = MaterialTheme.typography.titleMedium
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
                                onCheckedChange = null
                            )
                        },
                        headlineContent = { Text(symptom.name) }

                    )
                }
            }

            // Síntomas personalizados
            customSymptoms.forEach { symptom ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(symptom) },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    customSymptoms = customSymptoms.filter { it != symptom }
                                }
                            ) {
                                Icon(Icons.Default.Close, "Eliminar síntoma")
                            }
                        }
                    )
                }
            }

            // Botón para agregar síntoma personalizado
            OutlinedButton(
                onClick = { showAddCustomSymptom = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar otro síntoma")
            }

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

            // Campo de notas
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Botón de guardar
            Button(
                onClick = {
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
                modifier = Modifier.fillMaxWidth(),
                enabled = symptomEntryState.value != SymptomEntryState.Loading
            ) {
                Text(if (entryId != null) "Actualizar" else "Guardar")
            }
        }
    }

    // Diálogo para agregar síntoma personalizado
    if (showAddCustomSymptom) {
        AlertDialog(
            onDismissRequest = { showAddCustomSymptom = false },
            title = { Text("Agregar síntoma") },
            text = {
                OutlinedTextField(
                    value = newCustomSymptom,
                    onValueChange = { newCustomSymptom = it },
                    label = { Text("Descripción del síntoma") },
                    modifier = Modifier.fillMaxWidth()
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
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomSymptom = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}