package com.example.examen1.onboarding.data

sealed class OnboardingPage {
    data class Welcome(
        val title: String,
        val description: String,
        val image: Int
    ) : OnboardingPage()

    data class Features(
        val title: String,
        val description: String,
        val image: Int
    ) : OnboardingPage()

    data class Doctor(
        val title: String,
        val description: String,
        val image: Int
    ) : OnboardingPage()
}

data class OnboardingState(
    val currentPageIndex: Int = 0,
    val onboardingComplete: Boolean = false
)