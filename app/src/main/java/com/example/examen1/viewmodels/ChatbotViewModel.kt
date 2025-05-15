package com.example.examen1.viewmodels

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.examen1.BuildConfig
import com.example.examen1.utils.preferences.PreferencesManager
import com.google.ai.client.generativeai.type.TextPart
import com.itextpdf.kernel.pdf.PdfName.App

class ChatbotViewModel(private val context: Context) : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<UiChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()
    private val _isFirstTime = MutableStateFlow(true)
    val isFirstTime = _isFirstTime.asStateFlow()

    companion object {
        private const val INITIAL_MESSAGE  = "Hola! Soy tu asistente IA especializado en alergias. ¿En qué puedo ayudarte hoy?"
        private const val TAG = "ChatbotViewModel"
        private const val MODEL_NAME = "gemini-2.0-flash"
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )
    private val chat = generativeModel.startChat(
        history = listOf(
            content("user") {
                text("Soy un asistente especializado en alergias alimentarias. Puedo ayudarte a entender mejor las alergias, sus síntomas y cómo registrar la información importante. Recuerda que mi consejo es solo informativo y no sustituye la opinión médica profesional.")
            }
        )
    )

    init {
        // Verificar si ya se aceptaron los términos anteriormente
        _isFirstTime.value = !PreferencesManager.isChatbotTermsAccepted(context)

        // Si no es la primera vez, enviar mensaje inicial
        if (!_isFirstTime.value) {
            sendInitialMessage()
        }
        // Verificar que el modelo y el chat se hayan inicializado correctamente
        if (generativeModel == null || chat == null) {
            Log.e(TAG, "Error: No se pudo inicializar el modelo o el chat")
        }
    }

    fun acceptTerms() {
        _isFirstTime.update { false }
        // Guardar en SharedPreferences
        PreferencesManager.setChatbotTermsAccepted(context)
        sendInitialMessage()
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            // Añadir mensaje del usuario
            _chatMessages.update { it + UiChatMessage(true, userMessage) }

            try {
                Log.d(TAG, "Enviando mensaje: $userMessage")
                val response = chat.sendMessage(userMessage)
                Log.d(TAG, "Respuesta recibida: ${response.text}")
                // Filtrar la respuesta del chatbot
                response.text?.let { text ->
                    if (isMessageAboutFoodAllergy(text)) {
                        _chatMessages.update { it + UiChatMessage(false, text) }
                    } else {
                        _chatMessages.update {
                            it + UiChatMessage(false, "Lo siento, esta pregunta no está relacionada con alergias alimentarias. Si tienes preguntas relacionadas sobre alergias alimentarias o sintomas no dudes en preguntarme.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar mensaje: ${e.message}", e)
                _chatMessages.update {
                    it + UiChatMessage(
                        false,
                        "Lo siento, hubo un error. Por favor, intenta de nuevo."
                    )
                }
            }
        }
    }

    fun sendInitialMessage() {
        viewModelScope.launch {
            _chatMessages.update {
                it + UiChatMessage(
                    isUser = false,
                    text = INITIAL_MESSAGE
                )
            }
        }
    }

    private fun isMessageAboutFoodAllergy(message: String): Boolean {
        val keywords = listOf(
            "alergia",
            "alimento",
            "alergia alimentaria",
            "síntomas",
            "síntoma",
            "reacción",
            "reaccion",
            "deposiciones",
            "alimentos",
            "comida",
            "comer",
            "bebé",
            "bebe",
            "niño",
            "niña",
            "intolerancia",
            "proteína",
            "proteina",
            "leche",
            "huevo",
            "frutos secos",
            "picor",
            "picazón",
            "urticaria",
            "ronchas",
            "digestión",
            "digestion",
            "dieta",
            "médico",
            "medico",
            "pediatra",
            "alergólogo",
            "alergologo"
        )

        return keywords.any { message.contains(it, ignoreCase = true) }
    }

    private val initialAIMessage = "Hola! Soy tu asistente IA especializado en alergias. ¿En qué puedo ayudarte hoy?"
}

data class UiChatMessage(
    val isUser: Boolean,
    val text: String?, // Hacer nullable
    val timestamp: Long = System.currentTimeMillis()
) {
    val messageText: String
        get() = text ?: "Sin mensaje" // Proporcionar un valor por defecto
}