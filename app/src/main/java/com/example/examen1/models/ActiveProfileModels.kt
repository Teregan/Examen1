package com.example.examen1.models

sealed class ActiveProfileState {
    object Initial : ActiveProfileState()
    object Loading : ActiveProfileState()
    data class Success(val profile: UserProfile) : ActiveProfileState()
    data class Error(val message: String) : ActiveProfileState()
}