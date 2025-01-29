package com.example.examen1.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.R
import com.example.examen1.models.Symptom
import com.example.examen1.models.SymptomEntry
import com.example.examen1.models.SymptomEntryState
import com.example.examen1.utils.image.ImageManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Calendar
import java.util.Date

class SymptomEntryViewModel(private val imageManager: ImageManager) : BaseEntryViewModel() {

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
    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages.asStateFlow()


    init {
        setupEntriesListener()
    }

    fun addImage(uri: Uri, entryId: String = "") {
        viewModelScope.launch {
            try {
                // Verificar límite de imágenes
                if (_selectedImages.value.size >= 3) {
                    // Opcional: Mostrar mensaje de error
                    return@launch
                }

                val imagePath = imageManager.saveImage(uri, "symptoms", entryId)
                _selectedImages.value = _selectedImages.value + imagePath
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun removeImage(imagePath: String) {
        viewModelScope.launch {
            try {
                imageManager.deleteImage(imagePath)
                _selectedImages.value = _selectedImages.value - imagePath
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun resetState() {
        _symptomEntryState.value = SymptomEntryState.Initial
        _currentEntry.value = null
    }

    fun clearSelectedImages() {
        // Eliminar físicamente los archivos de imagen
        _selectedImages.value.forEach { imagePath ->
            try {
                imageManager.deleteImage(imagePath)
            } catch (e: Exception) {
                Log.e("SymptomEntryViewModel", "Error deleting image: $imagePath", e)
            }
        }
        // Limpiar la lista de imágenes seleccionadas
        _selectedImages.value = emptyList()
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
                    "imagesPaths" to _selectedImages.value,
                    "createdAt" to System.currentTimeMillis()
                )

                // Primero, añadir la entrada
                val documentReference = firestore.collection("symptom_entries")
                    .add(entryData)
                    .await() // Usar await() para obtener la referencia del documento

                // Si hay imágenes, actualizar el documento con rutas de imágenes actualizadas
                if (_selectedImages.value.isNotEmpty()) {
                    // Actualizar las rutas de imágenes con el ID del documento
                    val updatedImagePaths = _selectedImages.value.map { imagePath ->
                        imageManager.saveImage(
                            Uri.fromFile(File(imagePath)),
                            "symptoms",
                            documentReference.id
                        )
                    }

                    // Actualizar el documento con las nuevas rutas de imágenes
                    documentReference.update("imagesPaths", updatedImagePaths)
                }

                _symptomEntryState.value = SymptomEntryState.Success.Save
                _currentEntry.value = null
                clearSelectedImages()

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
                    "profileId" to profileId,
                    "imagesPaths" to _selectedImages.value
                )

                firestore.collection("symptom_entries")
                    .document(entryId)
                    .update(updates)
                    .addOnSuccessListener {
                        _symptomEntryState.value = SymptomEntryState.Success.Save
                        _currentEntry.value = null
                        clearSelectedImages()
                    }
                    .addOnFailureListener { e ->
                        _symptomEntryState.value = SymptomEntryState.Error(e.message ?: "Error updating symptom entry")
                    }

            } catch (e: Exception) {
                _symptomEntryState.value = SymptomEntryState.Error(e.message ?: "Error updating symptom entry")
            }
        }
    }

    fun loadSymptomEntry(entryId: String) {
        viewModelScope.launch {
            try {
                _symptomEntryState.value = SymptomEntryState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val documentSnapshot = firestore.collection("symptom_entries")
                    .document(entryId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val entry = documentSnapshot.toObject(SymptomEntry::class.java)
                    if (entry?.userId == currentUserId) {
                        _currentEntry.value = entry.copy(id = documentSnapshot.id)

                        // Importante: Restablecer las imágenes seleccionadas
                        _selectedImages.value = entry.imagesPaths ?: emptyList()

                        _symptomEntryState.value = SymptomEntryState.Success.Load
                    } else {
                        _symptomEntryState.value = SymptomEntryState.Error("No tienes permiso para ver este registro")
                    }
                } else {
                    _symptomEntryState.value = SymptomEntryState.Error("Registro no encontrado")
                }
            } catch (e: Exception) {
                _symptomEntryState.value = SymptomEntryState.Error(e.message ?: "Error loading entry")
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