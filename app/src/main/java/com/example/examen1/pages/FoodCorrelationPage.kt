package com.example.examen1.pages

import PDFService
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.models.FoodCorrelationState
import com.example.examen1.viewmodels.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.examen1.models.ProfileType
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.BorderStroke
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCorrelationPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: FoodCorrelationViewModel,
    foodEntryViewModel: FoodEntryViewModel,
    symptomEntryViewModel: SymptomEntryViewModel,
    activeProfileViewModel: ActiveProfileViewModel,
    profileViewModel: ProfileViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val profiles = profileViewModel.profiles.observeAsState(initial = emptyList())

    var startDate by remember { mutableStateOf(Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)) }
    var endDate by remember { mutableStateOf(Date()) }
    var selectedProfileId by remember { mutableStateOf<String?>(null) }

    val correlationState by viewModel.correlationState.observeAsState()

    val symptomTranslations = mapOf(
        "headache" to "Dolor de cabeza",
        "nausea" to "Náuseas",
        "stomachache" to "Malestar estomacal",
        "diarrhea" to "Diarrea",
        "constipation" to "Estreñimiento",
        "rash" to "Erupciones en la piel"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Correlaciones de Alimentos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Seleccionar Perfil",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Chips de selección de perfil
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    FilterChip(
                        selected = selectedProfileId == null,
                        onClick = {
                            selectedProfileId = null
                            viewModel.analyzeCorrelations(startDate, endDate, null)
                        },
                        label = { Text("Todos") },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary
                        )
                    )

                    profiles.value.forEach { profile ->
                        FilterChip(
                            selected = selectedProfileId == profile.id,
                            onClick = {
                                selectedProfileId = profile.id
                                viewModel.analyzeCorrelations(startDate, endDate, profile.id)
                            },
                            label = { Text(profile.name) },
                            leadingIcon = {
                                if (profile.profileType == ProfileType.MOTHER) {
                                    Icon(Icons.Default.Person, "Madre")
                                } else {
                                    Icon(Icons.Default.Face, "Niño")
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary,
                                selectedLabelColor = colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Selector de fecha inicial
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, colorScheme.outline),
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.time = startDate
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                startDate = calendar.time
                                viewModel.analyzeCorrelations(startDate, endDate, selectedProfileId)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Text(
                        text = "Fecha inicial: ${dateFormatter.format(startDate)}",
                        modifier = Modifier.padding(16.dp),
                        color = colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de fecha final
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, colorScheme.outline),
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.time = endDate
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                endDate = calendar.time
                                viewModel.analyzeCorrelations(startDate, endDate, selectedProfileId)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Text(
                        text = "Fecha final: ${dateFormatter.format(endDate)}",
                        modifier = Modifier.padding(16.dp),
                        color = colorScheme.onSurface
                    )
                }
            }

            // Estado de correlación
            item {
                when (val state = correlationState) {
                    is FoodCorrelationState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.Center),
                            color = colorScheme.primary
                        )
                    }
                    is FoodCorrelationState.Success -> {
                        if (state.correlations.isEmpty()) {
                            Text(
                                text = "No se encontraron correlaciones en el rango de fechas",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = colorScheme.onSurface
                            )
                        } else {
                            // Calcular conteos para gráficos
                            val allergenCounts = mutableMapOf<String, Int>()
                            val symptomCounts = mutableMapOf<String, Int>()

                            state.correlations.forEach { correlation ->
                                correlation.foodEntry.allergens.forEach { allergenId ->
                                    val allergenName = foodEntryViewModel.allergens.find { it.id == allergenId }?.name ?: allergenId
                                    allergenCounts[allergenName] = (allergenCounts[allergenName] ?: 0) + 1
                                }

                                correlation.relatedSymptoms.forEach { symptom ->
                                    symptom.symptoms.forEach { symptomId ->
                                        val symptomName = symptomTranslations[symptomId] ?: symptomId
                                        symptomCounts[symptomName] = (symptomCounts[symptomName] ?: 0) + 1
                                    }
                                }
                            }

                            // Botón de compartir
                            Button(
                                onClick = {
                                    val pdfService = PDFService(
                                        context = context,
                                        foodEntryViewModel = foodEntryViewModel,
                                        symptomEntryViewModel = symptomEntryViewModel
                                    )
                                    pdfService.generateAndShareCorrelationReport(
                                        correlations = state.correlations,
                                        profileName = selectedProfileId?.let { id ->
                                            profiles.value.find { it.id == id }?.name
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary,
                                    contentColor = colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Compartir",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Compartir Informe")
                                }
                            }

                            // Gráficos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                PieChart(
                                    title = "Alimentos más repetidos",
                                    data = allergenCounts
                                )

                                PieChart(
                                    title = "Síntomas más repetidos",
                                    data = symptomCounts
                                )
                            }
                        }
                    }
                    is FoodCorrelationState.Error -> {
                        Text(
                            text = state.message,
                            color = colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> Unit
                }
            }

            // Lista de correlaciones detalladas
            if (correlationState is FoodCorrelationState.Success) {
                val state = correlationState as FoodCorrelationState.Success
                items(state.correlations) { correlation ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Detalles de alimentos
                            Text(
                                "Alimentos: ${
                                    correlation.foodEntry.allergens.map { allergenId ->
                                        foodEntryViewModel.allergens.find { it.id == allergenId }?.name ?: allergenId
                                    }.joinToString(", ")
                                }",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurface
                            )
                            Text(
                                "Fecha: ${dateFormatter.format(correlation.foodEntry.date)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurface
                            )

                            // Síntomas relacionados
                            if (correlation.relatedSymptoms.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Síntomas Relacionados:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface
                                )
                                correlation.relatedSymptoms.forEach { symptom ->
                                    Text(
                                        "- ${
                                            symptom.symptoms.mapNotNull { symptomId ->
                                                symptomTranslations[symptomId] ?: symptomId
                                            }.joinToString(", ")
                                        } (${dateFormatter.format(symptom.date)})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurface
                                    )
                                }
                            }

                            // Deposiciones relacionadas
                            if (correlation.relatedStoolEntries.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Deposiciones Relacionadas:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface
                                )
                                correlation.relatedStoolEntries.forEach { stool ->
                                    Text(
                                        "- Tipo: ${stool.stoolType.displayName}, Color: ${stool.color.displayName} (${dateFormatter.format(stool.date)})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    title: String,
    data: MutableMap<String, Int>,
    onBitmapCreated: (Bitmap?) -> Unit = {}
) {
    val context = LocalContext.current

    val total = data.values.sum()
    // Usa una semilla para generar colores consistentes
    val slices = remember(data) {
        data.map { (name, count) ->
            val hash = name.hashCode()
            PieChartSlice(
                name = name,
                value = count.toFloat() / total,
                color = androidx.compose.ui.graphics.Color(
                    red = ((hash shr 16) and 0xFF) / 255f,
                    green = ((hash shr 8) and 0xFF) / 255f,
                    blue = (hash and 0xFF) / 255f
                )
            )
        }
    }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val drawModifier = Modifier
        .width(180.dp)
        .onGloballyPositioned { coordinates ->
            val width = coordinates.size.width
            val height = coordinates.size.height

            if (width > 0 && height > 0) {
                bitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
                ).also { bmp ->
                    val canvas = Canvas(bmp)

                    // Dibujar título
                    val titlePaint = Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 40f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(title, width / 2f, 50f, titlePaint)

                    // Dibujar gráfico de pastel
                    drawPieChart(canvas, width, height, slices)

                    // Dibujar leyenda
                    drawLegend(canvas, width, height, slices)

                    onBitmapCreated(bmp)
                }
            }
        }

    Card(modifier = drawModifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)

            Canvas(modifier = Modifier.size(150.dp)) {
                var startAngle = -90f
                slices.forEach { slice ->
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = slice.value * 360,
                        useCenter = true
                    )
                    startAngle += slice.value * 360
                }
            }

            slices.forEach { slice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(slice.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${slice.name} (${(slice.value * 100).roundToInt()}%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Función para dibujar el gráfico de pastel
private fun drawPieChart(
    canvas: Canvas,
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

// Función para dibujar la leyenda
private fun drawLegend(
    canvas: Canvas,
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

data class PieChartSlice(
    val name: String,
    val value: Float,
    val color: Color
)