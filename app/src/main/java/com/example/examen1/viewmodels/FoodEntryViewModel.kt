package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.R
import com.example.examen1.models.Allergen
import com.example.examen1.models.FoodEntry
import com.example.examen1.models.FoodEntryState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class FoodEntryViewModel : BaseEntryViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private var entriesListener: ListenerRegistration? = null

    private val _foodEntryState = MutableLiveData<FoodEntryState>()
    val foodEntryState: LiveData<FoodEntryState> = _foodEntryState

    private val _foodEntries = MutableLiveData<List<FoodEntry>>()
    val foodEntries: LiveData<List<FoodEntry>> = _foodEntries

    private val _currentEntry = MutableLiveData<FoodEntry?>()
    val currentEntry: LiveData<FoodEntry?> = _currentEntry

    fun resetState() {
        _foodEntryState.value = FoodEntryState.Initial
        _currentEntry.value = null
    }

    val allergens = listOf(
        Allergen("milk", "Leche", R.drawable.ic_milk),
        Allergen("egg", "Huevo", R.drawable.ic_egg),
        Allergen("soy", "Soya", R.drawable.ic_soy),
        Allergen("gluten", "Gluten", R.drawable.ic_gluten),
        Allergen("fish", "Pescado", R.drawable.ic_fish),
        Allergen("shellfish", "Mariscos", R.drawable.ic_shellfish),
        Allergen("nuts", "Frutos Secos", R.drawable.ic_nuts)
    )
    init {
        setupEntriesListener()
    }

    private fun setupEntriesListener(profileId: String? = null) {
        val currentUserId = auth.currentUser?.uid ?: return

        val query = firestore.collection("food_entries")
            .whereEqualTo("userId", currentUserId)
        cleanup()

        // Si se proporciona un profileId, añadir el filtro
        entriesListener = (if (profileId != null) {
            query.whereEqualTo("profileId", profileId)
        } else {
            query
        }).addSnapshotListener { snapshot, error ->
            if (error != null) {
                _foodEntryState.value = FoodEntryState.Error(error.message ?: "Error loading entries")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val entriesList = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(FoodEntry::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                _foodEntries.value = entriesList.sortedByDescending { it.createdAt }
            }
        }
    }

    fun updateProfileFilter(profileId: String?) {
        entriesListener?.remove()
        setupEntriesListener(profileId)
    }

    fun addFoodEntry(
        date: Date,
        time: String,
        selectedAllergens: List<String>,
        selectedTags: List<String>,
        notes: String = "",
        profileId: String // Añadir profileId como parámetro
    ) {
        viewModelScope.launch {
            try {
                _foodEntryState.value = FoodEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val entryData = hashMapOf(
                    "userId" to currentUserId,
                    "profileId" to profileId, // Añadir profileId
                    "timestamp" to Timestamp(date),
                    "time" to time,
                    "allergens" to selectedAllergens,
                    "tagIds" to selectedTags,
                    "createdAt" to System.currentTimeMillis()
                )

                if (notes.isNotEmpty()) {
                    entryData["notes"] = notes
                }

                firestore.collection("food_entries")
                    .add(entryData)
                    .addOnSuccessListener {
                        _foodEntryState.value = FoodEntryState.Success.Save
                    }
                    .addOnFailureListener { e ->
                        _foodEntryState.value = FoodEntryState.Error(e.message ?: "Error adding food entry")
                    }
            } catch (e: Exception) {
                _foodEntryState.value = FoodEntryState.Error(e.message ?: "Error adding food entry")
            }
        }
    }

    fun updateFoodEntry(
        entryId: String,
        date: Date,
        time: String,
        selectedAllergens: List<String>,
        selectedTags: List<String>, // Nuevo parámetro
        notes: String,
        profileId: String
    ) {
        viewModelScope.launch {
            try {
                _foodEntryState.value = FoodEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val updates = hashMapOf(
                    "timestamp" to Timestamp(date),
                    "time" to time,
                    "allergens" to selectedAllergens,
                    "tagIds" to selectedTags, // Agregar tags
                    "notes" to notes,
                    "userId" to currentUserId,
                    "profileId" to profileId
                )

                firestore.collection("food_entries")
                    .document(entryId)
                    .update(updates)
                    .addOnSuccessListener {
                        _foodEntryState.value = FoodEntryState.Success.Save
                        _currentEntry.value = null
                    }
                    .addOnFailureListener { e ->
                        _foodEntryState.value = FoodEntryState.Error(e.message ?: "Error updating food entry")
                    }
            } catch (e: Exception) {
                _foodEntryState.value = FoodEntryState.Error(e.message ?: "Error updating food entry")
            }
        }
    }

    fun deleteFoodEntry(entryId: String) {
        viewModelScope.launch {
            try {
                _foodEntryState.value = FoodEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                // Primero verificar que el registro pertenece al usuario actual
                val documentSnapshot = firestore.collection("food_entries")
                    .document(entryId)
                    .get()
                    .await()

                val entry = documentSnapshot.toObject(FoodEntry::class.java)
                if (entry?.userId != currentUserId) {
                    throw Exception("No tienes permiso para eliminar este registro")
                }

                firestore.collection("food_entries")
                    .document(entryId)
                    .delete()
                    .addOnSuccessListener {
                        _foodEntryState.value = FoodEntryState.Success.Save
                    }
                    .addOnFailureListener { e ->
                        _foodEntryState.value = FoodEntryState.Error(e.message ?: "Error deleting food entry")
                    }

            } catch (e: Exception) {
                _foodEntryState.value = FoodEntryState.Error(e.message ?: "Error deleting food entry")
            }
        }
    }

    fun loadFoodEntry(entryId: String) {
        viewModelScope.launch {
            try {
                _foodEntryState.value = FoodEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val documentSnapshot = firestore.collection("food_entries")
                    .document(entryId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val entry = documentSnapshot.toObject(FoodEntry::class.java)
                    // Verificar que el registro pertenece al usuario actual
                    if (entry?.userId == currentUserId) {
                        _currentEntry.value = entry.copy(id = documentSnapshot.id)
                        _foodEntryState.value = FoodEntryState.Success.Load
                    } else {
                        _foodEntryState.value = FoodEntryState.Error("No tienes permiso para ver este registro")
                    }
                } else {
                    _foodEntryState.value = FoodEntryState.Error("Registro no encontrado")
                }
            } catch (e: Exception) {
                Log.e("FoodEntryViewModel", "Error loading entry", e)
                _foodEntryState.value = FoodEntryState.Error(e.message ?: "Error cargando el registro")
            }
        }
    }

    suspend fun searchByDateRange(startDate: Date, endDate: Date): List<FoodEntry> {
        return try {
            Log.d("FoodEntryViewModel", "Searching entries - Start: $startDate, End: $endDate")
            val startTimestamp = Timestamp(startDate)
            val endTimestamp = Timestamp(endDate)

            val query = firestore.collection("food_entries")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThanOrEqualTo("timestamp", endTimestamp)
                .orderBy("timestamp")

            val snapshot = query.get().await()
            Log.d("FoodEntryViewModel", "Found ${snapshot.documents.size} entries")

            snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(FoodEntry::class.java)?.copy(id = doc.id)
                }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("FoodEntryViewModel", "Error searching by date range", e)
            emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        entriesListener?.remove()
    }
}