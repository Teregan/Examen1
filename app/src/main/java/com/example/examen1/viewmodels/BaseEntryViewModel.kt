package com.example.examen1.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

abstract class BaseEntryViewModel : ViewModel() {
    protected var listener: ListenerRegistration? = null
    protected val auth = FirebaseAuth.getInstance()

    fun cleanup() {
        listener?.remove()
        listener = null
    }

    protected fun setupListener(collection: String, profileId: String?, onSuccess: (QuerySnapshot) -> Unit) {
        // Remover listener existente
        cleanup()

        // Verificar autenticación
        val currentUser = auth.currentUser
        if (currentUser == null || profileId == null) {
            return
        }

        try {
            listener = FirebaseFirestore.getInstance()
                .collection(collection)
                .whereEqualTo("profileId", profileId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("BaseEntryViewModel", "Error loading entries", error)
                        return@addSnapshotListener
                    }

                    snapshot?.let { onSuccess(it) }
                }
        } catch (e: Exception) {
            Log.e("BaseEntryViewModel", "Error setting up listener", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}
