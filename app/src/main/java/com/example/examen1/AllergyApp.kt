package com.example.examen1

import android.app.Application
import com.google.firebase.FirebaseApp

class AllergyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: IllegalStateException) {
            // Firebase ya está inicializado
        }
    }

    companion object {
        private lateinit var instance: AllergyApp

        fun getInstance(): AllergyApp {
            return instance
        }
    }
}