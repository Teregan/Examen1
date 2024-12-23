package com.example.examen1.pages

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.examen1.ui.theme.PrimaryPinkDark

enum class RecordType {
    ALL, FOOD, SYMPTOM, STOOL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    historyViewModel: HistoryViewModel,
    activeProfileViewModel: ActiveProfileViewModel
) {
    val historyState = historyViewModel.historyState.observeAsState()
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date()) }
    var recordType by remember { mutableStateOf(RecordType.ALL) }
    var expandedTypeMenu by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val activeProfileState = activeProfileViewModel.activeProfileState.observeAsState()

    LaunchedEffect(Unit) {
        historyViewModel.searchRecords()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
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
                .padding(16.dp)
        ) {
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
                        }
                    }

                    Button(
                        onClick = {
                            historyViewModel.searchRecords(
                                startDate = startDate,
                                endDate = endDate,
                                recordType = recordType
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
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
                            items(state.foodEntries) { entry ->
                                HistoryEntryCard(
                                    title = "Alimentos",
                                    date = entry.date,
                                    time = entry.time,
                                    icon = Icons.Default.Star,
                                    onEdit = {
                                        navController.navigate("food_entry_edit/${entry.id}")
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
                                    onEdit = {
                                        navController.navigate("stool_entry_edit/${entry.id}/$profileId")
                                    }
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
                else -> Unit
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(
    title: String,
    date: Date,
    time: String,
    icon: ImageVector,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(time) },
            leadingContent = { Icon(icon, contentDescription = null) },
            trailingContent = { Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)) }
        )
    }
}