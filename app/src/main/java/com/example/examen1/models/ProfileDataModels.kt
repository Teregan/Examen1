package com.example.examen1.models

enum class ProfileType {
    MOTHER,
    INFANT
}

data class UserProfile(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val profileType: ProfileType = ProfileType.INFANT,
    val isInfant: Boolean = false,
    val isNursing: Boolean? = null,
    val nursingStatus: Boolean? = null, // Nuevo campo con nombre diferente
    val createdAt: Long = System.currentTimeMillis()
){
    fun getNursingState(): Boolean? {
        return if (profileType == ProfileType.INFANT) {
            nursingStatus ?: isNursing
        } else {
            null
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}