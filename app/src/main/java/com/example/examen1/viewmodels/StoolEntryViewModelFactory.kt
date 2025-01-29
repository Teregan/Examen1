package com.example.examen1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.examen1.utils.image.ImageManager

class StoolEntryViewModelFactory(
    private val imageManager: ImageManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoolEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoolEntryViewModel(imageManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    companion object {
        fun provideFactory(
            imageManager: ImageManager
        ): ViewModelProvider.Factory = StoolEntryViewModelFactory(imageManager)
    }
}