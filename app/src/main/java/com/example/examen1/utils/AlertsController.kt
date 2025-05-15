package com.example.examen1.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.examen1.components.AlertType

class AlertsController {
    // Estado para SweetAlert
    var showAlert by mutableStateOf(false)
        private set

    var alertType by mutableStateOf(AlertType.INFO)
        private set

    var alertTitle by mutableStateOf("")
        private set

    var alertMessage by mutableStateOf("")
        private set

    var alertConfirmText by mutableStateOf("Aceptar")
        private set

    var alertOnConfirm by mutableStateOf({})
        private set

    var alertAutoDismissTime by mutableStateOf<Long?>(null)
        private set

    // Estado para SweetToast
    var showToast by mutableStateOf(false)
        private set

    var toastMessage by mutableStateOf("")
        private set

    var toastType by mutableStateOf(AlertType.INFO)
        private set

    var toastDuration by mutableStateOf(2000L)
        private set

    // Métodos para mostrar alertas
    fun showSuccessAlert(
        title: String = "¡Éxito!",
        message: String,
        confirmText: String = "Aceptar",
        onConfirm: () -> Unit = {},
        autoDismissTime: Long? = 3000
    ) {
        alertType = AlertType.SUCCESS
        alertTitle = title
        alertMessage = message
        alertConfirmText = confirmText
        alertOnConfirm = onConfirm
        alertAutoDismissTime = autoDismissTime
        showAlert = true
    }

    fun showErrorAlert(
        title: String = "Error",
        message: String,
        confirmText: String = "Aceptar",
        onConfirm: () -> Unit = {}
    ) {
        alertType = AlertType.ERROR
        alertTitle = title
        alertMessage = message
        alertConfirmText = confirmText
        alertOnConfirm = onConfirm
        alertAutoDismissTime = null
        showAlert = true
    }

    fun showWarningAlert(
        title: String = "Advertencia",
        message: String,
        confirmText: String = "Aceptar",
        onConfirm: () -> Unit = {}
    ) {
        alertType = AlertType.WARNING
        alertTitle = title
        alertMessage = message
        alertConfirmText = confirmText
        alertOnConfirm = onConfirm
        alertAutoDismissTime = null
        showAlert = true
    }

    fun showInfoAlert(
        title: String = "Información",
        message: String,
        confirmText: String = "Aceptar",
        onConfirm: () -> Unit = {}
    ) {
        alertType = AlertType.INFO
        alertTitle = title
        alertMessage = message
        alertConfirmText = confirmText
        alertOnConfirm = onConfirm
        alertAutoDismissTime = null
        showAlert = true
    }

    fun hideAlert() {
        showAlert = false
    }

    // Métodos para mostrar toasts
    fun showSuccessToast(message: String, duration: Long = 2000) {
        toastType = AlertType.SUCCESS
        toastMessage = message
        toastDuration = duration
        showToast = true
    }

    fun showErrorToast(message: String, duration: Long = 2000) {
        toastType = AlertType.ERROR
        toastMessage = message
        toastDuration = duration
        showToast = true
    }

    fun showWarningToast(message: String, duration: Long = 2000) {
        toastType = AlertType.WARNING
        toastMessage = message
        toastDuration = duration
        showToast = true
    }

    fun showInfoToast(message: String, duration: Long = 2000) {
        toastType = AlertType.INFO
        toastMessage = message
        toastDuration = duration
        showToast = true
    }

    fun hideToast() {
        showToast = false
    }
}

// CompositionLocal para acceder al AlertsController desde cualquier parte de la UI
val LocalAlertsController = compositionLocalOf { AlertsController() }