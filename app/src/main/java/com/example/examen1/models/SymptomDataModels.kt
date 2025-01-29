package com.example.examen1.models

import com.google.firebase.Timestamp
import java.util.Date

data class Symptom(
    val id: String,
    val name: String,
    val iconResId: Int = 0,
    var isSelected: Boolean = false,
    var isCustom: Boolean = false,

)

data class SymptomEntry(
    val id: String = "",
    val userId: String = "",
    val profileId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val time: String = "",
    val symptoms: List<String> = emptyList(), // Lista de IDs de síntomas
    val customSymptoms: List<String> = emptyList(), // Lista de síntomas personalizados
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val imagesPaths: List<String> = emptyList()
) {
    val date: Date
        get() = timestamp.toDate()
}

sealed class SymptomEntryState {
    object Initial : SymptomEntryState() // Agregamos este estado
    object Loading : SymptomEntryState()
    sealed class Success : SymptomEntryState() {
        object Save : Success()
        object Load : Success()
    }
    data class Error(val message: String) : SymptomEntryState()
}