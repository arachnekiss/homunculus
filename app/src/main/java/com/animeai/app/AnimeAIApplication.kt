package com.animeai.app

import android.app.Application
import android.util.Log
import com.animeai.app.service.OpenAIService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class AnimeAIApplication : Application() {
    
    @Inject
    lateinit var openAIService: OpenAIService
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Check OpenAI API connectivity
        applicationScope.launch {
            try {
                val isApiAvailable = openAIService.isApiAvailable()
                Log.d(TAG, "OpenAI API available: $isApiAvailable")
                
                if (isApiAvailable) {
                    // Log available models for debugging
                    val models = openAIService.listModels()
                    Log.d(TAG, "Available models: ${models.joinToString()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking OpenAI API", e)
            }
        }
    }
    
    companion object {
        private const val TAG = "AnimeAIApplication"
    }
}
