package com.example.examen1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HistoryViewModelFactory(
    private val foodEntryViewModel: FoodEntryViewModel,
    private val symptomEntryViewModel: SymptomEntryViewModel,
    private val stoolEntryViewModel: StoolEntryViewModel,
    private val controlTypeViewModel: ControlTypeViewModel  // Nuevo parámetro
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                foodEntryViewModel,
                symptomEntryViewModel,
                stoolEntryViewModel,
                controlTypeViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}