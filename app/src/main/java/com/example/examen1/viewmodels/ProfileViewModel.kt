package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.ProfileState
import com.example.examen1.models.ProfileType
import com.example.examen1.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var profilesListener: ListenerRegistration? = null

    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState

    private val _profiles = MutableLiveData<List<UserProfile>>()
    val profiles: LiveData<List<UserProfile>> = _profiles

    init {
        setupProfilesListener()
    }

    fun hasProfiles(): Boolean {
        return profiles.value?.isNotEmpty() == true
    }

    fun setupProfilesListener() {
        val currentUserId = auth.currentUser?.uid ?: return

        Log.d("ProfileViewModel", "Setting up profiles listener for user: $currentUserId")

        _profileState.value = ProfileState.Loading

        // Limpiar el listener anterior si existe
        profilesListener?.remove()

        profilesListener = firestore.collection("profiles")
            .whereEqualTo("userId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProfileViewModel", "Error loading profiles", error)
                    _profileState.value = ProfileState.Error(error.message ?: "Error loading profiles")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val profilesList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(UserProfile::class.java)?.copy(id = doc.id)
                    }

                    _profiles.value = profilesList
                    _profileState.value = ProfileState.Success
                } else {
                    _profiles.value = emptyList()
                    _profileState.value = ProfileState.Success
                }
            }
    }

    fun addProfile(profile: UserProfile): Deferred<Boolean> = viewModelScope.async {
        try {
            _profileState.value = ProfileState.Loading
            val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

            // Crear una copia del perfil con el userId actualizado
            val profileData = profile.copy(
                userId = currentUserId,
                createdAt = System.currentTimeMillis()
            )

            // Añadir el documento y esperar a que se complete
            firestore.collection("profiles")
                .add(profileData)
                .await()

            // Actualizar el estado con éxito
            _profileState.value = ProfileState.Success
            true
        } catch (e: Exception) {
            _profileState.value = ProfileState.Error(e.message ?: "Error adding profile")
            false
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading

                // Verificar si hay registros asociados
                val hasFood = firestore.collection("food_entries")
                    .whereEqualTo("profileId", profileId)
                    .limit(1)
                    .get()
                    .await()
                    .size() > 0

                val hasSymptoms = firestore.collection("symptom_entries")
                    .whereEqualTo("profileId", profileId)
                    .limit(1)
                    .get()
                    .await()
                    .size() > 0

                val hasStools = firestore.collection("stool_entries")
                    .whereEqualTo("profileId", profileId)
                    .limit(1)
                    .get()
                    .await()
                    .size() > 0

                if (hasFood || hasSymptoms || hasStools) {
                    _profileState.value = ProfileState.Error("No se puede eliminar un perfil con registros asociados")
                    return@launch
                }

                // Si no hay registros, proceder con la eliminación
                firestore.collection("profiles")
                    .document(profileId)
                    .delete()
                    .await()

                _profileState.value = ProfileState.Success
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Error deleting profile")
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading

                profile.id?.let { id ->
                    // Crear un mapa con los campos a actualizar
                    val updates = mapOf(
                        "name" to profile.name,
                        "profileType" to profile.profileType.name,
                        "nursingStatus" to if (profile.profileType == ProfileType.INFANT) profile.isNursing else null
                    )

                    Log.d("ProfileViewModel", "Updating with: $updates")

                    // Actualizar con el nuevo campo
                    firestore.collection("profiles")
                        .document(id)
                        .update(updates)
                        .await()

                    _profileState.value = ProfileState.Success
                } ?: throw Exception("Profile ID is required for update")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile", e)
                _profileState.value = ProfileState.Error(e.message ?: "Error updating profile")
            }
        }
    }

    fun cleanup() {
        profilesListener?.remove()
        profilesListener = null
        _profiles.value = emptyList()
        _profileState.value = ProfileState.Success
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}