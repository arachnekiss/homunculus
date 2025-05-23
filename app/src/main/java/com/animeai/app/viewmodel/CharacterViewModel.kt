package com.animeai.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animeai.app.engine.AnimeCharacterEngine
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.CharacterResponse
import com.animeai.app.model.Emotion
import com.animeai.app.model.Message
import com.animeai.app.model.Role
import com.animeai.app.service.CharacterGenerationService
import com.animeai.app.service.ConversationService
import com.animeai.app.service.CreditService
import com.animeai.app.service.VoiceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the main character interaction screen
 */
@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val conversationService: ConversationService,
    private val characterEngine: AnimeCharacterEngine,
    private val voiceService: VoiceService,
    private val creditService: CreditService
) : ViewModel() {
    
    private val TAG = "CharacterViewModel"
    
    // Character state
    private val _currentCharacter = MutableStateFlow<AnimeCharacter?>(null)
    val currentCharacter: StateFlow<AnimeCharacter?> = _currentCharacter.asStateFlow()
    
    // Emotion state
    private val _currentEmotion = MutableStateFlow(Emotion.NEUTRAL)
    val currentEmotion: StateFlow<Emotion> = _currentEmotion.asStateFlow()
    
    // Speaking state
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    // Credit state
    private val _remainingCredits = MutableStateFlow(100f)
    val remainingCredits: StateFlow<Float> = _remainingCredits.asStateFlow()
    
    // Messages state
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    // User ID (would be retrieved from auth in a real app)
    private val userId = "user_${UUID.randomUUID()}"
    
    // Current conversation ID
    private var conversationId: String? = null
    
    // Audio playback job
    private var audioPlaybackJob: Job? = null
    
    // 에러 상태
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * 초기화 시 실행
     */
    init {
        // 테스트 데이터 로드
        loadTestCharacter()
    }

    /**
     * 테스트 캐릭터 로드
     */
    fun loadTestCharacter() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // MockData에서 테스트 캐릭터 로드
                val testCharacter = com.animeai.app.model.MockData.testCharacter
                initializeCharacter(testCharacter)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading test character", e)
                _error.value = "테스트 캐릭터를 로드하는 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Initialize the character
     */
    fun initializeCharacter(character: AnimeCharacter) {
        _currentCharacter.value = character
        conversationId = "conv_${UUID.randomUUID()}"
        
        // Start with greeting from character
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Add loading message
                addSystemMessage("캐릭터가 응답하는 중...")
                
                // Get character greeting
                val greeting = conversationService.getCharacterGreeting(character)
                
                // Remove loading message and add character message
                removeSystemMessages()
                addCharacterMessage(greeting)
                
                // Speak the greeting
                speakMessage(greeting.text, greeting.emotion)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing character", e)
                removeSystemMessages()
                addSystemMessage("캐릭터를 불러오는 중 오류가 발생했습니다.")
                _error.value = "캐릭터를 초기화하는 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
        
        // Load credits
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
     * Send a text message to the character
     */
    fun sendTextMessage(text: String) {
        if (text.isBlank() || currentCharacter.value == null) return
        
        // Add user message
        val userMessage = Message(
            content = text,
            role = Role.User,
            timestamp = System.currentTimeMillis()
        )
        addMessage(userMessage)
        
        // Get character response
        getCharacterResponse(text)
    }
    
    /**
     * Start voice recording
     */
    fun startVoiceRecording() {
        // In a real app, this would start audio recording
        viewModelScope.launch {
            try {
                // Show recording status
                addSystemMessage("음성을 녹음하는 중...")
                
                // Simulate recording delay
                delay(2000)
                
                // Simulate voice recognition result
                val recognizedText = "안녕하세요, 오늘 날씨가 어때요?"
                
                // Remove recording status
                removeSystemMessages()
                
                // Send recognized text as message
                sendTextMessage(recognizedText)
            } catch (e: Exception) {
                Log.e(TAG, "Error recording voice", e)
                removeSystemMessages()
                addSystemMessage("음성 인식 중 오류가 발생했습니다.")
            }
        }
    }
    
    /**
     * Capture user expression to trigger character reaction
     */
    fun captureUserExpression() {
        // In a real app, this would access the camera and analyze the image
        viewModelScope.launch {
            try {
                // Show processing status
                addSystemMessage("표정을 분석하는 중...")
                
                // Simulate processing delay
                delay(1500)
                
                // Simulate detected emotion
                val detectedEmotion = Emotion.values().random()
                
                // Remove processing status
                removeSystemMessages()
                
                // Add system message about detected emotion
                addSystemMessage("감지된 감정: ${detectedEmotion.displayName}")
                
                // Character reacts to user emotion
                reactToUserEmotion(detectedEmotion)
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing expression", e)
                removeSystemMessages()
                addSystemMessage("표정 분석 중 오류가 발생했습니다.")
            }
        }
    }
    
    /**
     * Get character response to user input
     */
    private fun getCharacterResponse(userText: String) {
        val character = currentCharacter.value ?: return
        
        viewModelScope.launch {
            try {
                // Add loading message
                addSystemMessage("캐릭터가 응답하는 중...")
                
                // Check if enough credits
                if (remainingCredits.value <= 0) {
                    removeSystemMessages()
                    addSystemMessage("크레딧이 부족합니다. 크레딧을 추가로 구매해주세요.")
                    return@launch
                }
                
                // Get character response
                val response = conversationService.generateResponse(
                    userInput = userText,
                    character = character,
                    userId = userId,
                    conversationId = conversationId ?: "conv_${UUID.randomUUID()}"
                )
                
                // Remove loading message
                removeSystemMessages()
                
                // Add character response
                addCharacterMessage(response)
                
                // Update emotion based on response
                updateEmotion(response.emotion)
                
                // Speak the response
                speakMessage(response.text, response.emotion)
                
                // Update credits
                updateCreditsAfterInteraction()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting character response", e)
                removeSystemMessages()
                addSystemMessage("응답을 생성하는 중 오류가 발생했습니다.")
            }
        }
    }
    
    /**
     * Character reacts to user emotion
     */
    private fun reactToUserEmotion(userEmotion: Emotion) {
        val character = currentCharacter.value ?: return
        
        viewModelScope.launch {
            try {
                // Add loading message
                addSystemMessage("캐릭터가 반응하는 중...")
                
                // Check if enough credits
                if (remainingCredits.value <= 0) {
                    removeSystemMessages()
                    addSystemMessage("크레딧이 부족합니다. 크레딧을 추가로 구매해주세요.")
                    return@launch
                }
                
                // Get character reaction
                val reaction = conversationService.generateEmotionReaction(
                    character = character,
                    userEmotion = userEmotion
                )
                
                // Remove loading message
                removeSystemMessages()
                
                // Add character reaction
                addCharacterMessage(reaction)
                
                // Update emotion based on reaction
                updateEmotion(reaction.emotion)
                
                // Speak the reaction
                speakMessage(reaction.text, reaction.emotion)
                
                // Update credits
                updateCreditsAfterInteraction()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting character reaction", e)
                removeSystemMessages()
                addSystemMessage("반응을 생성하는 중 오류가 발생했습니다.")
            }
        }
    }
    
    /**
     * Speak a message using the character's voice
     */
    private fun speakMessage(text: String, emotion: Emotion) {
        // Cancel any ongoing audio playback
        audioPlaybackJob?.cancel()
        
        audioPlaybackJob = viewModelScope.launch {
            try {
                val character = currentCharacter.value ?: return@launch
                val persona = character.persona ?: return@launch
                
                // Start speaking animation
                _isSpeaking.value = true
                
                // In a real app, this would use the voice service to convert text to speech
                // and play the audio in sync with lip movements
                delay(text.length * 50L) // Simulate speaking time
                
                // Stop speaking animation
                _isSpeaking.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error speaking message", e)
                _isSpeaking.value = false
            }
        }
    }
    
    /**
     * Update the current emotion
     */
    private fun updateEmotion(emotion: Emotion) {
        _currentEmotion.value = emotion
    }
    
    /**
     * Update credits after an interaction
     */
    private fun updateCreditsAfterInteraction() {
        viewModelScope.launch {
            try {
                // In a real app, this would query the actual credit usage
                // For now, simulate a credit usage of 0.1 to 0.3 credits
                val usage = (0.1f + Math.random() * 0.2f).toFloat()
                val currentCredits = _remainingCredits.value
                _remainingCredits.value = (currentCredits - usage).coerceAtLeast(0f)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating credits", e)
            }
        }
    }
    
    /**
     * Add a message to the conversation
     */
    private fun addMessage(message: Message) {
        _messages.update { currentMessages ->
            currentMessages + message
        }
    }
    
    /**
     * Add a character message to the conversation
     */
    private fun addCharacterMessage(response: CharacterResponse) {
        val message = Message(
            content = response.text,
            role = Role.Assistant,
            timestamp = System.currentTimeMillis(),
            emotion = response.emotion
        )
        addMessage(message)
    }
    
    /**
     * Add a system message to the conversation
     */
    private fun addSystemMessage(text: String) {
        val message = Message(
            id = "system_${System.currentTimeMillis()}",
            content = text,
            role = Role.Assistant,
            timestamp = System.currentTimeMillis()
        )
        addMessage(message)
    }
    
    /**
     * Remove system messages from the conversation
     */
    private fun removeSystemMessages() {
        _messages.update { currentMessages ->
            currentMessages.filter { !it.id.startsWith("system_") }
        }
    }
    
    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        audioPlaybackJob?.cancel()
    }
}