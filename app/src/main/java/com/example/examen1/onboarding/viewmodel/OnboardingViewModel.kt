package com.example.examen1.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import com.example.examen1.R
import com.example.examen1.onboarding.data.OnboardingPage
import com.example.examen1.onboarding.data.OnboardingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OnboardingViewModel : ViewModel() {
    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState = _onboardingState.asStateFlow()



    private val pagesList = listOf(
        OnboardingPage.Welcome(
            title = "Seguimiento de Alergias",
            description = "Registra y monitorea las reacciones alérgicas de tu familia de manera simple y efectiva",
            image = R.drawable.onboarding_welcome
        ),
        OnboardingPage.Features(
            title = "Control Detallado",
            description = "Registra alimentos, síntomas y deposiciones. Observa patrones y correlaciones para tomar mejores decisiones",
            image = R.drawable.onboarding_tracking
        ),
        OnboardingPage.Features(
            title = "Calendario y Estadísticas",
            description = "Visualiza los registros en el calendario y analiza las tendencias con gráficos detallados",
            image = R.drawable.onboarding_stats
        ),
        OnboardingPage.Doctor(
            title = "Informes Médicos",
            description = "Comparte informes detallados con tu médico para mejorar el diagnóstico y tratamiento",
            image = R.drawable.onboarding_doctor
        )
    )

    private val _onboardingPages = MutableStateFlow(pagesList)
    val onboardingPages = _onboardingPages.asStateFlow()

    fun nextPage() {
        _onboardingState.update { currentState ->
            if (currentState.currentPageIndex < pagesList.size - 1) {
                currentState.copy(currentPageIndex = currentState.currentPageIndex + 1)
            } else {
                currentState.copy(onboardingComplete = true)
            }
        }
    }
    fun goToPage(index: Int) {
        if (index in 0..pagesList.lastIndex) {
            _onboardingState.update { it.copy(currentPageIndex = index) }
        }
    }

    fun skipOnboarding() {
        _onboardingState.update { it.copy(onboardingComplete = true) }
    }

}