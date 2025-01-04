package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.AllergenControl
import com.example.examen1.models.ControlType
import com.example.examen1.models.ControlTypeState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ControlTypeViewModel : BaseEntryViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _controlTypeState = MutableLiveData<ControlTypeState>()
    val controlTypeState: LiveData<ControlTypeState> = _controlTypeState

    private val _activeControls = MutableLiveData<List<AllergenControl>>()
    val activeControls: LiveData<List<AllergenControl>> = _activeControls

    private var controlsListener: ListenerRegistration? = null

    init {
        setupControlsListener()
    }

    private fun setupControlsListener(profileId: String? = null) {
        val currentUserId = auth.currentUser?.uid ?: return

        val query = firestore.collection("allergen_controls")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isActive", true)

        controlsListener?.remove()

        controlsListener = (if (profileId != null) {
            query.whereEqualTo("profileId", profileId)
        } else {
            query
        }).addSnapshotListener { snapshot, error ->
            if (error != null) {
                _controlTypeState.value = ControlTypeState.Error(error.message ?: "Error loading controls")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val controlsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(AllergenControl::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                // Filtramos los controles que están realmente activos
                val reallyActiveControls = controlsList.filter { it.isCurrentlyActive() }
                _activeControls.value = reallyActiveControls.sortedByDescending { it.createdAt }
            }
        }
    }

    fun addControl(
        profileId: String,
        controlType: ControlType,
        allergenId: String,
        startDate: Date,
        endDate: Date,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                _controlTypeState.value = ControlTypeState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val controlData = hashMapOf(
                    "userId" to currentUserId,
                    "profileId" to profileId,
                    "controlType" to controlType.name,
                    "allergenId" to allergenId,
                    "startDate" to Timestamp(startDate),
                    "endDate" to Timestamp(endDate),
                    "notes" to notes,
                    "isActive" to true,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("allergen_controls")
                    .add(controlData)
                    .addOnSuccessListener {
                        _controlTypeState.value = ControlTypeState.Success.Save
                    }
                    .addOnFailureListener { e ->
                        _controlTypeState.value = ControlTypeState.Error(e.message ?: "Error adding control")
                    }
            } catch (e: Exception) {
                _controlTypeState.value = ControlTypeState.Error(e.message ?: "Error adding control")
            }
        }
    }

    fun updateProfileFilter(profileId: String?) {
        controlsListener?.remove()
        setupControlsListener(profileId)
    }

    override fun onCleared() {
        super.onCleared()
        controlsListener?.remove()
    }

    suspend fun searchByDateRange(startDate: Date, endDate: Date): List<AllergenControl> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        return try {
            Log.d("ControlTypeViewModel", "Buscando controles entre ${startDate} y ${endDate}")
            val snapshot = firestore.collection("allergen_controls")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            Log.d("ControlTypeViewModel", "Controles encontrados: ${snapshot.documents.size}")

            val controls = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(AllergenControl::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }

            // Filtramos los controles que se solapan con el rango de fechas seleccionado
            val filteredControls = controls.filter { control ->
                // Un control está dentro del rango si:
                // Su fecha de inicio está dentro del rango O
                // Su fecha de fin está dentro del rango O
                // El rango está contenido dentro del periodo del control
                (control.startDateAsDate >= startDate && control.startDateAsDate <= endDate) ||
                        (control.endDateAsDate >= startDate && control.endDateAsDate <= endDate) ||
                        (control.startDateAsDate <= startDate && control.endDateAsDate >= endDate)
            }

            Log.d("ControlTypeViewModel", "Controles filtrados por fecha: ${filteredControls.size}")
            filteredControls.forEach { control ->
                Log.d("ControlTypeViewModel", """
                Control encontrado:
                ID: ${control.id}
                Inicio: ${control.startDateAsDate}
                Fin: ${control.endDateAsDate}
                Activo: ${control.isActive}
            """.trimIndent())
            }

            filteredControls.sortedByDescending { it.startDate.seconds }
        } catch (e: Exception) {
            Log.e("ControlTypeViewModel", "Error buscando controles", e)
            emptyList()
        }
    }
}