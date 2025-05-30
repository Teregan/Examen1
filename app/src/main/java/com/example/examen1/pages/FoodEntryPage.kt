package com.example.examen1.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.components.ActionButton

import com.example.examen1.models.Allergen
import com.example.examen1.models.ControlType
import com.example.examen1.models.FoodEntryState
import com.example.examen1.ui.theme.MainGreen
import com.example.examen1.utils.LocalAlertsController
import com.example.examen1.viewmodels.ControlTypeViewModel
import com.example.examen1.viewmodels.FoodEntryViewModel
import com.example.examen1.viewmodels.TagViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodEntryPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: FoodEntryViewModel,
    controlTypeViewModel: ControlTypeViewModel,
    entryId: String? = null,
    profileId: String,
    tagViewModel: TagViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val alertsController = LocalAlertsController.current

    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTime by remember { mutableStateOf(timeFormatter.format(Date())) }
    var notes by remember { mutableStateOf("") }
    var selectedAllergens by remember { mutableStateOf(viewModel.allergens.map { it.copy() }) }

    val activeControls = controlTypeViewModel.activeControls.observeAsState(initial = emptyList())
    val foodEntryState = viewModel.foodEntryState.observeAsState()
    val currentEntry = viewModel.currentEntry.observeAsState()

    var selectedTagIds by remember { mutableStateOf<List<String>>(emptyList()) }
    val availableTags = tagViewModel.tags.observeAsState(initial = emptyList())

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
            selectedTagIds = entry.tagIds ?: emptyList()
        }
    }

    // Efecto para actualizar los alérgenos cuando hay controles activos
    LaunchedEffect(activeControls.value) {
        val currentlyActiveControls = activeControls.value.filter { it.isCurrentlyActive() }
        Log.d("FoodEntry", "Controles activos: ${currentlyActiveControls.size}")
        // Primero, manejar los controles activos normales
        if (currentlyActiveControls.isNotEmpty()) {
            currentlyActiveControls.forEach { control ->
                Log.d("FoodEntry", "Control: ${control.controlType} para alérgeno ${control.allergenId}")
            }
            selectedAllergens = viewModel.allergens.map { allergen ->
                val eliminationControl = currentlyActiveControls.find {
                    it.allergenId == allergen.id && it.controlType == ControlType.ELIMINATION
                }
                val controlledControl = currentlyActiveControls.find {
                    it.allergenId == allergen.id && it.controlType == ControlType.CONTROLLED
                }

                Log.d("FoodEntry", "Alérgeno ${allergen.id}: elimination=${eliminationControl != null}, controlled=${controlledControl != null}")

                allergen.copy(
                    isSelected = when {
                        eliminationControl != null -> {
                            Log.d("FoodEntry", "Alérgeno ${allergen.id} bloqueado por eliminación")
                            false
                        }
                        controlledControl != null -> {
                            Log.d("FoodEntry", "Alérgeno ${allergen.id} seleccionado por control")
                            true
                        }
                        else -> allergen.isSelected
                    }
                )
            }
        }
    }

    // Efecto para manejar los estados y la navegación
    LaunchedEffect(foodEntryState.value) {
        when (val state = foodEntryState.value) {
            is FoodEntryState.Success.Save -> {
                alertsController.showSuccessAlert(
                    title = "¡Éxito!",
                    message = if (entryId != null) "Registro actualizado" else "Registro guardado",
                    onConfirm = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            is FoodEntryState.Success.Load -> {
                // No hacer nada, solo se ha cargado el registro
            }
            is FoodEntryState.Error -> {
                alertsController.showErrorAlert(
                    title = "Error",
                    message = state.message
                )
            }
            else -> Unit
        }
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (entryId != null) "Editar Registro" else "Nuevo Registro",
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.primary
            )
            // Controles Activos
            if (activeControls.value.isNotEmpty()) {
                Text(
                    text = "(Preseleccionado por control activo)",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeControls.value) { control ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.primaryContainer.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(1.dp, colorScheme.primary)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = control.controlType.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colorScheme.primary
                                )
                                Text(
                                    text = viewModel.allergens.find { it.id == control.allergenId }?.name ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ){
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
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
                            },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "Fecha: ${dateFormatter.format(selectedDate)}",
                            modifier = Modifier.padding(16.dp),
                            color = colorScheme.onSurface
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
                            },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "Hora: $selectedTime",
                            modifier = Modifier.padding(16.dp),
                            color = colorScheme.onSurface
                        )
                    }

                    // Notes
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
                    // Sección de etiquetas
                    Column {
                        Text(
                            text = "Etiquetas:",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurface
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(availableTags.value) { tag ->
                                FilterChip(
                                    selected = tag.id in selectedTagIds,
                                    onClick = {
                                        selectedTagIds = if (tag.id in selectedTagIds) {
                                            selectedTagIds - tag.id
                                        } else {
                                            selectedTagIds + tag.id
                                        }
                                    },
                                    label = { Text(tag.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(android.graphics.Color.parseColor(tag.colorHex)),
                                        selectedLabelColor = colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        TextButton(
                            onClick = { navController.navigate("tag_management") },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Edit, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gestionar Etiquetas")
                        }
                    }
                    // Allergens Grid
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alérgenos:",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedAllergens.count { it.isSelected }}/${selectedAllergens.size} seleccionados",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )

                        }
                        if (activeControls.value.isNotEmpty()) {
                            Text(
                                text = "(Bloqueado por control activo)",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.error
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorScheme.outlineVariant),
                            color = colorScheme.surface.copy(alpha = 0.7f)
                        ){
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize()
                                ) {
                                    items(selectedAllergens) { allergen ->
                                        val hasActiveControl = activeControls.value
                                            .filter { it.isCurrentlyActive() }
                                            .any { it.allergenId == allergen.id }


                                        AllergenItem(
                                            allergen = allergen,
                                            onSelectionChanged = { isSelected ->
                                                val activeControl = activeControls.value
                                                    .find {
                                                        it.isCurrentlyActive() && it.allergenId == allergen.id
                                                    }

                                                // Si hay un control activo de tipo CONTROLLED, siempre permitir selección
                                                if (activeControl == null || activeControl.controlType == ControlType.CONTROLLED) {
                                                    selectedAllergens = selectedAllergens.map {
                                                        if (it.id == allergen.id) {
                                                            it.copy(isSelected = isSelected)
                                                        } else {
                                                            it
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = activeControls.value
                                                .find {
                                                    it.isCurrentlyActive() &&
                                                            it.allergenId == allergen.id &&
                                                            it.controlType == ControlType.ELIMINATION
                                                } == null
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 4.dp)
                                        .width(4.dp)
                                        .height(200.dp)
                                        .background(
                                            colorScheme.primary.copy(alpha = 0.1f),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }

                        }
                        Text(
                            text = "Desliza verticalmente para ver más alérgenos",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Column {
                        Text(
                            text = "Etiquetas:",
                            style = MaterialTheme.typography.titleMedium
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(availableTags.value) { tag ->
                                FilterChip(
                                    selected = tag.id in selectedTagIds,
                                    onClick = {
                                        selectedTagIds = if (tag.id in selectedTagIds) {
                                            selectedTagIds - tag.id
                                        } else {
                                            selectedTagIds + tag.id
                                        }
                                    },
                                    label = { Text(tag.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(android.graphics.Color.parseColor(tag.colorHex))
                                    )
                                )
                            }
                        }

                        TextButton(
                            onClick = { navController.navigate("tag_management") }
                        ) {
                            Icon(Icons.Default.Edit, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gestionar Etiquetas")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Save Button
            ActionButton(
                text = if (entryId != null) "Actualizar Registro" else "Guardar Registro",
                isNavigationArrowVisible = false,
                onClicked = {
                    val selectedAllergenIds = selectedAllergens
                        .filter { it.isSelected }
                        .map { it.id }

                    if (selectedAllergenIds.isEmpty()) {
                        alertsController.showWarningAlert(
                            title = "Aviso",
                            message = "Debe seleccionar al menos un alérgeno",
                            confirmText = "Entendido"
                        )
                        return@ActionButton
                    }

                    if (entryId != null) {
                        viewModel.updateFoodEntry(
                            entryId,
                            selectedDate,
                            selectedTime,
                            selectedAllergenIds,
                            selectedTags = selectedTagIds,
                            notes,
                            profileId = profileId
                        )
                    } else {
                        viewModel.addFoodEntry(
                            selectedDate,
                            selectedTime,
                            selectedAllergenIds,
                            selectedTags = selectedTagIds,
                            notes,
                            profileId = profileId
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                shadowColor = colorScheme.primary,
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

@Composable
fun AllergenItem(
    allergen: Allergen,
    onSelectionChanged: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(
                enabled = enabled,
                onClick = { onSelectionChanged(!allergen.isSelected) }
            ),
        border = if (allergen.isSelected) {
            BorderStroke(2.dp, if (enabled) MainGreen else MainGreen.copy(alpha = 0.5f))
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = when {
                !enabled -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                allergen.isSelected -> colorScheme.primaryContainer.copy(alpha = 0.1f)
                else -> colorScheme.surface
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
                tint = when {
                    !enabled -> colorScheme.onSurface.copy(alpha = 0.5f)
                    allergen.isSelected -> colorScheme.primary
                    else -> colorScheme.onSurface
                }
            )
            Text(
                text = allergen.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
                color = if (!enabled) colorScheme.onSurface.copy(alpha = 0.5f)  else colorScheme.onSurface
            )
        }
    }
}