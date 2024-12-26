package com.example.examen1

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class AllergyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeWorkManager()
    }

    private fun initializeWorkManager() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

        try {
            WorkManager.initialize(this, config)
        } catch (e: IllegalStateException) {
            // WorkManager ya está inicializado, ignorar
        }
    }

    companion object {
        private lateinit var instance: AllergyApp

        fun getInstance(): AllergyApp {
            return instance
        }
    }
}