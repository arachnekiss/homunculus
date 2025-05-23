package com.animeai.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.AnimeStyle
import com.animeai.app.model.Personality
import com.animeai.app.model.SpeechStyle
import com.animeai.app.model.UserPreferences
import com.animeai.app.service.CharacterGenerationService
import com.animeai.app.service.CreditService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for character creation screen
 */
@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    private val characterGenerationService: CharacterGenerationService,
    private val creditService: CreditService
) : ViewModel() {
    
    private val TAG = "CharacterCreationVM"
    
    // Generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()
    
    // Credits state
    private val _remainingCredits = MutableStateFlow(100f)
    val remainingCredits: StateFlow<Float> = _remainingCredits.asStateFlow()
    
    // Cost of generation
    private val _generationCost = MutableStateFlow(2.0f)
    val generationCost: StateFlow<Float> = _generationCost.asStateFlow()
    
    // Generated character
    private val _generatedCharacter = MutableStateFlow<AnimeCharacter?>(null)
    val generatedCharacter: StateFlow<AnimeCharacter?> = _generatedCharacter.asStateFlow()
    
    // User ID (would be retrieved from auth in a real app)
    private val userId = "user_${System.currentTimeMillis()}"
    
    init {
        // Load initial credits
        loadCredits()
    }
    
    /**
     * Load user credits
     */
    private fun loadCredits() {
        viewModelScope.launch {
            try {
                val credits = creditService.getUserCredits(userId)
                _remainingCredits.value = credits
            } catch (e: Exception) {
                Log.e(TAG, "Error loading credits", e)
            }
        }
    }
    
    /**
     * Generate a new character based on user preferences
     */
    fun generateCharacter(
        prompt: String,
        style: AnimeStyle,
        personality: Personality,
        speechStyle: SpeechStyle
    ) {
        if (_isGenerating.value) return
        
        // Check if enough credits
        if (_remainingCredits.value < _generationCost.value) {
            Log.e(TAG, "Not enough credits to generate character")
            return
        }
        
        _isGenerating.value = true
        
        viewModelScope.launch {
            try {
                // Create user preferences
                val preferences = UserPreferences(
                    preferredStyle = style,
                    preferredPersonality = personality,
                    preferredSpeechStyle = speechStyle,
                    customPrompt = prompt
                )
                
                // Generate character
                val character = characterGenerationService.generateCharacter(
                    prompt = prompt,
                    style = style
                )
                
                // Deduct credits
                deductCredits(_generationCost.value)
                
                // Store generated character
                _generatedCharacter.value = character
                
                Log.d(TAG, "Character generated successfully: ${character.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating character", e)
            } finally {
                _isGenerating.value = false
            }
        }
    }
    
    /**
     * Get the generated character
     */
    fun getGeneratedCharacter(): AnimeCharacter? {
        return _generatedCharacter.value
    }
    
    /**
     * Deduct credits for character generation
     */
    private suspend fun deductCredits(amount: Float) {
        try {
            // In a real app, this would call the credit service
            val currentCredits = _remainingCredits.value
            _remainingCredits.value = (currentCredits - amount).coerceAtLeast(0f)
            
            // Update credits in service
            creditService.updateUserCredits(userId, _remainingCredits.value)
        } catch (e: Exception) {
            Log.e(TAG, "Error deducting credits", e)
        }
    }
}