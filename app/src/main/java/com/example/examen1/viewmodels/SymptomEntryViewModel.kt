package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.R
import com.example.examen1.models.Symptom
import com.example.examen1.models.SymptomEntry
import com.example.examen1.models.SymptomEntryState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class SymptomEntryViewModel : BaseEntryViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private var entriesListener: ListenerRegistration? = null

    private val _symptomEntryState = MutableLiveData<SymptomEntryState>()
    val symptomEntryState: LiveData<SymptomEntryState> = _symptomEntryState

    private val _symptomEntries = MutableLiveData<List<SymptomEntry>>()
    val symptomEntries: LiveData<List<SymptomEntry>> = _symptomEntries

    private val _currentEntry = MutableLiveData<SymptomEntry?>()
    val currentEntry: LiveData<SymptomEntry?> = _currentEntry

    val predefinedSymptoms = listOf(
        Symptom("headache", "Dolor de cabeza"),
        Symptom("nausea", "Náuseas"),
        Symptom("stomachache", "Malestar estomacal"),
        Symptom("diarrhea", "Diarrea"),
        Symptom("constipation", "Estreñimiento"),
        Symptom("rash", "Erupciones en la piel")
    )

    init {
        setupEntriesListener()
    }

    fun resetState() {
        _symptomEntryState.value = SymptomEntryState.Initial
        _currentEntry.value = null
    }

    private fun setupEntriesListener() {
        val currentUserId = auth.currentUser?.uid ?: return

        cleanup()

        entriesListener = firestore.collection("symptom_entries")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SymptomEntryViewModel", "Error loading entries", error)
                    _symptomEntryState.value = SymptomEntryState.Error(error.message ?: "Error loading entries")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val entriesList = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(SymptomEntry::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("SymptomEntryViewModel", "Error converting document", e)
                            null
                        }
                    }
                    _symptomEntries.value = entriesList.sortedByDescending { it.createdAt }
                }
            }
    }

    fun addSymptomEntry(
        date: Date,
        time: String,
        selectedSymptoms: List<String>,
        customSymptoms: List<String>,
        notes: String = "",
        profileId: String
    ) {
        viewModelScope.launch {
            try {
                _symptomEntryState.value = SymptomEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val entryData = hashMapOf(
                    "userId" to currentUserId,
                    "profileId" to profileId,
                    "timestamp" to Timestamp(date),
                    "time" to time,
                    "symptoms" to selectedSymptoms,
                    "customSymptoms" to customSymptoms,
                    "notes" to notes,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("symptom_entries")
                    .add(entryData)
                    .addOnSuccessListener {
                        _symptomEntryState.value = SymptomEntryState.Success.Save
                        _currentEntry.value = null
                    }
                    .addOnFailureListener { e ->
                        _symptomEntryState.value = SymptomEntryState.Error(e.message ?: "Error adding symptom entry")
                    }

            } catch (e: Exception) {
                _symptomEntryState.value = SymptomEntryState.Error(e.message ?: "Error adding symptom entry")
            }
        }
    }

    fun updateSymptomEntry(
        entryId: String,
        date: Date,
        time: String,
        selectedSymptoms: List<String>,
        customSymptoms: List<String>,
        notes: String,
        profileId: String
    ) {
        viewModelScope.launch {
            try {
                _symptomEntryState.value = SymptomEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val documentSnapshot = firestore.collection("symptom_entries")
                    .document(entryId)
                    .get()
                    .await()

                val entry = documentSnapshot.toObject(SymptomEntry::class.java)
                if (entry?.userId != currentUserId) {
                    throw Exception("No tienes permiso para modificar este registro")
                }

                val updates = hashMapOf(
                    "timestamp" to Timestamp(date),
                    "time" to time,
                    "symptoms" to selectedSymptoms,
                    "customSymptoms" to customSymptoms,
                    "notes" to notes,
                    "profileId" to profileId
                )

                firestore.collection("symptom_entries")
                    .document(entryId)
                    .update(updates)
                    .addOnSuccessListener {
                        _symptomEntryState.value = SymptomEntryState.Success.Save
                        _currentEntry.value = null
                    }
                    .addOnFailureListener { e ->
                        _symptomEntryState.value = SymptomEntryState.Error(e.message ?: "Error updating symptom entry")
                    }

            } catch (e: Exception) {
                _symptomEntryState.value = SymptomEntryState.Error(e.message ?: "Error updating symptom entry")
            }
        }
    }

    suspend fun searchByDateRange(startDate: Date, endDate: Date): List<SymptomEntry> {
        return try {
            val startOfDay = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            // Establecer el final del día para endDate
            val endOfDay = Calendar.getInstance().apply {
                time = endDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val startTimestamp = Timestamp(startOfDay)
            val endTimestamp = Timestamp(endOfDay)

            firestore.collection("symptom_entries")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThanOrEqualTo("timestamp", endTimestamp)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(SymptomEntry::class.java)?.copy(id = doc.id)
                }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("SymptomEntryViewModel", "Error searching by date range", e)
            emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        entriesListener?.remove()
    }
}