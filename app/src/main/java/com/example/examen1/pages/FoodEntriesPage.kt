package com.example.examen1.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.examen1.R
import com.example.examen1.models.Allergen
import com.example.examen1.models.FoodEntry
import com.example.examen1.ui.theme.PrimaryPinkDark
import com.example.examen1.viewmodels.FoodEntryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodEntriesPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: FoodEntryViewModel
) {
    val entries = viewModel.foodEntries.observeAsState(initial = emptyList())
    val context = LocalContext.current
    var selectedEntry by remember { mutableStateOf<FoodEntry?>(null) }

    Column(
        modifier = modifier.fillMaxSize()
    ){
        SmallTopAppBar(
            title = { Text("Registros de Alimentación") },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Registros de Alimentación",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries.value) { entry ->
                    FoodEntryCard(
                        entry = entry,
                        allergens = viewModel.allergens,
                        onEdit = {
                            navController.navigate("food_entry_edit/${entry.id}")
                        },
                        onDelete = {
                            selectedEntry = entry
                        }
                    )
                }
            }
        }
    }


    selectedEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedEntry = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de eliminar este registro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFoodEntry(entry.id)
                        selectedEntry = null
                        Toast.makeText(context, "Registro eliminado", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEntry = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun FoodEntryCard(
    entry: FoodEntry,
    allergens: List<Allergen>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dateFormatter.format(entry.date)} - ${entry.time}",
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = "Editar registro"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Eliminar registro"
                        )
                    }
                }
            }

            // Display allergens
            if (entry.allergens.isNotEmpty()) {
                Text(
                    text = "Alérgenos:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = entry.allergens.joinToString(", ") { allergerId ->
                        allergens.find { it.id == allergerId }?.name ?: allergerId
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Display notes
            if (entry.notes.isNotEmpty()) {
                Text(
                    text = "Notas:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}