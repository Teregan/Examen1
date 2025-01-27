package com.example.examen1.models

import com.google.firebase.Timestamp
import java.util.Date

data class Allergen(
    val id: String,
    val name: String,
    val iconResId: Int,
    var isSelected: Boolean = false,
)

data class FoodEntry(
    val id: String = "",
    val userId: String = "",
    val profileId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val time: String = "",
    val allergens: List<String> = emptyList(),
    val notes: String = "", // Volvemos a string con default vacío
    val createdAt: Long = System.currentTimeMillis(),
    val tagIds: List<String> = emptyList()
) {
    // Solo mantenemos la propiedad para la fecha
    val date: Date
        get() = timestamp.toDate()
}
sealed class FoodEntryState {
    object Initial : FoodEntryState()
    object Loading : FoodEntryState()
    sealed class Success : FoodEntryState() {
        object Save : Success()
        object Load : Success()
    }
    data class Error(val message: String) : FoodEntryState()
}