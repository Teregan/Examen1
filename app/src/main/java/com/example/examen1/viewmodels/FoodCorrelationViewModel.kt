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

                // Si hay un profileId, obtener el perfil para verificar si es lactante
                val selectedProfile = profileId?.let { id ->
                    profileViewModel.profiles.value?.find { it.id == id }
                }

                // Obtener el perfil de la madre si es necesario
                val motherProfile = if (selectedProfile?.profileType == ProfileType.INFANT && selectedProfile.isNursing == true) {
                    profileViewModel.profiles.value?.find { it.profileType == ProfileType.MOTHER }
                } else null

                // Obtener registros de comida según el perfil seleccionado
                var foodEntries = foodEntryViewModel.searchByDateRange(startDate, endDate)

                // Aplicar filtros según el caso
                foodEntries = when {
                    profileId == null -> foodEntries // Todos los perfiles
                    motherProfile != null -> { // Infante lactante
                        foodEntries.filter {
                            it.profileId == profileId || it.profileId == motherProfile.id
                        }
                    }
                    else -> foodEntries.filter { it.profileId == profileId }
                }

                // Filtrar síntomas y deposiciones por el perfil seleccionado
                val symptoms = symptomEntryViewModel.searchByDateRange(startDate, endDate)
                    .filter { profileId == null || it.profileId == profileId }

                val stoolEntries = stoolEntryViewModel.searchByDateRange(startDate, endDate)
                    .filter { profileId == null || it.profileId == profileId }

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

                    FoodCorrelation(
                        foodEntry = foodEntry,
                        relatedSymptoms = relatedSymptoms,
                        relatedStoolEntries = relatedStoolEntries
                    )
                }.filter { it.relatedSymptoms.isNotEmpty() || it.relatedStoolEntries.isNotEmpty() }

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