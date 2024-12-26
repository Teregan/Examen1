package com.example.examen1.models

import com.google.firebase.Timestamp
import java.util.Date

enum class ControlType(val displayName: String) {
    ELIMINATION("Eliminación"),
    CONTROLLED("Controlada")
}
data class AllergenControl(
    val id: String = "",
    val userId: String = "",
    val profileId: String = "",
    val controlType: ControlType = ControlType.ELIMINATION,
    val allergenId: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val startDateAsDate: Date
        get() = startDate.toDate()

    val endDateAsDate: Date
        get() = endDate.toDate()
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

