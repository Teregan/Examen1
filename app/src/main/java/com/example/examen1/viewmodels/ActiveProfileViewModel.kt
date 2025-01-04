package com.example.examen1.viewmodels

import android.content.Context
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

class ActiveProfileViewModel(private val context: Context) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _activeProfileState = MutableLiveData<ActiveProfileState>(ActiveProfileState.Initial)
    val activeProfileState: LiveData<ActiveProfileState> = _activeProfileState

    fun setActiveProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                _activeProfileState.value = ActiveProfileState.Loading
                // Guardar en SharedPreferences
                saveLastActiveProfile(profile.id)
                _activeProfileState.value = ActiveProfileState.Success(profile)
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

                // Intentar cargar el último perfil activo desde SharedPreferences
                val lastActiveProfileId = getLastActiveProfileId()

                if (lastActiveProfileId != null) {
                    val profileDoc = firestore.collection("profiles")
                        .document(lastActiveProfileId)
                        .get()
                        .await()

                    if (profileDoc.exists()) {
                        val profile = profileDoc.toObject(UserProfile::class.java)
                        if (profile != null && profile.userId == currentUserId) {
                            _activeProfileState.value = ActiveProfileState.Success(profile.copy(id = lastActiveProfileId))
                            return@launch
                        }
                    }
                }

                // Si no hay perfil activo guardado o no se pudo cargar, intentar con el primer perfil
                val profilesSnapshot = firestore.collection("profiles")
                    .whereEqualTo("userId", currentUserId)
                    .limit(1)
                    .get()
                    .await()

                if (!profilesSnapshot.isEmpty) {
                    val doc = profilesSnapshot.documents[0]
                    val profile = doc.toObject(UserProfile::class.java)
                    if (profile != null) {
                        saveLastActiveProfile(doc.id)
                        _activeProfileState.value = ActiveProfileState.Success(profile.copy(id = doc.id))
                    } else {
                        _activeProfileState.value = ActiveProfileState.Initial
                    }
                } else {
                    _activeProfileState.value = ActiveProfileState.Initial
                }
            } catch (e: Exception) {
                _activeProfileState.value = ActiveProfileState.Error(e.message ?: "Error loading active profile")
            }
        }
    }

    private fun saveLastActiveProfile(profileId: String?) {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("last_active_profile", profileId)
            .apply()
    }

    private fun getLastActiveProfileId(): String? {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("last_active_profile", null)
    }

    fun getActiveProfileId(): String? = getLastActiveProfileId()

    fun cleanup() {
        saveLastActiveProfile(null)
        _activeProfileState.value = ActiveProfileState.Initial
    }
}