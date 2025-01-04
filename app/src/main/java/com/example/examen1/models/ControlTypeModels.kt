package com.example.examen1.models

import android.util.Log
import com.google.firebase.Timestamp
import java.util.Date

data class AllergenControl(
    val id: String = "",
    val userId: String = "",
    val profileId: String = "",
    val controlType: ControlType = ControlType.ELIMINATION,
    val allergenId: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val notes: String = "",
    var isActive: Boolean = true,  // Cambiar a var y asegurarnos que coincide con Firestore
    val createdAt: Long = System.currentTimeMillis()
) {
    val startDateAsDate: Date
        get() = startDate.toDate()

    val endDateAsDate: Date
        get() = endDate.toDate()

    fun isCurrentlyActive(): Boolean {
        val currentDate = Date()
        return isActive &&
                !currentDate.before(startDateAsDate) &&
                !currentDate.after(endDateAsDate)
    }
}

// Asegurarnos que el enum ControlType puede ser deserializado correctamente
enum class ControlType(val displayName: String) {
    @field:JvmField  // Agregar esta anotación
    ELIMINATION("Eliminación"),

    @field:JvmField  // Agregar esta anotación
    CONTROLLED("Controlada");

    companion object {
        fun fromString(value: String): ControlType {
            return try {
                valueOf(value)
            } catch (e: Exception) {
                ELIMINATION
            }
        }
    }
}

sealed class ControlTypeState {
    object Initial : ControlTypeState()
    object Loading : ControlTypeState()
    sealed class Success : ControlTypeState() {
        object Save : Success()
        object Load : Success()
    }
    data class Error(val message: String) : ControlTypeState()
}

