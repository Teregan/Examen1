package com.example.examen1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NotificationViewModelFactory(
    private val foodEntryViewModel: FoodEntryViewModel,
    private val controlTypeViewModel: ControlTypeViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(foodEntryViewModel, controlTypeViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}