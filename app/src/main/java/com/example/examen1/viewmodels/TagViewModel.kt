package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.Tag
import com.example.examen1.models.TagState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class TagViewModel : BaseEntryViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _tagState = MutableLiveData<TagState>()
    val tagState: LiveData<TagState> = _tagState

    private val _tags = MutableLiveData<List<Tag>>()
    val tags: LiveData<List<Tag>> = _tags

    init {
        setupTagsListener()
    }

    fun refreshTags() {
        setupTagsListener()
    }
    private fun setupTagsListener() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Eliminar cualquier listener existente
        cleanup()

        listener = firestore.collection("tags")
            .whereEqualTo("userId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _tagState.value = TagState.Error(error.message ?: "Error loading tags")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val tagsList = it.documents.mapNotNull { doc ->
                        doc.toObject(Tag::class.java)?.copy(id = doc.id)
                    }
                    _tags.value = tagsList
                }
            }
    }

    fun addTag(name: String, colorHex: String) {
        viewModelScope.launch {
            try {
                _tagState.value = TagState.Loading
                val currentUser = auth.currentUser

                // Logging detallado
                Log.d("TagViewModel", "Intentando agregar tag")
                Log.d("TagViewModel", "Usuario actual: ${currentUser?.uid}")

                if (currentUser == null) {
                    Log.e("TagViewModel", "Usuario no autenticado")
                    _tagState.value = TagState.Error("No user logged in")
                    return@launch
                }

                val tag = Tag(
                    userId = currentUser.uid,
                    name = name.trim(),
                    colorHex = colorHex,
                    createdAt = System.currentTimeMillis()
                )

                // Logging de datos del tag
                Log.d("TagViewModel", "Datos del tag:")
                Log.d("TagViewModel", "- UserId: ${tag.userId}")
                Log.d("TagViewModel", "- Name: ${tag.name}")
                Log.d("TagViewModel", "- Color: ${tag.colorHex}")

                firestore.collection("tags")
                    .add(tag)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TagViewModel", "Tag agregado exitosamente")
                        Log.d("TagViewModel", "ID del documento: ${documentReference.id}")
                        _tagState.value = TagState.Success.Save
                        refreshTags()
                    }
                    .addOnFailureListener { e ->
                        Log.e("TagViewModel", "Error al agregar tag", e)
                        Log.e("TagViewModel", "Detalles del error: ${e.message}")
                        _tagState.value = TagState.Error(e.message ?: "Error adding tag")
                    }
            } catch (e: Exception) {
                Log.e("TagViewModel", "Excepción al agregar tag", e)
                Log.e("TagViewModel", "Detalles de la excepción: ${e.message}")
                _tagState.value = TagState.Error(e.message ?: "Error adding tag")
            }
        }
    }

    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            try {
                _tagState.value = TagState.Loading
                firestore.collection("tags")
                    .document(tagId)
                    .delete()
                    .addOnSuccessListener {
                        _tagState.value = TagState.Success.Save
                        refreshTags()
                    }
                    .addOnFailureListener { e ->
                        _tagState.value = TagState.Error(e.message ?: "Error deleting tag")
                    }
            } catch (e: Exception) {
                _tagState.value = TagState.Error(e.message ?: "Error deleting tag")
            }
        }
    }
}