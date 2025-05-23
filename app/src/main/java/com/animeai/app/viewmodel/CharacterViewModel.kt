package com.animeai.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animeai.app.model.AnimeStyle
import com.animeai.app.model.Character
import com.animeai.app.model.CharacterPersona
import com.animeai.app.model.Emotion
import com.animeai.app.repository.CharacterRepository
import com.animeai.app.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    // Character state
    val currentCharacter = characterRepository.currentCharacter
    
    // Character emotion state
    private val _currentEmotion = MutableStateFlow(Emotion.NEUTRAL)
    val currentEmotion: StateFlow<Emotion> = _currentEmotion.asStateFlow()
    
    // Animation state
    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()
    
    // User credits
    val userCredits = userRepository.credits
    
    init {
        // Initialize with user ID (in a real app, this would come from authentication)
        viewModelScope.launch {
            try {
                userRepository.initUser("test_user")
                
                // Load user's characters
                characterRepository.loadUserCharacters("test_user")
                
                // If no characters exist, create a default one
                if (characterRepository.characters.value.isEmpty()) {
                    generateNewCharacter()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during initialization", e)
            }
        }
    }
    
    /**
     * Generate a new character
     */
    fun generateNewCharacter() {
        viewModelScope.launch {
            try {
                // Check if user has enough credits
                if (!userRepository.hasEnoughCredits(2.0f)) {
                    Log.d(TAG, "Insufficient credits to generate character")
                    return@launch
                }
                
                // Default prompts for character generation
                val prompts = listOf(
                    "Female high school student with brown hair and blue eyes",
                    "Male mage with white hair and purple eyes",
                    "Female warrior with red hair and green eyes",
                    "Male detective with black hair and glasses"
                )
                val randomPrompt = prompts.random()
                
                // Generate character
                val character = characterRepository.generateCharacter(
                    prompt = randomPrompt,
                    style = AnimeStyle.MODERN
                )
                
                // Track image generation usage
                userRepository.trackUsage(com.animeai.app.model.ImageGeneration())
                
                // Set as current character
                characterRepository.setCurrentCharacter(character)
                
                // Reset emotion
                _currentEmotion.value = Emotion.NEUTRAL
                
                Log.d(TAG, "New character generated: ${character.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating character", e)
            }
        }
    }
    
    /**
     * Update character persona
     */
    fun updateCharacterPersona(persona: CharacterPersona) {
        viewModelScope.launch {
            try {
                characterRepository.updateCharacterPersona(persona)
                Log.d(TAG, "Character persona updated")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating character persona", e)
            }
        }
    }
    
    /**
     * Update character emotion
     */
    fun updateCharacterEmotion(emotion: Emotion) {
        viewModelScope.launch {
            try {
                // Don't change if same emotion
                if (_currentEmotion.value == emotion) return@launch
                
                val character = currentCharacter.value ?: return@launch
                
                // Start animation
                _isAnimating.value = true
                
                // Generate the expression if we don't have it yet
                if (!character.hasExpression(emotion)) {
                    characterRepository.generateExpression(emotion)
                }
                
                // Update current emotion
                _currentEmotion.value = emotion
                
                // End animation after delay
                kotlinx.coroutines.delay(500)
                _isAnimating.value = false
                
                Log.d(TAG, "Character emotion updated to ${emotion.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating character emotion", e)
                _isAnimating.value = false
            }
        }
    }
    
    /**
     * Handle character tapped event
     */
    fun onCharacterTapped() {
        viewModelScope.launch {
            // Cycle through some emotions when tapped
            val emotions = listOf(
                Emotion.HAPPY,
                Emotion.SURPRISED,
                Emotion.EMBARRASSED
            )
            
            val nextEmotion = if (_currentEmotion.value == Emotion.NEUTRAL) {
                emotions.random()
            } else {
                Emotion.NEUTRAL
            }
            
            updateCharacterEmotion(nextEmotion)
        }
    }
    
    companion object {
        private const val TAG = "CharacterViewModel"
    }
}
