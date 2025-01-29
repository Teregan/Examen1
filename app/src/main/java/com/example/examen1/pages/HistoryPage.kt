package com.example.examen1.pages

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.examen1.viewmodels.*
import com.example.examen1.models.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.examen1.ui.theme.MainGreen

enum class RecordType {
    ALL, FOOD, SYMPTOM, STOOL, CONTROL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    historyViewModel: HistoryViewModel,
    activeProfileViewModel: ActiveProfileViewModel,
    tagViewModel: TagViewModel // Agregar TagViewModel
) {
    val historyState = historyViewModel.historyState.observeAsState()
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date()) }
    var recordType by remember { mutableStateOf(RecordType.ALL) }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) } // Nuevo
    var expandedTypeMenu by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val activeProfileState = activeProfileViewModel.activeProfileState.observeAsState()
    val availableTags = tagViewModel.tags.observeAsState(initial = emptyList()) // Nuevo



        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Historial",
                style = MaterialTheme.typography.headlineMedium,
                color = MainGreen
            )
            // Filtros
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Selector de fechas
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                navController.context,
                                { _, year, month, day ->
                                    calendar.set(year, month, day)
                                    startDate = calendar.time
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Text(
                            text = "Fecha inicial: ${dateFormatter.format(startDate)}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                navController.context,
                                { _, year, month, day ->
                                    calendar.set(year, month, day)
                                    endDate = calendar.time
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Text(
                            text = "Fecha final: ${dateFormatter.format(endDate)}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Selector de tipo de registro
                    ExposedDropdownMenuBox(
                        expanded = expandedTypeMenu,
                        onExpandedChange = { expandedTypeMenu = it }
                    ) {
                        OutlinedTextField(
                            value = when(recordType) {
                                RecordType.ALL -> "Todos los registros"
                                RecordType.FOOD -> "Registros de alimentación"
                                RecordType.SYMPTOM -> "Registros de síntomas"
                                RecordType.STOOL -> "Registros de deposiciones"
                                RecordType.CONTROL -> "Registros de control de alérgenos"
                            },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeMenu) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTypeMenu,
                            onDismissRequest = { expandedTypeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos los registros") },
                                onClick = {
                                    recordType = RecordType.ALL
                                    expandedTypeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Registros de alimentación") },
                                onClick = {
                                    recordType = RecordType.FOOD
                                    expandedTypeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Registros de síntomas") },
                                onClick = {
                                    recordType = RecordType.SYMPTOM
                                    expandedTypeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Registros de deposiciones") },
                                onClick = {
                                    recordType = RecordType.STOOL
                                    expandedTypeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Registros de control de alérgenos") },
                                onClick = {
                                    recordType = RecordType.CONTROL
                                    expandedTypeMenu = false
                                }
                            )
                        }
                    }

                    // Selector de etiquetas
                    if (recordType == RecordType.FOOD || recordType == RecordType.ALL) {
                        Text(
                            text = "Filtrar por etiquetas:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableTags.value) { tag ->
                                FilterChip(
                                    selected = tag.id in selectedTags,
                                    onClick = {
                                        selectedTags = if (tag.id in selectedTags) {
                                            selectedTags - tag.id
                                        } else {
                                            selectedTags + tag.id
                                        }
                                    },
                                    label = { Text(tag.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(android.graphics.Color.parseColor(tag.colorHex))
                                    )
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            historyViewModel.searchRecords(
                                startDate = startDate,
                                endDate = endDate,
                                recordType = recordType,
                                selectedTags = selectedTags
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Buscar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de resultados
            when (val state = historyState.value) {
                is HistoryState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is HistoryState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mostrar resultados de alimentación
                        if (recordType == RecordType.ALL || recordType == RecordType.FOOD) {
                            items(state.foodEntries.filter { entry ->
                                selectedTags.isEmpty() || entry.tagIds.any { it in selectedTags }
                            }) { entry ->
                                HistoryEntryCard(
                                    title = "Alimentos",
                                    date = entry.date,
                                    time = entry.time,
                                    icon = Icons.Default.Star,
                                    hasImages = false,
                                    onEdit = {
                                        val profileId = activeProfileState.value?.let {
                                            (it as? ActiveProfileState.Success)?.profile?.id
                                        } ?: return@HistoryEntryCard
                                        navController.navigate("food_entry_edit/${entry.id}/$profileId")
                                    }
                                )
                            }
                        }

                        // Mostrar resultados de síntomas
                        if (recordType == RecordType.ALL || recordType == RecordType.SYMPTOM) {
                            items(state.symptomEntries) { entry ->
                                val profileId = activeProfileState.value?.let {
                                    (it as? ActiveProfileState.Success)?.profile?.id
                                } ?: return@items
                                HistoryEntryCard(
                                    title = "Síntomas",
                                    date = entry.date,
                                    time = entry.time,
                                    icon = Icons.Default.Info,
                                    hasImages = entry.imagesPaths.isNotEmpty(),
                                    onEdit = {
                                        navController.navigate("symptom_entry_edit/${entry.id}/$profileId")
                                    }
                                )
                            }
                        }

                        // Mostrar resultados de deposiciones
                        if (recordType == RecordType.ALL || recordType == RecordType.STOOL) {
                            items(state.stoolEntries) { entry ->
                                val profileId = activeProfileState.value?.let {
                                    (it as? ActiveProfileState.Success)?.profile?.id
                                } ?: return@items
                                HistoryEntryCard(
                                    title = "Deposición",
                                    date = entry.date,
                                    time = entry.time,
                                    icon = Icons.Default.Check,
                                    hasImages = entry.imagesPaths.isNotEmpty(),
                                    onEdit = {
                                        navController.navigate("stool_entry_edit/${entry.id}/$profileId")
                                    }
                                )
                            }
                        }

                        if (recordType == RecordType.ALL || recordType == RecordType.CONTROL) {
                            items(state.controlEntries) { control ->
                                HistoryEntryCard(
                                    title = "Control de Alérgeno - ${control.controlType.displayName}",
                                    date = control.startDateAsDate,
                                    time = "Del ${dateFormatter.format(control.startDateAsDate)} al ${dateFormatter.format(control.endDateAsDate)} " +
                                            if (!control.isCurrentlyActive()) "(Finalizado)" else "(Activo)",
                                    icon = Icons.Default.AddCircle,
                                    onEdit = {} // Los controles no se pueden editar
                                )
                            }
                        }
                    }
                }
                is HistoryState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                HistoryState.Initial -> {
                    // No mostrar nada en el estado inicial
                }
                else -> Unit
            }
        }

}

@Composable
private fun HistoryEntryCard(
    title: String,
    date: Date,
    time: String,
    icon: ImageVector,
    hasImages: Boolean = false,
    tags: List<Tag> = emptyList(),
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ListItem(
                headlineContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(title)
                        // Indicador de imágenes
                        if (hasImages) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Contiene imágenes",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                supportingContent = { Text(time) },
                leadingContent = { Icon(icon, contentDescription = null) },
                trailingContent = { Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)) }
            )
            if (tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(tags) { tag ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(tag.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(android.graphics.Color.parseColor(tag.colorHex)).copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    }
}