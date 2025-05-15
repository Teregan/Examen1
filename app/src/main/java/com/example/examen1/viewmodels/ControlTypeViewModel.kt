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
import java.util.Calendar
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

        controlsListener?.remove()

        var  query = firestore.collection("allergen_controls")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isActive", true)

        if (profileId != null) {
            // Asegurarse de que el filtro por profileId se aplica correctamente
            query = query.whereEqualTo("profileId", profileId)
        }
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

                Log.d("ControlTypeViewModel", "Active controls loaded: ${controlsList.size}")
                _activeControls.value = controlsList.sortedByDescending { it.createdAt }
            }
        }
    }

    fun addControl(
        profileId: String,
        controlType: ControlType,
        allergenId: String,
        startDate: Date,
        endDate: Date,
        notes: String,
        controlDays: Int = 0
    ) {
        viewModelScope.launch {
            try {
                _controlTypeState.value = ControlTypeState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                // Actualizar controles expirados
                updateExpiredControls()



                val existingControlsQuery = firestore.collection("allergen_controls")
                    .whereEqualTo("userId", currentUserId)
                    .whereEqualTo("profileId", profileId)
                    .whereEqualTo("isActive", true)

                val existingControlsSnapshot = existingControlsQuery.get().await()
                if (existingControlsSnapshot.documents.isNotEmpty()) {
                    // Obtener el control activo y su tipo
                    val activeControl = existingControlsSnapshot.documents.first().toObject(AllergenControl::class.java)
                    throw Exception("Ya existe un control de alérgeno activo del tipo ${activeControl?.controlType?.displayName ?: ""}. Finaliza primero ese control.")
                }

                // Calcular la fecha final automáticamente si es de tipo CONTROLLED y se especificaron días
                val finalEndDate = if (controlType == ControlType.CONTROLLED && controlDays > 0) {
                    calculateEndDate(startDate, controlDays)
                } else {
                    endDate
                }

                val controlData = hashMapOf(
                    "userId" to currentUserId,
                    "profileId" to profileId,
                    "controlType" to controlType.name,
                    "allergenId" to allergenId,
                    "startDate" to Timestamp(startDate),
                    "endDate" to Timestamp(finalEndDate),
                    "notes" to notes,
                    "isActive" to true,
                    "createdAt" to System.currentTimeMillis()
                )

                // Agregar nuevo control
                firestore.collection("allergen_controls")
                    .add(controlData)
                    .addOnSuccessListener { documentReference ->
                        Log.d("ControlTypeViewModel", "Control added with ID: ${documentReference.id}")
                        _controlTypeState.value = ControlTypeState.Success.Save

                        // Forzar actualización de controles
                        setupControlsListener(profileId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ControlTypeViewModel", "Error adding control", e)
                        _controlTypeState.value = ControlTypeState.Error(e.message ?: "Error adding control")
                    }
            } catch (e: Exception) {
                Log.e("ControlTypeViewModel", "Exception in addControl", e)
                _controlTypeState.value = ControlTypeState.Error(e.message ?: "Error adding control")
            }
        }
    }

    fun updateProfileFilter(profileId: String?) {
        controlsListener?.remove()
        setupControlsListener(profileId)
    }

    private fun calculateEndDate(startDate: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.add(Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }

    fun getActiveControlsForProfile(profileId: String): List<AllergenControl> {
        updateExpiredControls()

        return activeControls.value?.filter {
            it.profileId == profileId && it.isCurrentlyActive()
        } ?: emptyList()
    }

    fun updateExpiredControls() {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val currentDate = Date()

                val expiredControlsQuery = firestore.collection("allergen_controls")
                    .whereEqualTo("userId", currentUserId)
                    .whereEqualTo("isActive", true)

                val expiredControlsSnapshot = expiredControlsQuery.get().await()

                expiredControlsSnapshot.documents.forEach { document ->
                    val control = document.toObject(AllergenControl::class.java)

                    if (control != null && isExpired(control.endDateAsDate, currentDate)) {
                        document.reference.update("isActive", false)
                        Log.d("ControlTypeViewModel", "Control ${control.id} updated to inactive")
                    }
                }

                // Forzar actualización de controles
                setupControlsListener()

            } catch (e: Exception) {
                Log.e("ControlTypeViewModel", "Error updating expired controls", e)
            }
        }
    }

    fun isAllergenUnderEliminationControl(allergenId: String, profileId: String): Boolean {
        val activeControls = getActiveControlsForProfile(profileId)
        updateExpiredControls()
        return activeControls.any {
            it.allergenId == allergenId &&
                    it.controlType == ControlType.ELIMINATION
        }
    }

    override fun onCleared() {
        super.onCleared()
        controlsListener?.remove()
    }

    public suspend fun searchByDateRange(startDate: Date, endDate: Date, profileId: String? = null): List<AllergenControl> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        updateExpiredControls()

        return try {
            var query = firestore.collection("allergen_controls")
                .whereEqualTo("userId", currentUserId)

            // Añadir filtro de perfil si se proporciona
            if (profileId != null) {
                query = query.whereEqualTo("profileId", profileId)
            }

            val snapshot = query.get().await()

            val controls = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(AllergenControl::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }

            // Filtrar por rango de fechas
            val filteredControls = controls.filter { control ->
                /*(control.startDateAsDate >= startDate && control.startDateAsDate <= endDate) ||
                        (control.endDateAsDate >= startDate && control.endDateAsDate <= endDate) ||
                        (control.startDateAsDate <= startDate && control.endDateAsDate >= endDate)*/
                (control.startDateAsDate <= endDate && control.endDateAsDate >= startDate)
            }

            filteredControls.sortedByDescending { it.startDate.seconds }
        } catch (e: Exception) {
            Log.e("ControlTypeViewModel", "Error buscando controles", e)
            emptyList()
        }
    }

    private fun isExpired(endDate: Date, currentDate: Date): Boolean {
        // Normalizar las fechas para comparar solo días
        val endCalendar = Calendar.getInstance().apply {
            time = endDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }

        val currentCalendar = Calendar.getInstance().apply {
            time = currentDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        return endCalendar.time < currentCalendar.time
    }
}