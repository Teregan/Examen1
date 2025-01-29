package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen1.models.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class FoodCorrelationViewModel(
    private val foodEntryViewModel: FoodEntryViewModel,
    private val symptomEntryViewModel: SymptomEntryViewModel,
    private val stoolEntryViewModel: StoolEntryViewModel,
    private val profileViewModel: ProfileViewModel
) : ViewModel() {

    private val _correlationState = MutableLiveData<FoodCorrelationState>(FoodCorrelationState.Initial)
    val correlationState: LiveData<FoodCorrelationState> = _correlationState

    fun analyzeCorrelations(startDate: Date, endDate: Date, profileId: String?) {
        viewModelScope.launch {
            try {
                _correlationState.value = FoodCorrelationState.Loading

                Log.d("FoodCorrelationViewModel", "Analizando correlaciones")
                Log.d("FoodCorrelationViewModel", "Fecha inicial: $startDate")
                Log.d("FoodCorrelationViewModel", "Fecha final: $endDate")
                Log.d("FoodCorrelationViewModel", "Profile ID: $profileId")

                // Obtener registros de comida
                var foodEntries = foodEntryViewModel.searchByDateRange(startDate, endDate)
                Log.d("FoodCorrelationViewModel", "Número de entradas de alimentos: ${foodEntries.size}")

                // Filtrar entradas por perfil
                foodEntries = when {
                    profileId == null -> foodEntries // Todos los perfiles
                    else -> foodEntries.filter { it.profileId == profileId }
                }
                Log.d("FoodCorrelationViewModel", "Entradas de alimentos después de filtro: ${foodEntries.size}")

                // Filtrar síntomas y deposiciones por perfil
                val symptoms = symptomEntryViewModel.searchByDateRange(startDate, endDate)
                    .filter { profileId == null || it.profileId == profileId }
                Log.d("FoodCorrelationViewModel", "Número de entradas de síntomas: ${symptoms.size}")

                val stoolEntries = stoolEntryViewModel.searchByDateRange(startDate, endDate)
                    .filter { profileId == null || it.profileId == profileId }
                Log.d("FoodCorrelationViewModel", "Número de entradas de deposiciones: ${stoolEntries.size}")

                // Analizar correlaciones
                val correlations = foodEntries.map { foodEntry ->
                    val relatedSymptoms = symptoms.filter { symptom ->
                        val daysDifference = ChronoUnit.DAYS.between(
                            foodEntry.date.toInstant(),
                            symptom.date.toInstant()
                        )
                        daysDifference in 0..5
                    }

                    val relatedStoolEntries = stoolEntries.filter { stoolEntry ->
                        val daysDifference = ChronoUnit.DAYS.between(
                            foodEntry.date.toInstant(),
                            stoolEntry.date.toInstant()
                        )
                        daysDifference in 0..2 && stoolEntry.stoolType != StoolType.NORMAL
                    }

                    Log.d("FoodCorrelationViewModel", "Entrada de alimento: ${foodEntry.date}")
                    Log.d("FoodCorrelationViewModel", "Síntomas relacionados: ${relatedSymptoms.size}")
                    Log.d("FoodCorrelationViewModel", "Deposiciones relacionadas: ${relatedStoolEntries.size}")

                    FoodCorrelation(
                        foodEntry = foodEntry,
                        relatedSymptoms = relatedSymptoms,
                        relatedStoolEntries = relatedStoolEntries
                    )
                }.filter { it.relatedSymptoms.isNotEmpty() || it.relatedStoolEntries.isNotEmpty() }

                Log.d("FoodCorrelationViewModel", "Número total de correlaciones: ${correlations.size}")

                _correlationState.value = FoodCorrelationState.Success(correlations)
            } catch (e: Exception) {
                Log.e("FoodCorrelationViewModel", "Error analizando correlaciones", e)
                _correlationState.value = FoodCorrelationState.Error(
                    e.message ?: "Error al analizar correlaciones"
                )
            }
        }
    }
}