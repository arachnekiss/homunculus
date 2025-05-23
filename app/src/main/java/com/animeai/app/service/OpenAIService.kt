package com.animeai.app.service

import android.util.Log
import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.animeai.app.BuildConfig
import com.animeai.app.model.OpenAIModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * OpenAI API와의 통신을 담당하는 서비스
 */
@Singleton
class OpenAIService @Inject constructor() {
    private val TAG = "OpenAIService"
    
    // OpenAI 클라이언트 초기화
    private val openAI by lazy {
        val apiKey = BuildConfig.OPENAI_API_KEY.ifEmpty {
            // API 키가 설정되지 않은 경우
            Log.e(TAG, "OpenAI API key not configured. Using mock data.")
            "dummy_key_for_mock_data"
        }
        
        OpenAI(
            config = OpenAIConfig(
                token = apiKey,
                timeout = Timeout(socket = 60.seconds)
            )
        )
    }
    
    /**
     * 텍스트 완성(채팅)
     */
    suspend fun chatCompletion(messages: List<ChatMessage>, model: String = OpenAIModels.GPT_4_1): String = withContext(Dispatchers.IO) {
        try {
            // API 키가 설정되지 않은 경우 Mock 데이터 반환
            if (BuildConfig.OPENAI_API_KEY.isEmpty()) {
                return@withContext generateMockResponse(messages)
            }
            
            Log.d(TAG, "Requesting chat completion with ${messages.size} messages")
            
            val completionRequest = ChatCompletionRequest(
                model = ModelId(model),
                messages = messages,
                temperature = 0.7
            )
            
            val response = openAI.chatCompletion(completionRequest)
            val result = response.choices.first().message.content ?: "No response"
            
            Log.d(TAG, "Chat completion received: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in chat completion", e)
            "오류가 발생했습니다. 다시 시도해주세요."
        }
    }
    
    /**
     * 이미지 생성
     */
    suspend fun generateImage(prompt: String, model: String = OpenAIModels.GPT_IMAGE_1): String = withContext(Dispatchers.IO) {
        try {
            // API 키가 설정되지 않은 경우 Mock 이미지 URL 반환
            if (BuildConfig.OPENAI_API_KEY.isEmpty()) {
                return@withContext "https://via.placeholder.com/512x512.png?text=AI+Anime+Character"
            }
            
            Log.d(TAG, "Requesting image generation with prompt: $prompt")
            
            val imageRequest = ImageCreation(
                prompt = prompt,
                model = ModelId(model),
                n = 1
            )
            
            val response = openAI.imageCreation(imageRequest)
            val imageUrl = response.created.firstOrNull()?.url
                ?: throw IllegalStateException("No image URL in response")
            
            Log.d(TAG, "Image generated: $imageUrl")
            imageUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error in image generation", e)
            "https://via.placeholder.com/512x512.png?text=Error"
        }
    }
    
    /**
     * 텍스트를 음성으로 변환
     */
    suspend fun textToSpeech(text: String, voice: String = "alloy", model: String = OpenAIModels.GPT_4O_MINI_TTS): ByteArray = withContext(Dispatchers.IO) {
        try {
            // API 키가 설정되지 않은 경우 빈 ByteArray 반환
            if (BuildConfig.OPENAI_API_KEY.isEmpty()) {
                return@withContext ByteArray(0)
            }
            
            Log.d(TAG, "Requesting text-to-speech conversion: $text")
            
            val speechRequest = SpeechRequest(
                model = ModelId(model),
                input = text,
                voice = voice
            )
            
            val audio = openAI.speech(speechRequest)
            
            Log.d(TAG, "Audio generated: ${audio.size} bytes")
            audio
        } catch (e: Exception) {
            Log.e(TAG, "Error in text-to-speech conversion", e)
            ByteArray(0)
        }
    }
    
    /**
     * Mock 응답 생성 (테스트용)
     */
    private fun generateMockResponse(messages: List<ChatMessage>): String {
        // 사용자 메시지 가져오기
        val lastUserMessage = messages.lastOrNull { it.role == ChatRole.User }?.content ?: ""
        
        // 간단한 응답 생성
        return when {
            lastUserMessage.contains("안녕") -> "안녕하세요! 오늘 기분이 어떠신가요?"
            lastUserMessage.contains("이름") -> "저는 AI 애니메이션 캐릭터 친구입니다. 원하시는 이름으로 불러주세요!"
            lastUserMessage.contains("날씨") -> "제가 날씨를 확인할 수는 없지만, 오늘 하루가 좋은 날이길 바랍니다!"
            lastUserMessage.contains("감사") -> "천만에요! 더 필요한 것이 있으면 언제든지 말씀해주세요."
            else -> "네, 더 얘기해 볼까요? 어떤 이야기든 들려주세요."
        }
    }
}