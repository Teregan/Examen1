package com.example.examen1.pages

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.examen1.models.FoodEntry
import com.example.examen1.models.StoolEntry
import com.example.examen1.models.SymptomEntry
import com.example.examen1.ui.theme.*
import com.example.examen1.viewmodels.*
import java.text.SimpleDateFormat
import java.util.*

data class DayData(
    val day: Int,
    val hasFoodEntry: Boolean = false,
    val hasSymptomEntry: Boolean = false,
    val hasStoolEntry: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyCalendarPage(
    navController: NavController,
    foodEntryViewModel: FoodEntryViewModel,
    symptomEntryViewModel: SymptomEntryViewModel,
    stoolEntryViewModel: StoolEntryViewModel,
    profileId: String
) {
    val context = LocalContext.current
    val foodEntries = foodEntryViewModel.foodEntries.observeAsState(initial = emptyList())
    val symptomEntries = symptomEntryViewModel.symptomEntries.observeAsState(initial = emptyList())
    val stoolEntries = stoolEntryViewModel.stoolEntries.observeAsState(initial = emptyList())

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDay by remember { mutableStateOf<DayData?>(null) }
    var showDayDialog by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())

    // Calcular los datos del día
    val daysData = remember(foodEntries.value, symptomEntries.value, stoolEntries.value, currentMonth) {
        val startOfMonth = Calendar.getInstance().apply {
            time = currentMonth.time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.time

        val endOfMonth = Calendar.getInstance().apply {
            time = currentMonth.time
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.time

        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        (1..daysInMonth).map { day ->
            val currentDate = Calendar.getInstance().apply {
                time = currentMonth.time
                set(Calendar.DAY_OF_MONTH, day)
            }.time

            DayData(
                day = day,
                hasFoodEntry = foodEntries.value.any {
                    it.profileId == profileId &&
                            dayFormatter.format(it.date).toInt() == day &&
                            it.date in startOfMonth..endOfMonth
                },
                hasSymptomEntry = symptomEntries.value.any {
                    it.profileId == profileId &&
                            dayFormatter.format(it.date).toInt() == day &&
                            it.date in startOfMonth..endOfMonth
                },
                hasStoolEntry = stoolEntries.value.any {
                    it.profileId == profileId &&
                            dayFormatter.format(it.date).toInt() == day &&
                            it.date in startOfMonth..endOfMonth
                }
            )
        }
    }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    currentMonth = Calendar.getInstance().apply {
                        time = currentMonth.time
                        add(Calendar.MONTH, -1)
                    }
                }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Mes anterior",
                        tint = MainGreen
                    )
                }

                Text(
                    text = dateFormatter.format(currentMonth.time).capitalize(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MainGreen
                )

                IconButton(onClick = {
                    currentMonth = Calendar.getInstance().apply {
                        time = currentMonth.time
                        add(Calendar.MONTH, 1)
                    }
                }) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Mes siguiente",
                        tint = MainGreen
                    )
                }
            }
            // Leyenda
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = LightGreen, text = "Alimentos")
                LegendItem(color = Color.Red.copy(alpha = 0.7f), text = "Síntomas")
                LegendItem(color = Color.Blue.copy(alpha = 0.7f), text = "Deposiciones")
            }

            // Cabecera de días
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MainGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rejilla del calendario
            val firstDayOfMonth = Calendar.getInstance().apply {
                time = currentMonth.time
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val startingDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Celdas vacías antes del primer día
                items(startingDayOfWeek) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Días del mes
                items(daysData) { dayData ->
                    DayCell(
                        dayData = dayData,
                        onClick = {
                            selectedDay = dayData
                            showDayDialog = true
                        }
                    )
                }

        }

        // Diálogo de detalle del día
        if (showDayDialog && selectedDay != null) {
            DayDetailDialog(
                dayData = selectedDay!!,
                currentMonth = currentMonth,
                foodEntries = foodEntries.value,
                symptomEntries = symptomEntries.value,
                stoolEntries = stoolEntries.value,
                foodEntryViewModel = foodEntryViewModel,
                symptomEntryViewModel = symptomEntryViewModel,
                onDismiss = { showDayDialog = false }
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun DayCell(
    dayData: DayData,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (dayData.hasFoodEntry || dayData.hasSymptomEntry || dayData.hasStoolEntry)
                    MainGreen.copy(alpha = 0.3f)
                else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayData.day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (dayData.hasFoodEntry || dayData.hasSymptomEntry || dayData.hasStoolEntry) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (dayData.hasFoodEntry) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(LightGreen, CircleShape)
                        )
                    }
                    if (dayData.hasSymptomEntry) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                        )
                    }
                    if (dayData.hasStoolEntry) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.Blue.copy(alpha = 0.7f), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailDialog(
    dayData: DayData,
    currentMonth: Calendar,
    foodEntries: List<FoodEntry>,
    symptomEntries: List<SymptomEntry>,
    stoolEntries: List<StoolEntry>,
    foodEntryViewModel: FoodEntryViewModel,
    symptomEntryViewModel: SymptomEntryViewModel,
    onDismiss: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dayDate = Calendar.getInstance().apply {
        time = currentMonth.time
        set(Calendar.DAY_OF_MONTH, dayData.day)
    }.time

    val dayFoodEntries = foodEntries.filter {
        isSameDay(it.date, dayDate)
    }
    val daySymptomEntries = symptomEntries.filter {
        isSameDay(it.date, dayDate)
    }
    val dayStoolEntries = stoolEntries.filter {
        isSameDay(it.date, dayDate)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Registros del día ${dayData.day}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MainGreen
                )

                if (dayFoodEntries.isNotEmpty()) {
                    EntrySection(
                        title = "Alimentos",
                        color = LightGreen
                    ) {
                        dayFoodEntries.forEach { entry ->
                            EntryItem(
                                time = entry.time,
                                details = buildString {
                                    append("Alérgenos: ")
                                    append(entry.allergens.joinToString(", ") { allergenId ->
                                        foodEntryViewModel.allergens.find { it.id == allergenId }?.name ?: ""
                                    })
                                    if (entry.notes.isNotEmpty()) {
                                        append("\nNotas: ${entry.notes}")
                                    }
                                }
                            )
                        }
                    }
                }

                if (daySymptomEntries.isNotEmpty()) {
                    EntrySection(
                        title = "Síntomas",
                        color = Color.Red.copy(alpha = 0.7f)
                    ) {
                        daySymptomEntries.forEach { entry ->
                            EntryItem(
                                time = entry.time,
                                details = buildString {
                                    append("Síntomas: ")
                                    append(entry.symptoms.joinToString(", ") { symptomId ->
                                        symptomEntryViewModel.predefinedSymptoms.find { it.id == symptomId }?.name ?: ""
                                    })
                                    if (entry.customSymptoms.isNotEmpty()) {
                                        append("\nOtros síntomas: ${entry.customSymptoms.joinToString(", ")}")
                                    }
                                    if (entry.notes.isNotEmpty()) {
                                        append("\nNotas: ${entry.notes}")
                                    }
                                }
                            )
                        }
                    }
                }

                if (dayStoolEntries.isNotEmpty()) {
                    EntrySection(
                        title = "Deposiciones",
                        color = Color.Blue.copy(alpha = 0.7f)
                    ) {
                        dayStoolEntries.forEach { entry ->
                            EntryItem(
                                time = entry.time,
                                details = buildString {
                                    append("Tipo: ${entry.stoolType}")
                                    append("\nColor: ${entry.color}")
                                    if (entry.notes.isNotEmpty()) {
                                        append("\nNotas: ${entry.notes}")
                                    }
                                }
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun EntrySection(
    title: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun EntryItem(
    time: String,
    details: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryPinkDark
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}