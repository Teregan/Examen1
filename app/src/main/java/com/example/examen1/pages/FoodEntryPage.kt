package com.example.examen1.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.examen1.components.ActionButton
import com.example.examen1.models.Allergen
import com.example.examen1.models.FoodEntryState
import com.example.examen1.ui.theme.PrimaryPinkDark
import com.example.examen1.viewmodels.FoodEntryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodEntryPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: FoodEntryViewModel,
    entryId: String? = null
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTime by remember { mutableStateOf(timeFormatter.format(Date())) }
    var notes by remember { mutableStateOf("") }
    var selectedAllergens by remember { mutableStateOf(viewModel.allergens.map { it.copy() }) }

    val foodEntryState = viewModel.foodEntryState.observeAsState()
    val currentEntry = viewModel.currentEntry.observeAsState()

    // Efecto para limpiar datos cuando es un nuevo registro
    LaunchedEffect(Unit) {
        if (entryId == null) {
            viewModel.resetState()
            selectedDate = Date()
            selectedTime = timeFormatter.format(Date())
            notes = ""
            selectedAllergens = viewModel.allergens.map { it.copy(isSelected = false) }
        }
    }

    // Efecto para cargar el registro si estamos en modo edición
    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.loadFoodEntry(entryId)
        }
    }

    // Efecto para actualizar la UI cuando se carga un registro existente
    LaunchedEffect(currentEntry.value) {
        currentEntry.value?.let { entry ->
            selectedDate = entry.date
            selectedTime = entry.time
            notes = entry.notes
            selectedAllergens = viewModel.allergens.map { allergen ->
                allergen.copy(isSelected = entry.allergens.contains(allergen.id))
            }
        }
    }

    // Efecto para manejar los estados y la navegación
    LaunchedEffect(foodEntryState.value) {
        when (val state = foodEntryState.value) {
            is FoodEntryState.Success.Save -> {
                Toast.makeText(
                    context,
                    if (entryId != null) "Registro actualizado" else "Registro guardado",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
            is FoodEntryState.Success.Load -> {
                // No hacer nada, solo se ha cargado el registro
            }
            is FoodEntryState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SmallTopAppBar(
            title = { Text(text = if (entryId != null) "Editar Registro" else "Nuevo Registro") },
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
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (entryId != null) "Editar Registro" else "Nuevo Registro",
                style = MaterialTheme.typography.headlineMedium
            )

            // Date Selector
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
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
                    text = "Hora: $selectedTime",
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Notes Field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Text(
                text = "Selecciona los Alérgenos:",
                style = MaterialTheme.typography.titleMedium
            )

            // Allergens Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(selectedAllergens) { allergen ->
                    AllergenItem(
                        allergen = allergen,
                        onSelectionChanged = { isSelected ->
                            selectedAllergens = selectedAllergens.map {
                                if (it.id == allergen.id) it.copy(isSelected = isSelected)
                                else it
                            }
                        }
                    )
                }
            }

            // Save Button
            ActionButton(
                text = if (entryId != null) "Actualizar Registro" else "Guardar Registro",
                isNavigationArrowVisible = false,
                onClicked = {
                    val selectedAllergenIds = selectedAllergens
                        .filter { it.isSelected }
                        .map { it.id }

                    if (entryId != null) {
                        viewModel.updateFoodEntry(entryId, selectedDate, selectedTime, selectedAllergenIds, notes)
                    } else {
                        viewModel.addFoodEntry(selectedDate, selectedTime, selectedAllergenIds, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPinkDark,
                    contentColor = Color.White
                ),
                shadowColor = PrimaryPinkDark,
                enabled = foodEntryState.value != FoodEntryState.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            if (foodEntryState.value == FoodEntryState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

}

@Composable
fun AllergenItem(
    allergen: Allergen,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onSelectionChanged(!allergen.isSelected) },
        border = if (allergen.isSelected) {
            BorderStroke(2.dp, PrimaryPinkDark)
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = if (allergen.isSelected) {
                PrimaryPinkDark.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(allergen.iconResId),
                contentDescription = allergen.name,
                modifier = Modifier.size(32.dp),
                tint = if (allergen.isSelected) PrimaryPinkDark else LocalContentColor.current
            )
            Text(
                text = allergen.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}