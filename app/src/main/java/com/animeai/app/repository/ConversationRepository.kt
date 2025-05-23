package com.animeai.app.repository

import android.util.Log
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.animeai.app.engine.VoiceSyncEngine
import com.animeai.app.model.*
import com.animeai.app.service.ReplitService
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

@Singleton
class ConversationRepository @Inject constructor(
    private val openAI: OpenAI,
    private val voiceSyncEngine: VoiceSyncEngine,
    private val characterRepository: CharacterRepository,
    private val replitService: ReplitService,
    private val userRepository: UserRepository
) {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    // Store recent messages for context window
    private val conversationHistory = mutableListOf<ChatMessage>()
    
    /**
     * Processes a user message and generates an AI response
     */
    suspend fun processUserMessage(messageText: String): CharacterResponse {
        return withContext(Dispatchers.IO) {
            try {
                _isProcessing.value = true
                
                // Get current character
                val character = characterRepository.currentCharacter.value
                    ?: throw IllegalStateException("No character available")
                
                // Add user message to the conversation
                val userMessage = Message(
                    id = UUID.randomUUID().toString(),
                    content = messageText,
                    role = Role.User
                )
                addMessage(userMessage)
                
                // Convert to OpenAI chat message format
                val userChatMessage = ChatMessage(
                    role = ChatRole.User,
                    content = messageText
                )
                conversationHistory.add(userChatMessage)
                
                // Trim history to avoid token limits
                if (conversationHistory.size > 20) {
                    conversationHistory.removeAt(0)
                }
                
                // Get character persona
                val persona = character.persona ?: throw IllegalStateException("Character has no persona")
                
                // Create system prompt with character persona
                val systemMessage = ChatMessage(
                    role = ChatRole.System,
                    content = """
                        You are ${persona.name}, an anime character with these traits:
                        - Personality: ${persona.personality}
                        - Speech style: ${persona.speechStyle}
                        - Background: ${persona.backgroundStory}
                        - Interests: ${persona.interests.joinToString(", ")}
                        
                        Respond in character, maintaining consistency.
                        Use appropriate Japanese honorifics if needed.
                        Keep responses relatively concise (under 100 words).
                        Express emotions naturally in your response.
                    """.trimIndent()
                )
                
                // Create chat completion request
                val request = ChatCompletionRequest(
                    model = ModelId("gpt-4.1"),
                    messages = listOf(systemMessage) + conversationHistory,
                    temperature = 0.8,
                    maxTokens = 300
                )
                
                // Track usage
                userRepository.trackUsage(TextGeneration(messageText.length / 4)) // Approximate tokens
                
                // Get completion from OpenAI
                val completion = openAI.chatCompletion(request)
                val responseContent = completion.choices.first().message.content
                    ?: throw IllegalStateException("Empty response from AI")
                
                // Track response tokens
                userRepository.trackUsage(TextGeneration(responseContent.length / 4))
                
                // Add AI message to conversation history
                val assistantChatMessage = ChatMessage(
                    role = ChatRole.Assistant,
                    content = responseContent
                )
                conversationHistory.add(assistantChatMessage)
                
                // Analyze emotion from response
                val emotion = analyzeResponseEmotion(responseContent)
                
                // Generate speech audio
                val audioData = voiceSyncEngine.generateSpeech(
                    responseContent,
                    persona.voiceSettings
                )
                userRepository.trackUsage(AudioProcessing(audioData.size / 16000f)) // Approximate seconds
                
                // Create AI message
                val aiMessage = Message(
                    id = UUID.randomUUID().toString(),
                    content = responseContent,
                    role = Role.Assistant,
                    emotion = emotion
                )
                addMessage(aiMessage)
                
                // Ensure we have the expression for the emotion
                characterRepository.generateExpression(emotion)
                
                // Create and return character response
                CharacterResponse(
                    text = responseContent,
                    audio = audioData,
                    emotion = emotion
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error processing message", e)
                throw e
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * Process voice recording and send as a message
     */
    suspend fun processVoiceRecording(audioData: ByteArray): String {
        return withContext(Dispatchers.IO) {
            try {
                // Track usage
                userRepository.trackUsage(AudioProcessing(audioData.size / 16000f))
                
                // Transcribe audio to text
                val transcribedText = voiceSyncEngine.transcribeAudio(audioData)
                
                transcribedText
            } catch (e: Exception) {
                Log.e(TAG, "Error processing voice recording", e)
                throw e
            }
        }
    }
    
    /**
     * Analyze response text to determine the emotion
     */
    private suspend fun analyzeResponseEmotion(text: String): Emotion {
        return withContext(Dispatchers.Default) {
            try {
                // Simple approach to emotion detection
                val lowerText = text.lowercase()
                
                when {
                    lowerText.contains("laugh") || lowerText.contains("haha") || 
                    lowerText.contains("smile") || lowerText.contains("joy") || 
                    lowerText.contains("happy") -> Emotion.HAPPY
                    
                    lowerText.contains("sad") || lowerText.contains("sorry") || 
                    lowerText.contains("upset") || lowerText.contains("cry") -> Emotion.SAD
                    
                    lowerText.contains("angry") || lowerText.contains("mad") || 
                    lowerText.contains("furious") -> Emotion.ANGRY
                    
                    lowerText.contains("surprise") || lowerText.contains("shock") || 
                    lowerText.contains("wow") || lowerText.contains("!?") -> Emotion.SURPRISED
                    
                    lowerText.contains("blush") || lowerText.contains("embarrass") || 
                    lowerText.contains("shy") -> Emotion.EMBARRASSED
                    
                    else -> Emotion.NEUTRAL
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing emotion", e)
                Emotion.NEUTRAL
            }
        }
    }
    
    /**
     * Add a message to the conversation
     */
    private fun addMessage(message: Message) {
        val updatedMessages = _messages.value.toMutableList().apply {
            add(message)
        }
        _messages.value = updatedMessages
    }
    
    /**
     * Load conversation history for a character
     */
    suspend fun loadConversationHistory(characterId: String) {
        withContext(Dispatchers.IO) {
            try {
                val history = replitService.getConversationHistory(characterId)
                _messages.value = history
                
                // Reset OpenAI conversation history
                conversationHistory.clear()
                
                // Convert to OpenAI format for recent messages (limit to last 10)
                history.takeLast(10).forEach { message ->
                    val role = when (message.role) {
                        Role.User -> ChatRole.User
                        Role.Assistant -> ChatRole.Assistant
                    }
                    conversationHistory.add(ChatMessage(role = role, content = message.content))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading conversation history", e)
                // Continue with empty history if loading fails
                _messages.value = emptyList()
                conversationHistory.clear()
            }
        }
    }
    
    /**
     * Clear conversation history
     */
    fun clearConversation() {
        _messages.value = emptyList()
        conversationHistory.clear()
    }
    
    companion object {
        private const val TAG = "ConversationRepository"
    }
}
