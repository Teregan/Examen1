package com.example.examen1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FoodCorrelationViewModelFactory(
    private val foodEntryViewModel: FoodEntryViewModel,
    private val symptomEntryViewModel: SymptomEntryViewModel,
    private val stoolEntryViewModel: StoolEntryViewModel,
    private val profileViewModel: ProfileViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodCorrelationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodCorrelationViewModel(
                foodEntryViewModel,
                symptomEntryViewModel,
                stoolEntryViewModel,
                profileViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}