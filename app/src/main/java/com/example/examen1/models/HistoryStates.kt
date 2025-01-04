package com.example.examen1.models

import com.example.examen1.models.FoodEntry
import com.example.examen1.models.StoolEntry
import com.example.examen1.models.SymptomEntry

sealed class HistoryState {
    object Initial : HistoryState()
    object Loading : HistoryState()
    data class Success(
        val foodEntries: List<FoodEntry> = emptyList(),
        val symptomEntries: List<SymptomEntry> = emptyList(),
        val stoolEntries: List<StoolEntry> = emptyList(),
        val controlEntries: List<AllergenControl> = emptyList()
    ) : HistoryState()
    data class Error(val message: String) : HistoryState()
}