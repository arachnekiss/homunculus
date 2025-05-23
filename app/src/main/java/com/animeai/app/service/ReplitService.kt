package com.animeai.app.service

import android.util.Log
import com.animeai.app.model.Character
import com.animeai.app.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with Replit database and storage
 */
@Singleton
class ReplitService @Inject constructor() {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    // Base URL for Replit API
    private val baseUrl = "https://your-app.repl.co/api"
    
    /**
     * Save a character to Replit storage
     */
    suspend fun saveCharacter(character: Character) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Saving character ${character.id} to Replit")
                
                val jsonString = json.encodeToString(Character.serializer(), character)
                val requestBody = jsonString.toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url("$baseUrl/character")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to save character: ${response.code}")
                    }
                }
                
                Log.d(TAG, "Character saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving character to Replit", e)
                // In development, continue even if saving fails
            }
        }
    }
    
    /**
     * Get characters for a user
     */
    suspend fun getUserCharacters(userId: String): List<Character> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting characters for user $userId")
                
                val request = Request.Builder()
                    .url("$baseUrl/characters?userId=$userId")
                    .get()
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to get characters: ${response.code}")
                    }
                    
                    val jsonString = response.body?.string() ?: "[]"
                    json.decodeFromString<List<Character>>(jsonString)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user characters from Replit", e)
                // Return empty list on error during development
                emptyList()
            }
        }
    }
    
    /**
     * Get conversation history for a character
     */
    suspend fun getConversationHistory(characterId: String): List<Message> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting conversation history for character $characterId")
                
                val request = Request.Builder()
                    .url("$baseUrl/conversation?characterId=$characterId")
                    .get()
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to get conversation history: ${response.code}")
                    }
                    
                    val jsonString = response.body?.string() ?: "[]"
                    json.decodeFromString<List<Message>>(jsonString)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting conversation history from Replit", e)
                // Return empty list on error during development
                emptyList()
            }
        }
    }
    
    /**
     * Get user's credit balance
     */
    suspend fun getUserCredits(userId: String): Float {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting credits for user $userId")
                
                val request = Request.Builder()
                    .url("$baseUrl/credits?userId=$userId")
                    .get()
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to get credits: ${response.code}")
                    }
                    
                    val jsonString = response.body?.string() ?: "0"
                    jsonString.toFloatOrNull() ?: 0f
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user credits from Replit", e)
                // Return default credits on error during development
                100f
            }
        }
    }
    
    /**
     * Update user's credit balance
     */
    suspend fun updateUserCredits(userId: String, credits: Float) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Updating credits for user $userId to $credits")
                
                val jsonString = """{"userId": "$userId", "credits": $credits}"""
                val requestBody = jsonString.toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url("$baseUrl/credits")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to update credits: ${response.code}")
                    }
                }
                
                Log.d(TAG, "Credits updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user credits on Replit", e)
                // Continue even if updating fails during development
            }
        }
    }
    
    /**
     * Track usage statistics
     */
    suspend fun trackUsage(
        userId: String,
        textTokens: Int,
        imageGenerations: Int,
        audioMinutes: Float
    ) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Tracking usage for user $userId")
                
                val jsonString = """
                    {
                        "userId": "$userId",
                        "textTokens": $textTokens,
                        "imageGenerations": $imageGenerations,
                        "audioMinutes": $audioMinutes
                    }
                """.trimIndent()
                val requestBody = jsonString.toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url("$baseUrl/usage")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Failed to track usage: ${response.code}")
                    }
                }
                
                Log.d(TAG, "Usage tracked successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error tracking usage on Replit", e)
                // Continue even if tracking fails during development
            }
        }
    }
    
    companion object {
        private const val TAG = "ReplitService"
    }
}
