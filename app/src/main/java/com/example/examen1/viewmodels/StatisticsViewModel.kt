package com.example.examen1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.FoodEntry
import com.example.examen1.models.StoolEntry
import com.example.examen1.models.StoolType
import com.example.examen1.models.SymptomEntry
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class StatisticsViewModel(
    private val foodEntryViewModel: FoodEntryViewModel,
    private val symptomEntryViewModel: SymptomEntryViewModel,
    private val stoolEntryViewModel: StoolEntryViewModel,
    private val profileViewModel: ProfileViewModel
) : ViewModel() {

    // Mantén los LiveData existentes
    private val _foodEntriesOverTime = MutableLiveData<List<FoodStatData>>()
    val foodEntriesOverTime: LiveData<List<FoodStatData>> = _foodEntriesOverTime

    private val _symptomFrequency = MutableLiveData<List<SymptomStatData>>()
    val symptomFrequency: LiveData<List<SymptomStatData>> = _symptomFrequency

    private val _stoolTypeDistribution = MutableLiveData<List<StoolStatData>>()
    val stoolTypeDistribution: LiveData<List<StoolStatData>> = _stoolTypeDistribution

    // Nuevo LiveData para análisis
    private val _comprehensiveAnalysis = MutableLiveData<StatisticalAnalysis>()
    val comprehensiveAnalysis: LiveData<StatisticalAnalysis> = _comprehensiveAnalysis

    // Tipos de datos para estadísticas
    data class FoodStatData(
        val name: String,
        val count: Int,
        val percentage: Float
    )

    data class SymptomStatData(
        val symptom: String,
        val frequency: Int,
        val percentageOfEntries: Double
    )

    data class StoolStatData(
        val type: String,
        val count: Int,
        val percentage: Double
    )

    fun generateStatistics(
        startDate: Date,
        endDate: Date,
        profileId: String? = null
    ) {
        viewModelScope.launch {
            // Obtener entradas filtradas
            val foodEntries = foodEntryViewModel.searchByDateRange(startDate, endDate)
                .let { entries ->
                    profileId?.let {
                        entries.filter { it.profileId == profileId }
                    } ?: entries
                }

            val symptomEntries = symptomEntryViewModel.searchByDateRange(startDate, endDate)
                .let { entries ->
                    profileId?.let {
                        entries.filter { it.profileId == profileId }
                    } ?: entries
                }

            val stoolEntries = stoolEntryViewModel.searchByDateRange(startDate, endDate)
                .let { entries ->
                    profileId?.let {
                        entries.filter { it.profileId == profileId }
                    } ?: entries
                }

            // Procesar estadísticas de alimentos
            val foodStats = processFoodStatistics(foodEntries)
            _foodEntriesOverTime.value = foodStats

            // Procesar estadísticas de síntomas
            val symptomStats = processSymptomStatistics(symptomEntries)
            _symptomFrequency.value = symptomStats

            // Procesar estadísticas de deposiciones
            val stoolStats = processStoolStatistics(stoolEntries)
            _stoolTypeDistribution.value = stoolStats

            // Generar análisis comprehensivo
            val analysis = generateComprehensiveAnalysis(
                foodEntries = foodStats,
                symptoms = symptomStats,
                stoolTypes = stoolStats
            )
            _comprehensiveAnalysis.value = analysis
        }
    }

    private fun processFoodStatistics(entries: List<FoodEntry>): List<FoodStatData> {
        // Aplanamos todos los alérgenos y contamos sus ocurrencias
        val allergenCounts = entries
            .flatMap { it.allergens }
            .groupBy { it }
            .mapValues { it.value.size }

        val total = allergenCounts.values.sum().toFloat()

        // Convertimos a porcentajes
        return allergenCounts.map { (allergenId, count) ->
            val allergenName = foodEntryViewModel.allergens
                .find { it.id == allergenId }?.name ?: allergenId

            FoodStatData(
                name = allergenName,
                count = count,
                percentage = (count / total) * 100
            )
        }.sortedByDescending { it.percentage }
    }



    private fun processSymptomStatistics(entries: List<SymptomEntry>): List<SymptomStatData> {
        val totalEntries = entries.size

        return entries
            .flatMap { it.symptoms }
            .groupBy { it }
            .map { (symptomId, occurrences) ->
                val symptomName = symptomEntryViewModel.predefinedSymptoms
                    .find { it.id == symptomId }?.name ?: symptomId

                SymptomStatData(
                    symptom = symptomName,
                    frequency = occurrences.size,
                    percentageOfEntries = (occurrences.size.toDouble() / totalEntries) * 100
                )
            }
            .sortedByDescending { it.frequency }
    }

    private fun processStoolStatistics(entries: List<StoolEntry>): List<StoolStatData> {
        val totalEntries = entries.size

        return entries
            .groupBy { it.stoolType }
            .map { (type, typeEntries) ->
                val typeName = when(type) {
                    StoolType.LIQUID -> "Líquida"
                    StoolType.HARD -> "Dura"
                    StoolType.PELLETS -> "Pelotitas"
                    StoolType.NORMAL -> "Normal"
                }

                StoolStatData(
                    type = typeName,
                    count = typeEntries.size,
                    percentage = (typeEntries.size.toDouble() / totalEntries) * 100
                )
            }
            .sortedByDescending { it.count }
    }

    private fun generateComprehensiveAnalysis(
        foodEntries: List<FoodStatData>,
        symptoms: List<SymptomStatData>,
        stoolTypes: List<StoolStatData>
    ): StatisticalAnalysis {
        val summaryBuilder = StringBuilder()
        val alerts = mutableListOf<String>()

        // Análisis de Alimentos
        val totalAllergens = foodEntries.sumOf { it.count }
        val topAllergens = foodEntries
            .sortedByDescending { it.percentage }
            .take(3)

        summaryBuilder.append("En el período analizado:\n")
        summaryBuilder.append("• Se registraron $totalAllergens exposiciones a alérgenos\n")

        if (topAllergens.isNotEmpty()) {
            summaryBuilder.append("• Alérgenos más frecuentes:\n")
            topAllergens.forEach { allergen ->
                summaryBuilder.append("  - ${allergen.name}: ${String.format("%.1f", allergen.percentage)}%\n")
            }
        }

        // Análisis de Síntomas
        val topSymptoms = symptoms.take(3)
        summaryBuilder.append("\nSíntomas más frecuentes:\n")
        topSymptoms.forEach { symptom ->
            summaryBuilder.append("• ${symptom.symptom}: ${symptom.frequency} registros (${String.format("%.2f", symptom.percentageOfEntries)}%)\n")
        }

        // Análisis de Deposiciones
        val stoolDistribution = stoolTypes.map { "${it.type}: ${String.format("%.2f", it.percentage)}%" }
        summaryBuilder.append("\nDistribución de tipos de deposiciones:\n")
        summaryBuilder.append("• ${stoolDistribution.joinToString(", ")}\n")

        // Generación de Alertas
        topAllergens
            .filter { it.percentage > 30 }
            .forEach { allergen ->
                alerts.add("Alta frecuencia de exposición a ${allergen.name} (${String.format("%.1f", allergen.percentage)}%)")
            }

        if (topSymptoms.any { it.percentageOfEntries > 30 }) {
            alerts.add("Se han registrado síntomas frecuentes que pueden requerir atención médica")
        }

        if (stoolTypes.any { it.type != "Normal" && it.percentage > 40 }) {
            alerts.add("Se observan variaciones significativas en los tipos de deposiciones")
        }

        return StatisticalAnalysis(
            summary = summaryBuilder.toString(),
            alerts = alerts
        )
    }
}

// Estructura de datos para Análisis
data class StatisticalAnalysis(
    val summary: String,
    val alerts: List<String>
)