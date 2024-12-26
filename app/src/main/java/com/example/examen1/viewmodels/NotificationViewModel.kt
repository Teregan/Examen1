package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.examen1.AllergyApp
import com.example.examen1.models.AllergenNotificationConfig
import com.example.examen1.models.NotificationState
import com.example.examen1.services.AllergenNotificationService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit
import java.util.Date

class NotificationViewModel(
    private val foodEntryViewModel: FoodEntryViewModel,
    private val controlTypeViewModel: ControlTypeViewModel
) : BaseEntryViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "NotificationViewModel"

    private val _notificationState = MutableLiveData<NotificationState>()
    val notificationState: LiveData<NotificationState> = _notificationState

    private val _notifications = MutableLiveData<List<AllergenNotificationConfig>>()
    val notifications: LiveData<List<AllergenNotificationConfig>> = _notifications

    fun setupNotification(
        profileId: String,
        controlId: String,
        allergenId: String,
        notifyTime: String,
        isEnabled: Boolean
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando configuración de notificación")
                _notificationState.value = NotificationState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                // Obtener el nombre del alérgeno
                val allergen = foodEntryViewModel.allergens.find { it.id == allergenId }
                    ?: throw Exception("Alérgeno no encontrado")

                // Obtener el control para calcular días restantes
                val control = controlTypeViewModel.activeControls.value?.find { it.id == controlId }
                    ?: throw Exception("Control no encontrado")

                // Calcular días restantes
                val daysRemaining = ChronoUnit.DAYS.between(
                    Date().toInstant(),
                    control.endDateAsDate.toInstant()
                ).toInt()

                Log.d(TAG, "Días restantes calculados: $daysRemaining")

                val notificationData = AllergenNotificationConfig(
                    userId = currentUserId,
                    profileId = profileId,
                    controlId = controlId,
                    allergenId = allergenId,
                    notifyTime = notifyTime,
                    isEnabled = isEnabled
                )

                firestore.collection("notifications")
                    .add(notificationData)
                    .addOnSuccessListener {
                        if (isEnabled) {
                            Log.d(TAG, "Configurando notificación para ${allergen.name}")
                            val context = AllergyApp.getInstance()
                            AllergenNotificationService.scheduleNotification(
                                context,
                                allergen.name,
                                daysRemaining,
                                notifyTime
                            )
                        }
                        _notificationState.value = NotificationState.Success.Save
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error guardando notificación", e)
                        _notificationState.value = NotificationState.Error(e.message ?: "Error saving notification")
                    }

            } catch (e: Exception) {
                Log.e(TAG, "Error en setupNotification", e)
                _notificationState.value = NotificationState.Error(e.message ?: "Error setting up notification")
            }
        }
    }

    fun updateNotificationStatus(notificationId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                _notificationState.value = NotificationState.Loading

                firestore.collection("notifications")
                    .document(notificationId)
                    .update("isEnabled", isEnabled)
                    .addOnSuccessListener {
                        _notificationState.value = NotificationState.Success.Save
                    }
                    .addOnFailureListener { e ->
                        _notificationState.value = NotificationState.Error(e.message ?: "Error updating notification")
                    }
            } catch (e: Exception) {
                _notificationState.value = NotificationState.Error(e.message ?: "Error updating notification")
            }
        }
    }

    fun loadNotificationsForProfile(profileId: String) {
        setupListener("notifications", profileId) { snapshot ->
            val notificationsList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AllergenNotificationConfig::class.java)?.copy(id = doc.id)
            }
            _notifications.value = notificationsList
        }
    }
}