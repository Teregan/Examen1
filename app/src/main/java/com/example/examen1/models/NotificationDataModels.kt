package com.example.examen1.models

data class AllergenNotificationConfig(
    val id: String = "",
    val userId: String = "",
    val profileId: String = "",
    val controlId: String = "",
    val allergenId: String = "",
    val notifyTime: String = "07:49", // Hora de notificación por defecto
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

sealed class NotificationState {
    object Initial : NotificationState()
    object Loading : NotificationState()
    sealed class Success : NotificationState() {
        object Save : Success()
        object Load : Success()
    }
    data class Error(val message: String) : NotificationState()
}