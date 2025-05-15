package com.example.examen1.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.HistoryState
import com.example.examen1.pages.RecordType
import com.example.examen1.services.PDFGenerator
import com.example.examen1.services.sharePDF
import kotlinx.coroutines.launch
import java.util.Date

enum class RecordType {
    ALL,        // Todos los registros
    FOOD,       // Registros de alimentos
    SYMPTOM,    // Registros de síntomas
    STOOL,      // Registros de deposiciones
    CONTROL     // Registros de control de alérgenos
}

class HistoryViewModel(
    private val foodEntryViewModel: FoodEntryViewModel,
    private val symptomEntryViewModel: SymptomEntryViewModel,
    private val stoolEntryViewModel: StoolEntryViewModel,
    private val controlTypeViewModel: ControlTypeViewModel
) : ViewModel() {

    private val _historyState = MutableLiveData<HistoryState>(HistoryState.Initial)
    val historyState: LiveData<HistoryState> = _historyState

    fun searchRecords(
        startDate: Date = Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000), // Default to last 30 days
        endDate: Date = Date(),
        recordType: RecordType = RecordType.ALL,
        selectedTags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                _historyState.value = HistoryState.Loading

                val foodEntries = when (recordType) {
                    RecordType.ALL, RecordType.FOOD -> foodEntryViewModel.searchByDateRange(startDate, endDate)
                        .filter { entry ->
                            selectedTags.isEmpty() || entry.tagIds.any { it in selectedTags }
                        }
                    else -> emptyList()
                }

                val symptomEntries = when (recordType) {
                    RecordType.ALL, RecordType.SYMPTOM -> symptomEntryViewModel.searchByDateRange(startDate, endDate)
                    else -> emptyList()
                }

                val stoolEntries = when (recordType) {
                    RecordType.ALL, RecordType.STOOL -> stoolEntryViewModel.searchByDateRange(startDate, endDate)
                    else -> emptyList()
                }

                val controlEntries = when (recordType) {
                    RecordType.ALL, RecordType.CONTROL -> {
                        controlTypeViewModel.searchByDateRange(startDate, endDate)
                    }
                    else -> emptyList()
                }

                if (foodEntries.isEmpty() && symptomEntries.isEmpty() &&
                    stoolEntries.isEmpty() && controlEntries.isEmpty()) {
                    _historyState.value = HistoryState.Error("No se encontraron registros en el rango de fechas")
                } else {
                    _historyState.value = HistoryState.Success(
                        foodEntries = foodEntries,
                        symptomEntries = symptomEntries,
                        stoolEntries = stoolEntries,
                        controlEntries = controlEntries  // Agregar los controlEntries al estado
                    )
                }
            } catch (e: Exception) {
                _historyState.value = HistoryState.Error(e.message ?: "Error al buscar registros")
            }
        }
    }
    fun generateAndShareHistoryPDF(context: Context) {
        viewModelScope.launch {
            try {
                val currentState = historyState.value
                when (currentState) {
                    is HistoryState.Success -> {
                        // Verificar si hay datos para exportar
                        if (currentState.foodEntries.isEmpty() &&
                            currentState.symptomEntries.isEmpty() &&
                            currentState.stoolEntries.isEmpty() &&
                            currentState.controlEntries.isEmpty()) {

                            Toast.makeText(
                                context,
                                "No hay datos disponibles para exportar",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        val pdfGenerator =  PDFGenerator(
                            context = context,
                            foodEntryViewModel = foodEntryViewModel,
                            symptomEntryViewModel = symptomEntryViewModel
                        )
                        val file = pdfGenerator.generateHistoryPDF(
                            foodEntries = currentState.foodEntries,
                            symptomEntries = currentState.symptomEntries,
                            stoolEntries = currentState.stoolEntries,
                            controlEntries = currentState.controlEntries
                        )
                        sharePDF(context, file)
                    }
                    is HistoryState.Loading -> {
                        Toast.makeText(
                            context,
                            "Por favor espere mientras se cargan los datos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is HistoryState.Error -> {
                        Toast.makeText(
                            context,
                            "Error al cargar los datos: ${currentState.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "No hay datos disponibles. Intente de nuevo más tarde",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PDFGeneration", "Error generando PDF", e)
                Toast.makeText(
                    context,
                    "Error al generar el PDF: ${e.message ?: "Error desconocido"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}