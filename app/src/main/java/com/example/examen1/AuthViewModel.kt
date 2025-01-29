package com.example.examen1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.ProfileType
import com.example.examen1.models.UserProfile
import com.example.examen1.viewmodels.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val profileViewModel: ProfileViewModel,
    private val symptomEntryViewModel: SymptomEntryViewModel,
    private val stoolEntryViewModel: StoolEntryViewModel,
    private val foodEntryViewModel: FoodEntryViewModel
) : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if(auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            profileViewModel.setupProfilesListener() // Asegurar que el listener esté configurado
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if(email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                // Asegurarnos de que el ProfileViewModel tenga tiempo de configurarse
                profileViewModel.setupProfilesListener() // Forzar la configuración del listener
                _authState.value = AuthState.Authenticated
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Something went wrong")
            }
    }

    fun signup(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Crear el usuario
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()

                // Configurar el listener de perfiles
                profileViewModel.setupProfilesListener()

                // Crear un perfil predeterminado
                val defaultProfile = UserProfile(
                    name = "Perfil Principal",
                    userId = authResult.user?.uid ?: "",
                    profileType = ProfileType.MOTHER,
                    createdAt = System.currentTimeMillis()
                )

                // Esperar a que se complete la creación del perfil
                val profileCreated = profileViewModel.addProfile(defaultProfile).await()

                if (profileCreated) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Error creating default profile")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun signout(){
        // Primero limpiar todos los listeners
        profileViewModel.cleanup()
        symptomEntryViewModel.cleanup()
        stoolEntryViewModel.cleanup()
        foodEntryViewModel.cleanup()

        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}