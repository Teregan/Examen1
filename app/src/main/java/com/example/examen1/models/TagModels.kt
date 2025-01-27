package com.example.examen1.models

data class Tag(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val colorHex: String = "#808080",
    val createdAt: Long = System.currentTimeMillis()
)

sealed class TagState {
    object Initial : TagState()
    object Loading : TagState()
    sealed class Success : TagState() {
        object Save : Success()
        object Load : Success()
    }
    data class Error(val message: String) : TagState()
}