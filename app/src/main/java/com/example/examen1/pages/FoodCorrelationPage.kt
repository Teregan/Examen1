package com.example.examen1.pages

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examen1.models.FoodCorrelationState
import com.example.examen1.ui.theme.PrimaryPinkDark
import com.example.examen1.viewmodels.FoodCorrelationViewModel
import com.example.examen1.viewmodels.FoodEntryViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCorrelationPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: FoodCorrelationViewModel,
    foodEntryViewModel: FoodEntryViewModel
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var startDate by remember { mutableStateOf(Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)) }
    var endDate by remember { mutableStateOf(Date()) }

    val correlationState by viewModel.correlationState.observeAsState()

    val symptomTranslations = mapOf(
        "headache" to "Dolor de cabeza",
        "nausea" to "Náuseas",
        "stomachache" to "Malestar estomacal",
        "diarrhea" to "Diarrea",
        "constipation" to "Estreñimiento",
        "rash" to "Erupciones en la piel"
    )

    // Efecto para cargar correlaciones al inicio
    LaunchedEffect(Unit) {
        viewModel.analyzeCorrelations(startDate, endDate)
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Correlaciones de Alimentos") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Selector de fecha inicial
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val calendar = Calendar.getInstance()
                    calendar.time = startDate
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(year, month, day)
                            startDate = calendar.time
                            viewModel.analyzeCorrelations(startDate, endDate)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de fecha final
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val calendar = Calendar.getInstance()
                    calendar.time = endDate
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(year, month, day)
                            endDate = calendar.time
                            viewModel.analyzeCorrelations(startDate, endDate)
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

            Spacer(modifier = Modifier.height(16.dp))


            // Mostrar resultados
            when (val state = correlationState) {
                is FoodCorrelationState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is FoodCorrelationState.Success -> {
                    if (state.correlations.isEmpty()) {
                        Text(
                            text = "No se encontraron correlaciones en el rango de fechas",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        val allergenCounts = mutableMapOf<String, Int>()
                        val symptomCounts = mutableMapOf<String, Int>()

                        state.correlations.forEach { correlation ->
                            correlation.foodEntry.allergens.forEach { allergenId ->
                                val allergenName = foodEntryViewModel.allergens.find { it.id == allergenId }?.name
                                    ?: allergenId
                                allergenCounts[allergenName] = (allergenCounts[allergenName] ?: 0) + 1
                            }

                            correlation.relatedSymptoms.forEach { symptom ->
                                symptom.symptoms.forEach { symptomId ->
                                    val symptomName = symptomTranslations[symptomId] ?: symptomId
                                    symptomCounts[symptomName] = (symptomCounts[symptomName] ?: 0) + 1
                                }
                            }
                        }

                        // Mostrar gráficos de torta
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

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn {
                            items(state.correlations) { correlation ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
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
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            "Fecha: ${dateFormatter.format(correlation.foodEntry.date)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        // Síntomas relacionados
                                        if (correlation.relatedSymptoms.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Síntomas Relacionados:",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            correlation.relatedSymptoms.forEach { symptom ->
                                                Text(
                                                    "- ${
                                                        symptom.symptoms.mapNotNull { symptomId ->
                                                            symptomTranslations[symptomId] ?: symptomId
                                                        }.joinToString(", ")
                                                    } (${dateFormatter.format(symptom.date)})",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }

                                        // Deposiciones relacionadas
                                        if (correlation.relatedStoolEntries.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Deposiciones Relacionadas:",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            correlation.relatedStoolEntries.forEach { stool ->
                                                Text(
                                                    "- Tipo: ${stool.stoolType}, Color: ${stool.color} (${dateFormatter.format(stool.date)})",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is FoodCorrelationState.Error -> {
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
fun PieChart(
    title: String,
    data: Map<String, Int>
) {
    val total = data.values.sum()
    val slices = data.map { (name, count) ->
        PieChartSlice(
            name = name,
            value = count.toFloat() / total,
            color = Color(
                red = (Math.random() * 256).toInt(),
                green = (Math.random() * 256).toInt(),
                blue = (Math.random() * 256).toInt()
            )
        )
    }

    Card(modifier = Modifier.width(180.dp)) {
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

data class PieChartSlice(
    val name: String,
    val value: Float,
    val color: Color
)