package com.example.examen1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.ProfileState
import com.example.examen1.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    private fun setupProfilesListener() {
        val currentUserId = auth.currentUser?.uid ?: return

        profilesListener = firestore.collection("profiles")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _profileState.value = ProfileState.Error(error.message ?: "Error loading profiles")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val profilesList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(UserProfile::class.java)?.copy(id = doc.id)
                    }
                    _profiles.value = profilesList
                    _profileState.value = ProfileState.Success
                }
            }
    }

    fun addProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading
                val currentUserId = auth.currentUser?.uid ?: throw Exception("No user logged in")

                val profileData = profile.copy(userId = currentUserId)
                firestore.collection("profiles")
                    .add(profileData)
                    .await()

                _profileState.value = ProfileState.Success
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Error adding profile")
            }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading

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

    override fun onCleared() {
        super.onCleared()
        profilesListener?.remove()
    }
}