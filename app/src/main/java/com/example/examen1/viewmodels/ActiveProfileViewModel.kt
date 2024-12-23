package com.example.examen1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.ActiveProfileState
import com.example.examen1.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ActiveProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _activeProfileState = MutableLiveData<ActiveProfileState>(ActiveProfileState.Initial)
    val activeProfileState: LiveData<ActiveProfileState> = _activeProfileState

    // Para mantener el ID del perfil activo en SharedPreferences
    private var activeProfileId: String? = null

    fun setActiveProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                _activeProfileState.value = ActiveProfileState.Loading
                activeProfileId = profile.id
                _activeProfileState.value = ActiveProfileState.Success(profile)
                // Aquí podrías guardar el ID en SharedPreferences si quieres que persista
            } catch (e: Exception) {
                _activeProfileState.value = ActiveProfileState.Error(e.message ?: "Error setting active profile")
            }
        }
    }

    fun loadLastActiveProfile() {
        viewModelScope.launch {
            try {
                _activeProfileState.value = ActiveProfileState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                // Primero intentar cargar el último perfil activo
                activeProfileId?.let { id ->
                    val profileDoc = firestore.collection("profiles")
                        .document(id)
                        .get()
                        .await()

                    profileDoc.toObject(UserProfile::class.java)?.let { profile ->
                        if (profile.userId == currentUserId) {
                            _activeProfileState.value = ActiveProfileState.Success(profile)
                            return@launch
                        }
                    }
                }

                // Si no hay perfil activo o no se pudo cargar, cargar el primer perfil del usuario
                val profilesSnapshot = firestore.collection("profiles")
                    .whereEqualTo("userId", currentUserId)
                    .limit(1)
                    .get()
                    .await()

                if (!profilesSnapshot.isEmpty) {
                    val profile = profilesSnapshot.documents[0].toObject(UserProfile::class.java)
                    if (profile != null) {
                        activeProfileId = profilesSnapshot.documents[0].id
                        _activeProfileState.value = ActiveProfileState.Success(profile.copy(id = activeProfileId!!))
                    } else {
                        _activeProfileState.value = ActiveProfileState.Error("No profiles found")
                    }
                } else {
                    _activeProfileState.value = ActiveProfileState.Error("No profiles found")
                }
            } catch (e: Exception) {
                _activeProfileState.value = ActiveProfileState.Error(e.message ?: "Error loading active profile")
            }
        }
    }

    fun getActiveProfileId(): String? = activeProfileId
}