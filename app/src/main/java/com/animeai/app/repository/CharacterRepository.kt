package com.animeai.app.repository

import android.util.Log
import com.animeai.app.engine.AnimeCharacterEngine
import com.animeai.app.model.AnimeStyle
import com.animeai.app.model.Character
import com.animeai.app.model.CharacterPersona
import com.animeai.app.model.Emotion
import com.animeai.app.service.ReplitService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

@Singleton
class CharacterRepository @Inject constructor(
    private val animeCharacterEngine: AnimeCharacterEngine,
    private val replitService: ReplitService
) {
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters
    
    private val _currentCharacter = MutableStateFlow<Character?>(null)
    val currentCharacter: StateFlow<Character?> = _currentCharacter
    
    /**
     * Generate a new character from a prompt
     */
    suspend fun generateCharacter(
        prompt: String,
        style: AnimeStyle = AnimeStyle.MODERN
    ): Character {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Generating new character with prompt: $prompt")
                
                // Generate the character using the engine
                val character = animeCharacterEngine.generateCharacter(prompt, style)
                
                // Save to Replit storage
                replitService.saveCharacter(character)
                
                // Update the current character
                _currentCharacter.value = character
                
                // Update the characters list
                val updatedList = _characters.value.toMutableList()
                updatedList.add(character)
                _characters.value = updatedList
                
                Log.d(TAG, "Character generated with ID: ${character.id}")
                character
            } catch (e: Exception) {
                Log.e(TAG, "Error generating character", e)
                throw e
            }
        }
    }
    
    /**
     * Load user's characters from Replit
     */
    suspend fun loadUserCharacters(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading characters for user: $userId")
                val userCharacters = replitService.getUserCharacters(userId)
                _characters.value = userCharacters
                
                if (userCharacters.isNotEmpty() && _currentCharacter.value == null) {
                    _currentCharacter.value = userCharacters.first()
                }
                
                Log.d(TAG, "Loaded ${userCharacters.size} characters")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user characters", e)
                // If we can't load from remote, don't crash - just keep any existing data
            }
        }
    }
    
    /**
     * Generate expression for the current character
     */
    suspend fun generateExpression(emotion: Emotion): String? {
        return withContext(Dispatchers.IO) {
            try {
                val character = _currentCharacter.value ?: throw IllegalStateException("No character selected")
                
                if (character.hasExpression(emotion)) {
                    return@withContext character.getExpressionForEmotion(emotion)
                }
                
                Log.d(TAG, "Generating expression ${emotion.name} for character ${character.id}")
                val expressionUrl = animeCharacterEngine.generateExpression(character, emotion)
                
                // Save updated character
                replitService.saveCharacter(character)
                
                // Update the characters list
                val updatedList = _characters.value.map {
                    if (it.id == character.id) character else it
                }
                _characters.value = updatedList
                
                expressionUrl
            } catch (e: Exception) {
                Log.e(TAG, "Error generating expression", e)
                null
            }
        }
    }
    
    /**
     * Update character persona
     */
    suspend fun updateCharacterPersona(persona: CharacterPersona) {
        withContext(Dispatchers.IO) {
            try {
                val character = _currentCharacter.value ?: throw IllegalStateException("No character selected")
                
                Log.d(TAG, "Updating persona for character ${character.id}")
                character.updatePersona(persona)
                
                // Save updated character
                replitService.saveCharacter(character)
                
                // Update the characters list
                val updatedList = _characters.value.map {
                    if (it.id == character.id) character else it
                }
                _characters.value = updatedList
                
                // Update current character flow
                _currentCharacter.value = character
            } catch (e: Exception) {
                Log.e(TAG, "Error updating character persona", e)
                throw e
            }
        }
    }
    
    /**
     * Set the current active character
     */
    fun setCurrentCharacter(character: Character) {
        _currentCharacter.value = character
    }
    
    companion object {
        private const val TAG = "CharacterRepository"
    }
}
