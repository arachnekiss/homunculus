package com.animeai.app.repository

import android.util.Log
import com.animeai.app.model.UsageAction
import com.animeai.app.model.TextGeneration
import com.animeai.app.model.ImageGeneration
import com.animeai.app.model.AudioProcessing
import com.animeai.app.service.ReplitService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

@Singleton
class UserRepository @Inject constructor(
    private val replitService: ReplitService
) {
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId
    
    private val _credits = MutableStateFlow(0f)
    val credits: StateFlow<Float> = _credits
    
    private var textTokensUsed = 0
    private var imageGenerationsUsed = 0
    private var audioMinutesUsed = 0f
    
    /**
     * Initialize user data
     */
    suspend fun initUser(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                _userId.value = userId
                
                // Load user data from Replit
                val userCredits = replitService.getUserCredits(userId)
                _credits.value = userCredits
                
                Log.d(TAG, "User initialized: $userId, credits: $userCredits")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing user", e)
                // If we fail to load from remote, start with default values
                _credits.value = DEFAULT_CREDITS
            }
        }
    }
    
    /**
     * Track usage of AI features
     */
    suspend fun trackUsage(action: UsageAction) {
        withContext(Dispatchers.IO) {
            try {
                val userId = _userId.value ?: throw IllegalStateException("User not initialized")
                
                // Update local usage counters
                when (action) {
                    is TextGeneration -> {
                        textTokensUsed += action.tokens
                        _credits.value -= action.tokens * TOKEN_COST
                    }
                    is ImageGeneration -> {
                        imageGenerationsUsed += action.count
                        _credits.value -= action.count * IMAGE_COST
                    }
                    is AudioProcessing -> {
                        audioMinutesUsed += action.duration
                        _credits.value -= action.duration * AUDIO_COST
                    }
                }
                
                // Sync with Replit
                replitService.trackUsage(
                    userId,
                    textTokensUsed,
                    imageGenerationsUsed,
                    audioMinutesUsed
                )
                replitService.updateUserCredits(userId, _credits.value)
                
                Log.d(TAG, "Usage tracked: $action, remaining credits: ${_credits.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error tracking usage", e)
                // Continue even if tracking fails
            }
        }
    }
    
    /**
     * Add credits to user account
     */
    suspend fun addCredits(amount: Float) {
        withContext(Dispatchers.IO) {
            try {
                val userId = _userId.value ?: throw IllegalStateException("User not initialized")
                
                _credits.value += amount
                replitService.updateUserCredits(userId, _credits.value)
                
                Log.d(TAG, "Added $amount credits, new total: ${_credits.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding credits", e)
                throw e
            }
        }
    }
    
    /**
     * Check if user has sufficient credits for an operation
     */
    fun hasEnoughCredits(requiredAmount: Float): Boolean {
        return _credits.value >= requiredAmount
    }
    
    companion object {
        private const val TAG = "UserRepository"
        
        // Default values
        private const val DEFAULT_CREDITS = 100f
        
        // Cost rates (credits per unit)
        private const val TOKEN_COST = 0.00001f  // Per token
        private const val IMAGE_COST = 0.02f     // Per image
        private const val AUDIO_COST = 0.006f    // Per minute
    }
}
