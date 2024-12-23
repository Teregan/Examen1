package com.example.examen1.models

data class FoodCorrelation(
    val foodEntry: FoodEntry,
    val relatedSymptoms: List<SymptomEntry> = emptyList(),
    val relatedStoolEntries: List<StoolEntry> = emptyList()
)

sealed class FoodCorrelationState {
    object Initial : FoodCorrelationState()
    object Loading : FoodCorrelationState()
    data class Success(val correlations: List<FoodCorrelation>) : FoodCorrelationState()
    data class Error(val message: String) : FoodCorrelationState()
}