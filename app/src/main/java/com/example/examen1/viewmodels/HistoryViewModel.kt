package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.HistoryState
import com.example.examen1.pages.RecordType
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
        recordType: RecordType = RecordType.ALL
    ) {
        viewModelScope.launch {
            try {
                _historyState.value = HistoryState.Loading

                val foodEntries = when (recordType) {
                    RecordType.ALL, RecordType.FOOD -> foodEntryViewModel.searchByDateRange(startDate, endDate)
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
}