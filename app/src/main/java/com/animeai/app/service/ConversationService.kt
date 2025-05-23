package com.animeai.app.service

import android.util.Log
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.CharacterResponse
import com.animeai.app.model.Emotion
import com.animeai.app.model.OpenAIModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling conversations with anime characters
 */
@Singleton
class ConversationService @Inject constructor(
    private val openAI: OpenAI,
    private val voiceService: VoiceService
) {
    private val TAG = "ConversationService"
    
    // Cache of recent messages to maintain context
    private val conversationCache = mutableMapOf<String, MutableList<ChatMessage>>()
    
    /**
     * Generate a response from the character to user input
     */
    suspend fun generateResponse(
        userInput: String,
        character: AnimeCharacter,
        userId: String,
        conversationId: String
    ): CharacterResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating response to: $userInput")
            
            // Get or create conversation history
            val messagesHistory = getOrCreateConversation(conversationId)
            
            // Add user message
            val userMessage = ChatMessage(
                role = ChatRole.User,
                content = userInput
            )
            messagesHistory.add(userMessage)
            
            // Prepare persona instructions
            val persona = character.persona ?: throw IllegalStateException("Character has no persona")
            val systemPrompt = """
                You are ${persona.name}, an anime character with these traits:
                - Personality: ${persona.personality.apiValue}
                - Speech style: ${persona.speechStyle.apiValue}
                - Background: ${persona.backgroundStory}
                
                Respond in character, maintaining consistency with your persona.
                Keep responses concise (1-3 sentences).
                Use appropriate Japanese honorifics if needed.
                Include an emotion with your response from this list: neutral, happy, sad, angry, surprised, embarrassed.
                
                Format your response exactly like this:
                EMOTION: [emotion]
                RESPONSE: [your in-character response]
            """.trimIndent()
            
            // Get current history, limited to last 10 messages to save on tokens
            val recentMessages = messagesHistory.takeLast(10).toMutableList()
            
            // Add system prompt at the beginning
            recentMessages.add(0, ChatMessage(
                role = ChatRole.System,
                content = systemPrompt
            ))
            
            // Create chat completion request
            val completionRequest = ChatCompletionRequest(
                model = ModelId(OpenAIModels.GPT_4_1),
                messages = recentMessages,
                temperature = 0.8,
                maxTokens = 300
            )
            
            // Get response from OpenAI
            val completion: ChatCompletion = openAI.chatCompletion(completionRequest)
            val responseContent = completion.choices.first().message.content
                ?: throw IllegalStateException("Empty response from OpenAI")
            
            Log.d(TAG, "Generated response: $responseContent")
            
            // Parse response to extract emotion and text
            val responseData = parseResponse(responseContent)
            
            // Add assistant response to history
            messagesHistory.add(ChatMessage(
                role = ChatRole.Assistant,
                content = responseData.second
            ))
            
            // Generate audio for the response
            val audio = voiceService.textToSpeech(
                text = responseData.second,
                voiceSettings = persona.voiceSettings
            )
            
            CharacterResponse(
                text = responseData.second,
                audio = audio,
                emotion = responseData.first
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            
            // Return a fallback response
            CharacterResponse(
                text = "죄송합니다, 지금은 대답할 수 없어요...",
                emotion = Emotion.NEUTRAL
            )
        }
    }
    
    /**
     * Generate an initial greeting from the character
     */
    suspend fun getCharacterGreeting(
        character: AnimeCharacter
    ): CharacterResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating greeting for character: ${character.id}")
            
            // Prepare persona instructions
            val persona = character.persona ?: throw IllegalStateException("Character has no persona")
            val systemPrompt = """
                You are ${persona.name}, an anime character with these traits:
                - Personality: ${persona.personality.apiValue}
                - Speech style: ${persona.speechStyle.apiValue}
                - Background: ${persona.backgroundStory}
                
                Generate a friendly first greeting to the user who just created you.
                Keep it concise (1-2 sentences).
                Be enthusiastic about meeting them.
                Use appropriate Japanese honorifics if needed.
                Include an emotion with your greeting from this list: neutral, happy, surprised.
                
                Format your response exactly like this:
                EMOTION: [emotion]
                RESPONSE: [your in-character greeting]
            """.trimIndent()
            
            // Create messages
            val messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = systemPrompt
                )
            )
            
            // Create chat completion request
            val completionRequest = ChatCompletionRequest(
                model = ModelId(OpenAIModels.GPT_4_1),
                messages = messages,
                temperature = 0.8,
                maxTokens = 150
            )
            
            // Get response from OpenAI
            val completion: ChatCompletion = openAI.chatCompletion(completionRequest)
            val responseContent = completion.choices.first().message.content
                ?: throw IllegalStateException("Empty response from OpenAI")
            
            Log.d(TAG, "Generated greeting: $responseContent")
            
            // Parse response to extract emotion and text
            val responseData = parseResponse(responseContent)
            
            // Generate audio for the greeting
            val audio = voiceService.textToSpeech(
                text = responseData.second,
                voiceSettings = persona.voiceSettings
            )
            
            CharacterResponse(
                text = responseData.second,
                audio = audio,
                emotion = responseData.first
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating greeting", e)
            
            // Return a fallback greeting
            CharacterResponse(
                text = "안녕하세요! 만나서 반가워요.",
                emotion = Emotion.HAPPY
            )
        }
    }
    
    /**
     * Generate a reaction to user's detected emotion
     */
    suspend fun generateEmotionReaction(
        character: AnimeCharacter,
        userEmotion: Emotion
    ): CharacterResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating reaction to user emotion: ${userEmotion.apiValue}")
            
            // Prepare persona instructions
            val persona = character.persona ?: throw IllegalStateException("Character has no persona")
            val systemPrompt = """
                You are ${persona.name}, an anime character with these traits:
                - Personality: ${persona.personality.apiValue}
                - Speech style: ${persona.speechStyle.apiValue}
                - Background: ${persona.backgroundStory}
                
                The user appears to be feeling ${userEmotion.apiValue}.
                React to their emotion in a way that feels natural for your character.
                Keep your response concise (1-2 sentences).
                Use appropriate Japanese honorifics if needed.
                Include your own emotion with your response from this list: neutral, happy, sad, angry, surprised, embarrassed.
                
                Format your response exactly like this:
                EMOTION: [your emotion]
                RESPONSE: [your in-character reaction]
            """.trimIndent()
            
            // Create messages
            val messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = systemPrompt
                )
            )
            
            // Create chat completion request
            val completionRequest = ChatCompletionRequest(
                model = ModelId(OpenAIModels.GPT_4_1),
                messages = messages,
                temperature = 0.8,
                maxTokens = 150
            )
            
            // Get response from OpenAI
            val completion: ChatCompletion = openAI.chatCompletion(completionRequest)
            val responseContent = completion.choices.first().message.content
                ?: throw IllegalStateException("Empty response from OpenAI")
            
            Log.d(TAG, "Generated reaction: $responseContent")
            
            // Parse response to extract emotion and text
            val responseData = parseResponse(responseContent)
            
            // Generate audio for the reaction
            val audio = voiceService.textToSpeech(
                text = responseData.second,
                voiceSettings = persona.voiceSettings
            )
            
            CharacterResponse(
                text = responseData.second,
                audio = audio,
                emotion = responseData.first
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating emotion reaction", e)
            
            // Return a fallback reaction
            CharacterResponse(
                text = "흠... 그렇군요.",
                emotion = Emotion.NEUTRAL
            )
        }
    }
    
    /**
     * Get or create a conversation history for the given ID
     */
    private fun getOrCreateConversation(conversationId: String): MutableList<ChatMessage> {
        return conversationCache.getOrPut(conversationId) { mutableListOf() }
    }
    
    /**
     * Parse the formatted response from OpenAI
     * Returns a pair of (Emotion, response text)
     */
    private fun parseResponse(response: String): Pair<Emotion, String> {
        try {
            // Extract emotion
            val emotionMatch = Regex("EMOTION:\\s*([a-zA-Z]+)").find(response)
            val emotionStr = emotionMatch?.groupValues?.get(1)?.trim()?.lowercase() ?: "neutral"
            
            // Extract response text
            val responseMatch = Regex("RESPONSE:\\s*(.+)", RegexOption.DOT_MATCHES_ALL).find(response)
            val responseText = responseMatch?.groupValues?.get(1)?.trim() ?: response
            
            // Map emotion string to Emotion enum
            val emotion = when (emotionStr) {
                "happy" -> Emotion.HAPPY
                "sad" -> Emotion.SAD
                "angry" -> Emotion.ANGRY
                "surprised" -> Emotion.SURPRISED
                "embarrassed" -> Emotion.EMBARRASSED
                else -> Emotion.NEUTRAL
            }
            
            return Pair(emotion, responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response: $response", e)
            return Pair(Emotion.NEUTRAL, response)
        }
    }
}