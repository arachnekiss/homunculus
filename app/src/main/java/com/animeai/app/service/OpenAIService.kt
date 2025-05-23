package com.animeai.app.service

import android.util.Log
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class OpenAIService @Inject constructor() {
    
    // Create OpenAI client with configuration
    private val openAI = OpenAI(
        config = OpenAIConfig(
            token = System.getenv("OPENAI_API_KEY") ?: "default_key",
            logging = LoggingConfig(LogLevel.None),
            timeout = Timeout(socket = 60.seconds)
        )
    )
    
    // List available models (for debugging)
    suspend fun listModels(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                openAI.models().map { it.id }
            } catch (e: Exception) {
                Log.e(TAG, "Error listing models", e)
                emptyList()
            }
        }
    }
    
    // Get the OpenAI client instance
    fun getClient(): OpenAI {
        return openAI
    }
    
    // Check if the API is available and the key is valid
    suspend fun isApiAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Try to list models as a basic availability check
                val models = openAI.models()
                models.isNotEmpty()
            } catch (e: Exception) {
                Log.e(TAG, "OpenAI API is not available", e)
                false
            }
        }
    }

    // Get the appropriate model ID based on model name
    fun getModelId(modelName: String): ModelId {
        return ModelId(modelName)
    }
    
    companion object {
        private const val TAG = "OpenAIService"
    }
}
