package com.example.examen1.models

import com.google.firebase.Timestamp
import java.util.Date

enum class StoolType {
    LIQUID,
    HARD,
    PELLETS,
    NORMAL;

    val displayName: String
        get() = when (this) {
            LIQUID -> "Líquida"
            HARD -> "Dura"
            PELLETS -> "Pelotitas"
            NORMAL -> "Normal"
        }
}

enum class StoolColor {
    BROWN,
    BLACK,
    GREEN,
    YELLOW,
    RED,
    WHITE;

    val displayName: String
        get() = when (this) {
            BROWN -> "Café"
            BLACK -> "Negro"
            GREEN -> "Verde"
            YELLOW -> "Amarillo"
            RED -> "Rojo"
            WHITE -> "Blanco"
        }
}

data class StoolEntry(
    val id: String = "",
    val userId: String = "",
    val profileId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val time: String = "",
    val stoolType: StoolType = StoolType.NORMAL,
    val color: StoolColor = StoolColor.BROWN,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val imagesPaths: List<String> = emptyList()
) {
    val date: Date
        get() = timestamp.toDate()
}

sealed class StoolEntryState {
    object Initial : StoolEntryState()
    object Loading : StoolEntryState()
    sealed class Success : StoolEntryState() {
        object Save : Success()
        object Load : Success()
    }
    data class Error(val message: String) : StoolEntryState()
}