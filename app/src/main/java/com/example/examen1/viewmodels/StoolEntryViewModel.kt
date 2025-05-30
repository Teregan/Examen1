package com.example.examen1.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.*
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

class StoolEntryViewModel(private val imageManager: ImageManager) : BaseEntryViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private var entriesListener: ListenerRegistration? = null

    private val _stoolEntryState = MutableLiveData<StoolEntryState>()
    val stoolEntryState: LiveData<StoolEntryState> = _stoolEntryState

    private val _stoolEntries = MutableLiveData<List<StoolEntry>>()
    val stoolEntries: LiveData<List<StoolEntry>> = _stoolEntries

    private val _currentEntry = MutableLiveData<StoolEntry?>()
    val currentEntry: LiveData<StoolEntry?> = _currentEntry

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

                val imagePath = imageManager.saveImage(uri, "stools", entryId)
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
        _stoolEntryState.value = StoolEntryState.Initial
        _currentEntry.value = null
    }

    fun clearSelectedImages() {
        // Eliminar físicamente los archivos de imagen
        _selectedImages.value.forEach { imagePath ->
            try {
                imageManager.deleteImage(imagePath)
            } catch (e: Exception) {
                Log.e("StoolEntryViewModel", "Error deleting image: $imagePath", e)
            }
        }
        // Limpiar la lista de imágenes seleccionadas
        _selectedImages.value = emptyList()
    }

    private fun setupEntriesListener() {
        val currentUserId = auth.currentUser?.uid ?: return

        cleanup()

        entriesListener = firestore.collection("stool_entries")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _stoolEntryState.value = StoolEntryState.Error(error.message ?: "Error loading entries")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val entriesList = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(StoolEntry::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
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
                    "imagesPaths" to _selectedImages.value,
                    "createdAt" to System.currentTimeMillis()
                )

                // Primero, añadir la entrada
                val documentReference = firestore.collection("stool_entries")
                    .add(entryData)
                    .await()

                // Si hay imágenes, actualizar el documento con rutas de imágenes actualizadas
                if (_selectedImages.value.isNotEmpty()) {
                    // Actualizar las rutas de imágenes con el ID del documento
                    val updatedImagePaths = _selectedImages.value.map { imagePath ->
                        imageManager.saveImage(
                            Uri.fromFile(File(imagePath)),
                            "stools",
                            documentReference.id
                        )
                    }

                    // Actualizar el documento con las nuevas rutas de imágenes
                    documentReference.update("imagesPaths", updatedImagePaths)
                }

                _stoolEntryState.value = StoolEntryState.Success.Save
                _currentEntry.value = null
                clearSelectedImages()


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

                        // Importante: Restablecer las imágenes seleccionadas
                        _selectedImages.value = entry.imagesPaths ?: emptyList()

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
                    "imagesPaths" to _selectedImages.value,
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
            val startOfDay = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time



            val endOfDay = Calendar.getInstance().apply {
                time = endDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val startTimestamp = Timestamp(startOfDay)
            val endTimestamp = Timestamp(endOfDay)

            Log.d("StoolEntryViewModel", "Buscando entradas de deposiciones")
            Log.d("StoolEntryViewModel", "Fecha inicial: $startOfDay")
            Log.d("StoolEntryViewModel", "Fecha final: $endOfDay")

            val entries = firestore.collection("stool_entries")
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

            Log.d("StoolEntryViewModel", "Número de entradas encontradas: ${entries.size}")
            entries
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