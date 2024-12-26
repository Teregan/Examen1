package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class StoolEntryViewModel : BaseEntryViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private var entriesListener: ListenerRegistration? = null

    private val _stoolEntryState = MutableLiveData<StoolEntryState>()
    val stoolEntryState: LiveData<StoolEntryState> = _stoolEntryState

    private val _stoolEntries = MutableLiveData<List<StoolEntry>>()
    val stoolEntries: LiveData<List<StoolEntry>> = _stoolEntries

    private val _currentEntry = MutableLiveData<StoolEntry?>()
    val currentEntry: LiveData<StoolEntry?> = _currentEntry

    init {
        setupEntriesListener()
    }

    fun resetState() {
        _stoolEntryState.value = StoolEntryState.Initial
        _currentEntry.value = null
    }

    private fun setupEntriesListener() {
        val currentUserId = auth.currentUser?.uid ?: return

        cleanup()
        entriesListener = firestore.collection("stool_entries")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("StoolEntryViewModel", "Error loading entries", error)
                    _stoolEntryState.value = StoolEntryState.Error(error.message ?: "Error loading entries")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val entriesList = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(StoolEntry::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("StoolEntryViewModel", "Error converting document", e)
                            null
                        }
                    }
                    _stoolEntries.value = entriesList.sortedByDescending { it.createdAt }
                }
            }
    }

    fun addStoolEntry(
        date: Date,
        time: String,
        stoolType: StoolType,
        color: StoolColor,
        notes: String = "",
        profileId: String
    ) {
        viewModelScope.launch {
            try {
                _stoolEntryState.value = StoolEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val entryData = hashMapOf(
                    "userId" to currentUserId,
                    "profileId" to profileId,
                    "timestamp" to Timestamp(date),
                    "time" to time,
                    "stoolType" to stoolType.name,
                    "color" to color.name,
                    "notes" to notes,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("stool_entries")
                    .add(entryData)
                    .addOnSuccessListener {
                        _stoolEntryState.value = StoolEntryState.Success.Save
                        _currentEntry.value = null
                    }
                    .addOnFailureListener { e ->
                        _stoolEntryState.value = StoolEntryState.Error(e.message ?: "Error adding stool entry")
                    }

            } catch (e: Exception) {
                _stoolEntryState.value = StoolEntryState.Error(e.message ?: "Error adding stool entry")
            }
        }
    }

    fun loadStoolEntry(entryId: String) {
        viewModelScope.launch {
            try {
                _stoolEntryState.value = StoolEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val documentSnapshot = firestore.collection("stool_entries")
                    .document(entryId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val entry = documentSnapshot.toObject(StoolEntry::class.java)
                    if (entry?.userId == currentUserId) {
                        _currentEntry.value = entry.copy(id = documentSnapshot.id)
                        _stoolEntryState.value = StoolEntryState.Success.Load
                    } else {
                        _stoolEntryState.value = StoolEntryState.Error("No tienes permiso para ver este registro")
                    }
                } else {
                    _stoolEntryState.value = StoolEntryState.Error("Registro no encontrado")
                }
            } catch (e: Exception) {
                _stoolEntryState.value = StoolEntryState.Error(e.message ?: "Error loading entry")
            }
        }
    }

    fun updateStoolEntry(
        entryId: String,
        date: Date,
        time: String,
        stoolType: StoolType,
        color: StoolColor,
        notes: String,
        profileId: String
    ) {
        viewModelScope.launch {
            try {
                _stoolEntryState.value = StoolEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                // Verificar permisos primero
                val documentSnapshot = firestore.collection("stool_entries")
                    .document(entryId)
                    .get()
                    .await()

                val entry = documentSnapshot.toObject(StoolEntry::class.java)
                if (entry?.userId != currentUserId) {
                    throw Exception("No tienes permiso para modificar este registro")
                }

                // Crear los datos actualizados usando el mismo formato que en add
                val entryData = hashMapOf(
                    "userId" to currentUserId,
                    "profileId" to profileId,
                    "timestamp" to Timestamp(date),
                    "time" to time,
                    "stoolType" to stoolType.name,
                    "color" to color.name,
                    "notes" to notes,
                    "createdAt" to (entry.createdAt ?: System.currentTimeMillis()) // Mantener la fecha original o crear una nueva
                )

                // Usar set en lugar de update
                firestore.collection("stool_entries")
                    .document(entryId)
                    .set(entryData)
                    .addOnSuccessListener {
                        _stoolEntryState.value = StoolEntryState.Success.Save
                        _currentEntry.value = null
                    }
                    .addOnFailureListener { e ->
                        _stoolEntryState.value = StoolEntryState.Error(e.message ?: "Error updating stool entry")
                    }

            } catch (e: Exception) {
                _stoolEntryState.value = StoolEntryState.Error(e.message ?: "Error updating stool entry")
            }
        }
    }

    suspend fun searchByDateRange(startDate: Date, endDate: Date): List<StoolEntry> {
        return try {
            val startTimestamp = Timestamp(startDate)
            val endTimestamp = Timestamp(endDate)

            firestore.collection("stool_entries")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThanOrEqualTo("timestamp", endTimestamp)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(StoolEntry::class.java)?.copy(id = doc.id)
                }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("StoolEntryViewModel", "Error searching by date range", e)
            emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        entriesListener?.remove()
    }
}