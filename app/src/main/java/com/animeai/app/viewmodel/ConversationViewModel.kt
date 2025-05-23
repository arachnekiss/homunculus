package com.animeai.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animeai.app.model.Character
import com.animeai.app.model.CharacterResponse
import com.animeai.app.model.Message
import com.animeai.app.repository.CharacterRepository
import com.animeai.app.repository.ConversationRepository
import com.animeai.app.util.AudioProcessing
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val characterRepository: CharacterRepository,
    private val audioProcessing: AudioProcessing
) : ViewModel() {
    
    // Messages in the conversation
    val messages = conversationRepository.messages
    
    // Processing state
    val isProcessing = conversationRepository.isProcessing
    
    // Audio recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    // Audio playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    // Character response state
    private val _characterResponse = MutableStateFlow<CharacterResponse?>(null)
    val characterResponse: StateFlow<CharacterResponse?> = _characterResponse.asStateFlow()
    
    // Audio recorder
    private var audioRecorder: AudioProcessing.AudioRecorder? = null
    
    // Audio player
    private var audioPlayer: AudioProcessing.AudioPlayer? = null
    
    init {
        viewModelScope.launch {
            // Observe current character changes
            characterRepository.currentCharacter.collect { character ->
                if (character != null) {
                    loadConversationForCharacter(character)
                }
            }
        }
    }
    
    /**
     * Send a text message from the user
     */
    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Process the message
                val response = conversationRepository.processUserMessage(messageText)
                
                // Update character emotion based on response
                characterRepository.currentCharacter.value?.let { character ->
                    (characterRepository as? CharacterViewModel)?.updateCharacterEmotion(
                        response.emotion
                    )
                }
                
                // Store response for playback
                _characterResponse.value = response
                
                // Play audio response
                playResponseAudio(response.audio)
                
                Log.d(TAG, "Message sent and processed: $messageText")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }
    }
    
    /**
     * Start voice recording
     */
    fun startVoiceRecording() {
        viewModelScope.launch {
            try {
                _isRecording.value = true
                
                // Create and start the audio recorder
                audioRecorder = audioProcessing.createAudioRecorder()
                audioRecorder?.start()
                
                Log.d(TAG, "Voice recording started")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting voice recording", e)
                _isRecording.value = false
            }
        }
    }
    
    /**
     * Stop voice recording and process the recorded audio
     */
    fun stopVoiceRecordingAndProcess() {
        viewModelScope.launch {
            try {
                if (!_isRecording.value) return@launch
                
                // Stop recording
                val audioData = audioRecorder?.stop()
                audioRecorder = null
                _isRecording.value = false
                
                if (audioData != null && audioData.isNotEmpty()) {
                    // Transcribe the audio
                    val transcribedText = conversationRepository.processVoiceRecording(audioData)
                    
                    // Send the transcribed text as a message
                    if (transcribedText.isNotBlank()) {
                        sendMessage(transcribedText)
                    }
                }
                
                Log.d(TAG, "Voice recording processed")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing voice recording", e)
                _isRecording.value = false
            }
        }
    }
    
    /**
     * Play audio response from character
     */
    private fun playResponseAudio(audioData: ByteArray?) {
        viewModelScope.launch {
            try {
                if (audioData == null || audioData.isEmpty()) return@launch
                
                _isPlaying.value = true
                
                // Create and start the audio player
                audioPlayer = audioProcessing.createAudioPlayer(
                    audioData = audioData,
                    onComplete = {
                        viewModelScope.launch {
                            _isPlaying.value = false
                            audioPlayer = null
                        }
                    }
                )
                audioPlayer?.start()
                
                Log.d(TAG, "Playing response audio")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing response audio", e)
                _isPlaying.value = false
                audioPlayer = null
            }
        }
    }
    
    /**
     * Stop audio playback
     */
    fun stopAudioPlayback() {
        viewModelScope.launch {
            try {
                audioPlayer?.stop()
                audioPlayer = null
                _isPlaying.value = false
                
                Log.d(TAG, "Audio playback stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping audio playback", e)
            }
        }
    }
    
    /**
     * Load conversation history for a character
     */
    private fun loadConversationForCharacter(character: Character) {
        viewModelScope.launch {
            try {
                conversationRepository.loadConversationHistory(character.id)
                Log.d(TAG, "Loaded conversation for character ${character.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading conversation", e)
            }
        }
    }
    
    /**
     * Clear conversation
     */
    fun clearConversation() {
        viewModelScope.launch {
            conversationRepository.clearConversation()
            Log.d(TAG, "Conversation cleared")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Clean up resources
        audioRecorder?.stop()
        audioPlayer?.stop()
    }
    
    companion object {
        private const val TAG = "ConversationViewModel"
    }
}
