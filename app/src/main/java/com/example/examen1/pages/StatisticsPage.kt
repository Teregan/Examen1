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
import kotlin.math.min
import kotlin.math.roundToInt



// Funciones auxiliares para dibujar el pie chart
private fun drawPieChart(
    canvas: AndroidCanvas,
    width: Int,
    height: Int,
    slices: List<PieChartSlice>
) {
    val center = PointF(width / 2f, height / 2f)
    val radius = min(width, height) / 3f
    var startAngle = -90f

    val paint = Paint()
    paint.style = Paint.Style.FILL

    slices.forEach { slice ->
        paint.color = android.graphics.Color.argb(
            255,
            (slice.color.red * 255).toInt(),
            (slice.color.green * 255).toInt(),
            (slice.color.blue * 255).toInt()
        )

        val path = AndroidPath()
        path.moveTo(center.x, center.y)
        path.arcTo(
            RectF(
                center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius
            ),
            startAngle,
            slice.value * 360,
            false
        )
        path.close()

        canvas.drawPath(path, paint)

        startAngle += slice.value * 360
    }
}

private fun drawLegend(
    canvas: AndroidCanvas,
    width: Int,
    height: Int,
    slices: List<PieChartSlice>
) {
    val legendPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 30f
    }
    val colorPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    var yOffset = height * 0.8f

    slices.forEach { slice ->
        // Dibujar color
        colorPaint.color = android.graphics.Color.argb(
            255,
            (slice.color.red * 255).toInt(),
            (slice.color.green * 255).toInt(),
            (slice.color.blue * 255).toInt()
        )
        canvas.drawRect(
            width * 0.1f,
            yOffset - 20f,
            width * 0.2f,
            yOffset,
            colorPaint
        )

        // Dibujar texto
        canvas.drawText(
            "${slice.name} (${(slice.value * 100).roundToInt()}%)",
            width * 0.3f,
            yOffset,
            legendPaint
        )

        yOffset += 40f
    }
}

@Composable
private fun PieChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier,
    onBitmapCreated: (Bitmap?) -> Unit = {}
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Código existente del gráfico circular
                    var startAngle = -90f
                    val total = data.values.sum()

                    data.forEach { (label, value) ->
                        val sliceAngle = (value / total) * 360f
                        drawArc(
                            color = generateColorForLabel(label),
                            startAngle = startAngle,
                            sweepAngle = sliceAngle,
                            useCenter = true
                        )
                        startAngle += sliceAngle
                    }
                }
            }

            // Agregamos la leyenda debajo del gráfico
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                data.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = generateColorForLabel(label),
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = String.format("%.1f%%", (value / data.values.sum()) * 100),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSeriesDataRow(
    date: String,
    count: Int,
    maxValue: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fecha
        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp)
        )

        // Barra de progreso
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .padding(horizontal = 8.dp)
        ) {
            // Fondo de la barra
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            )

            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth((count / maxValue).coerceIn(0f, 1f))
                    .height(24.dp)
                    .background(MainGreen.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            )

            // Número de alérgenos
            Text(
                text = count.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun StatisticsPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: StatisticsViewModel,
    profileViewModel: ProfileViewModel // Añadir como parámetro
) {
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filtros Dinámicos
        StatisticsFilterSection(
            profiles = profiles,
            onDateRangeChanged = { newRange ->
                dateRange = newRange
                viewModel.generateStatistics(newRange.start, newRange.end, selectedProfile?.id)
            },
            onProfileSelected = { profile ->
                selectedProfile = profile
                viewModel.generateStatistics(dateRange.start, dateRange.end, profile?.id)
            },
            onStatTypeChanged = { statType ->
                selectedStatType = statType
            }
        )

        // Análisis Detallado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                // Contenido del análisis
                StatisticalInsightsCard(statisticalAnalysis)
            }
        }

        // Gráficos
        Text(
            "Visualización de Datos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            item {
                if (foodEntriesStats.isNotEmpty()) {
                    StatisticChartCard(
                        title = "Alérgenos Consumidos",
                        data = foodEntriesStats.associate {
                            it.name to it.percentage.toFloat()
                        }
                    )
                }
            }
            item {
                if (symptomFrequencyStats.isNotEmpty()) {
                    StatisticChartCard(
                        title = "Frecuencia de Síntomas",
                        data = symptomFrequencyStats.associate {
                            it.symptom to it.percentageOfEntries.toFloat()
                        }
                    )
                }
            }
            item {
                if (stoolTypeStats.isNotEmpty()) {
                    StatisticChartCard(
                        title = "Tipos de Deposiciones",
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
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(320.dp)
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
                modifier = Modifier.padding(bottom = 8.dp)
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