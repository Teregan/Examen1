package com.example.examen1.pages

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.models.ControlType
import com.example.examen1.models.ControlTypeState
import com.example.examen1.viewmodels.ControlTypeViewModel
import com.example.examen1.viewmodels.FoodEntryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlTypePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    controlTypeViewModel: ControlTypeViewModel,
    foodEntryViewModel: FoodEntryViewModel,
    profileId: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var selectedControlType by remember { mutableStateOf<ControlType?>(null) }
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date()) }
    var notes by remember { mutableStateOf("") }
    var allergens by remember { mutableStateOf(foodEntryViewModel.allergens.map { it.copy() }) }

    val controlTypeState = controlTypeViewModel.controlTypeState.observeAsState()

    LaunchedEffect(controlTypeState.value) {
        when (val state = controlTypeState.value) {
            is ControlTypeState.Success.Save -> {
                Toast.makeText(context, "Control guardado exitosamente", Toast.LENGTH_SHORT).show()
                navController.navigateUp()
            }
            is ControlTypeState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nuevo Control de Alérgeno",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.primary
        )

        // Control Type Selection
        Text(
            "Tipo de Control",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedControlType == ControlType.ELIMINATION,
                onClick = { selectedControlType = ControlType.ELIMINATION },
                label = { Text("Eliminación") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primary,
                    selectedLabelColor = colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = selectedControlType == ControlType.CONTROLLED,
                onClick = { selectedControlType = ControlType.CONTROLLED },
                label = { Text("Controlada") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primary,
                    selectedLabelColor = colorScheme.onPrimary
                )
            )
        }

        // Allergen Selection Grid
        Text(
            "Alérgeno a Evaluar",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .height(240.dp)
        ) {
            items(allergens) { allergen ->
                AllergenItem(
                    allergen = allergen,
                    onSelectionChanged = { isSelected ->
                        allergens = allergens.map {
                            if (it.id == allergen.id) {
                                it.copy(isSelected = isSelected)
                            } else {
                                it.copy(isSelected = false)
                            }
                        }
                    }
                )
            }
        }

        // Date Selection
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val calendar = Calendar.getInstance()
                        calendar.time = startDate
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                startDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = colorScheme.surface
                ),
                border = BorderStroke(1.dp, colorScheme.outline)
            ) {
                Text(
                    text = "Desde: ${dateFormatter.format(startDate)}",
                    modifier = Modifier.padding(16.dp),
                    color = colorScheme.onSurface
                )
            }

            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val calendar = Calendar.getInstance()
                        calendar.time = endDate
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                endDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = colorScheme.surface
                ),
                border = BorderStroke(1.dp, colorScheme.outline)
            ) {
                Text(
                    text = "Hasta: ${dateFormatter.format(endDate)}",
                    modifier = Modifier.padding(16.dp),
                    color = colorScheme.onSurface
                )
            }
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

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = {
                if (selectedControlType == null) {
                    Toast.makeText(context, "Seleccione un tipo de control", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val selectedAllergenId = allergens.find { it.isSelected }?.id
                if (selectedAllergenId == null) {
                    Toast.makeText(context, "Seleccione un alérgeno", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (endDate.before(startDate)) {
                    Toast.makeText(context, "La fecha final debe ser posterior a la inicial", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                controlTypeViewModel.addControl(
                    profileId = profileId,
                    controlType = selectedControlType!!,
                    allergenId = selectedAllergenId,
                    startDate = startDate,
                    endDate = endDate,
                    notes = notes
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            enabled = controlTypeState.value != ControlTypeState.Loading
        ) {
            Text(
                text = "Guardar Control",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    if (controlTypeState.value is ControlTypeState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = colorScheme.primary
            )
        }
    }
}