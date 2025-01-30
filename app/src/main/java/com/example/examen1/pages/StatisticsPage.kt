package com.example.examen1.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.components.StatisticalInsightsCard
import com.example.examen1.models.UserProfile
import com.example.examen1.viewmodels.ProfileViewModel
import com.example.examen1.viewmodels.StatisticalAnalysis
import com.example.examen1.viewmodels.StatisticsViewModel
import java.util.*

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.examen1.ui.theme.MainGreen
import kotlin.math.abs

@Composable
fun StatisticsPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: StatisticsViewModel,
    profileViewModel: ProfileViewModel // Añadir como parámetro
) {
    val colorScheme = MaterialTheme.colorScheme
    var dateRange by remember {
        mutableStateOf(
            DateRange(
                start = Date(System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000),
                end = Date()
            )
        )
    }

    var selectedProfile by remember { mutableStateOf<UserProfile?>(null) }
    var selectedStatType by remember { mutableStateOf(StatType.ALL) }
    var expandedDateFilter by remember { mutableStateOf(false) }
    var expandedProfileFilter by remember { mutableStateOf(false) }

    // Estados de los datos
    val foodEntriesStats by viewModel.foodEntriesOverTime.observeAsState(initial = emptyList())
    val symptomFrequencyStats by viewModel.symptomFrequency.observeAsState(initial = emptyList())
    val stoolTypeStats by viewModel.stoolTypeDistribution.observeAsState(initial = emptyList())

    // Análisis estadístico
    val statisticalAnalysis by viewModel.comprehensiveAnalysis.observeAsState(
        initial = StatisticalAnalysis("No hay datos disponibles", emptyList())
    )

    val profiles by profileViewModel.profiles.observeAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de perfil
                Box {
                    OutlinedButton(
                        onClick = { expandedProfileFilter = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.primary
                        )
                    ) {
                        Text(selectedProfile?.name ?: "Todos los perfiles")
                    }

                    DropdownMenu(
                        expanded = expandedProfileFilter,
                        onDismissRequest = { expandedProfileFilter = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los perfiles") },
                            onClick = {
                                selectedProfile = null
                                viewModel.generateStatistics(dateRange.start, dateRange.end, null)
                                expandedProfileFilter = false
                            }
                        )
                        profiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile.name) },
                                onClick = {
                                    selectedProfile = profile
                                    viewModel.generateStatistics(dateRange.start, dateRange.end, profile.id)
                                    expandedProfileFilter = false
                                }
                            )
                        }
                    }
                }

                // Selector de rango de fechas
                Box {
                    OutlinedButton(
                        onClick = { expandedDateFilter = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.primary
                        )
                    ) {
                        Text("Rango de fechas")
                    }

                    DropdownMenu(
                        expanded = expandedDateFilter,
                        onDismissRequest = { expandedDateFilter = false }
                    ) {
                        listOf(
                            "Última Semana" to 7,
                            "Último Mes" to 30,
                            "Últimos 3 Meses" to 90
                        ).forEach { (label, days) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    val endDate = Date()
                                    val startDate = Date(endDate.time - days * 24 * 60 * 60 * 1000L)
                                    dateRange = DateRange(startDate, endDate)
                                    viewModel.generateStatistics(startDate, endDate, selectedProfile?.id)
                                    expandedDateFilter = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Análisis Detallado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ){
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Análisis",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = statisticalAnalysis.summary,
                    color = colorScheme.onSurface
                )
                if (statisticalAnalysis.alerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    statisticalAnalysis.alerts.forEach { alert ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = alert,
                                color = colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Gráficos
        Text(
            text = "Visualización de Datos",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                if (foodEntriesStats.isNotEmpty()) {
                    StatisticChartCard(
                        title = "Alérgenos",
                        data = foodEntriesStats.associate {
                            it.name to it.percentage.toFloat()
                        }
                    )
                }
            }
            item {
                if (symptomFrequencyStats.isNotEmpty()) {
                    StatisticChartCard(
                        title = "Síntomas",
                        data = symptomFrequencyStats.associate {
                            it.symptom to it.percentageOfEntries.toFloat()
                        }
                    )
                }
            }
            item {
                if (stoolTypeStats.isNotEmpty()) {
                    StatisticChartCard(
                        title = "Deposiciones",
                        data = stoolTypeStats.associate {
                            it.type to it.percentage.toFloat()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticChartCard(
    title: String,
    data: Map<String, Float>
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(320.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = colorScheme.primary,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val total = data.values.sum()
                    var startAngle = 0f

                    data.forEach { (name, value) ->
                        val sweepAngle = (value / total) * 360f
                        drawArc(
                            color = generateColorForName(name),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            // Leyenda
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(data.toList().sortedByDescending { it.second }) { (name, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        generateColorForName(name),
                                        CircleShape
                                    )
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                        Text(
                            text = String.format("%.1f%%", value),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Filtros Dinámicos
@Composable
fun StatisticsFilterSection(
    profiles: List<UserProfile>,
    onDateRangeChanged: (DateRange) -> Unit,
    onProfileSelected: (UserProfile?) -> Unit,
    onStatTypeChanged: (StatType) -> Unit
) {
    var expandedDateFilter by remember { mutableStateOf(false) }
    var expandedProfileFilter by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Filtro de Rango de Fechas
        Box {
            OutlinedButton(onClick = { expandedDateFilter = true }) {
                Text("Rango de Fechas")
            }

            DropdownMenu(
                expanded = expandedDateFilter,
                onDismissRequest = { expandedDateFilter = false }
            ) {
                // Opciones predefinidas de rango
                listOf(
                    "Última Semana" to 7,
                    "Último Mes" to 30,
                    "Últimos 3 Meses" to 90
                ).forEach { (label, days) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            val endDate = Date()
                            val startDate = Date(endDate.time - days * 24 * 60 * 60 * 1000L)
                            onDateRangeChanged(DateRange(startDate, endDate))
                            expandedDateFilter = false
                        }
                    )
                }
            }
        }

        // Filtro de Perfil
        Box {
            OutlinedButton(onClick = { expandedProfileFilter = true }) {
                Text("Perfil")
            }

            DropdownMenu(
                expanded = expandedProfileFilter,
                onDismissRequest = { expandedProfileFilter = false }
            ) {
                // Cargar perfiles dinámicamente
                profiles.forEach { profile ->
                    DropdownMenuItem(
                        text = { Text(profile.name) },
                        onClick = {
                            onProfileSelected(profile)
                            expandedProfileFilter = false
                        }
                    )
                }
            }
        }
    }
}

// Tipos de Estadísticas
enum class StatType {
    ALL, FOOD, SYMPTOMS, STOOL
}

// Rango de Fechas
data class DateRange(
    val start: Date,
    val end: Date
)
private fun generateColorForLabel(label: String): Color {
    val hue = abs(label.hashCode() % 360)
    return Color.hsv(hue.toFloat(), 0.7f, 0.9f)
}

private fun generateColorForName(name: String): Color {
    val hue = abs(name.hashCode() % 360f)
    return Color.hsv(
        hue = hue,
        saturation = 0.7f,
        value = 0.9f
    )
}