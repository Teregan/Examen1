package com.example.examen1.utils.preferences

import android.content.Context

object PreferencesManager {
    private const val PREFS_NAME = "app_preferences"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    fun setOnboardingCompleted(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun clearPreferences(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}