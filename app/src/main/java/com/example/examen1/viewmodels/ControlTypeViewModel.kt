package com.example.examen1.viewmodels

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
}