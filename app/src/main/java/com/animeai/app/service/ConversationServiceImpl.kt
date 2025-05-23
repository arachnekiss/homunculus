package com.animeai.app.service

import android.util.Log
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.CharacterResponse
import com.animeai.app.model.Emotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 실제 구현된 대화 서비스
 */
@Singleton
class ConversationServiceImpl @Inject constructor(
    private val openAIService: OpenAIService,
    private val voiceService: VoiceService
) {
    private val TAG = "ConversationServiceImpl"
    
    // 대화 내용 캐시
    private val conversationCache = mutableMapOf<String, MutableList<ChatMessage>>()
    
    /**
     * 캐릭터의 인사말 생성
     */
    suspend fun getCharacterGreeting(character: AnimeCharacter): CharacterResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "캐릭터 인사말 생성: ${character.id}")
            
            // 페르소나 지시사항 준비
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
            
            // 메시지 생성
            val messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = systemPrompt
                )
            )
            
            // OpenAIService를 통해 응답 생성
            val responseContent = openAIService.chatCompletion(messages)
            
            Log.d(TAG, "생성된 인사말: $responseContent")
            
            // 응답 파싱하여 감정과 텍스트 추출
            val responseData = parseResponse(responseContent)
            
            // 음성 생성
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
            Log.e(TAG, "인사말 생성 오류", e)
            
            // 기본 인사말 반환
            CharacterResponse(
                text = "안녕하세요! 만나서 반가워요.",
                emotion = Emotion.HAPPY
            )
        }
    }
    
    /**
     * 사용자 입력에 대한 캐릭터 응답 생성
     */
    suspend fun generateResponse(
        userInput: String,
        character: AnimeCharacter,
        userId: String,
        conversationId: String
    ): CharacterResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "응답 생성 중: $userInput")
            
            // 대화 기록 가져오기 또는 생성
            val messagesHistory = getOrCreateConversation(conversationId)
            
            // 사용자 메시지 추가
            val userMessage = ChatMessage(
                role = ChatRole.User,
                content = userInput
            )
            messagesHistory.add(userMessage)
            
            // 페르소나 지시사항 준비
            val persona = character.persona ?: throw IllegalStateException("Character has no persona")
            val systemPrompt = """
                You are ${persona.name}, an anime character with these traits:
                - Personality: ${persona.personality.apiValue}
                - Speech style: ${persona.speechStyle.apiValue}
                - Background: ${persona.backgroundStory}
                
                Respond in character, maintaining consistency with your persona.
                Keep responses concise (1-3 sentences).
                Use appropriate Japanese honorifics if needed.
                Express the current emotion in your response from this list: neutral, happy, sad, angry, surprised, embarrassed.
                
                Format your response exactly like this:
                EMOTION: [emotion]
                RESPONSE: [your in-character response]
            """.trimIndent()
            
            // 최신 대화 내용 (토큰 절약을 위해 최근 10개로 제한)
            val recentMessages = messagesHistory.takeLast(10).toMutableList()
            
            // 시스템 프롬프트를 처음에 추가
            recentMessages.add(0, ChatMessage(
                role = ChatRole.System,
                content = systemPrompt
            ))
            
            // OpenAIService를 통해 응답 생성
            val responseContent = openAIService.chatCompletion(recentMessages)
            
            Log.d(TAG, "생성된 응답: $responseContent")
            
            // 응답 파싱하여 감정과 텍스트 추출
            val responseData = parseResponse(responseContent)
            
            // 어시스턴트 응답을 기록에 추가
            messagesHistory.add(ChatMessage(
                role = ChatRole.Assistant,
                content = responseData.second
            ))
            
            // 응답 음성 생성
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
            Log.e(TAG, "응답 생성 오류", e)
            
            // 기본 응답 반환
            CharacterResponse(
                text = "죄송합니다, 지금은 대답할 수 없어요...",
                emotion = Emotion.NEUTRAL
            )
        }
    }
    
    /**
     * 사용자 감정에 대한 캐릭터 반응 생성
     */
    suspend fun generateEmotionReaction(
        character: AnimeCharacter,
        userEmotion: Emotion
    ): CharacterResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "감정 반응 생성 중: ${userEmotion.apiValue}")
            
            // 페르소나 지시사항 준비
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
            
            // 메시지 생성
            val messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = systemPrompt
                )
            )
            
            // OpenAIService를 통해 응답 생성
            val responseContent = openAIService.chatCompletion(messages)
            
            Log.d(TAG, "생성된 반응: $responseContent")
            
            // 응답 파싱하여 감정과 텍스트 추출
            val responseData = parseResponse(responseContent)
            
            // 음성 생성
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
            Log.e(TAG, "감정 반응 생성 오류", e)
            
            // 기본 반응 반환
            CharacterResponse(
                text = "흠... 그렇군요.",
                emotion = Emotion.NEUTRAL
            )
        }
    }
    
    /**
     * 대화 ID로 대화 기록 가져오기 또는 생성
     */
    private fun getOrCreateConversation(conversationId: String): MutableList<ChatMessage> {
        return conversationCache.getOrPut(conversationId) { mutableListOf() }
    }
    
    /**
     * OpenAI 응답에서 감정과 텍스트 추출
     * (Emotion, 응답 텍스트) 쌍 반환
     */
    private fun parseResponse(response: String): Pair<Emotion, String> {
        try {
            // 감정 추출
            val emotionMatch = Regex("EMOTION:\\s*([a-zA-Z]+)").find(response)
            val emotionStr = emotionMatch?.groupValues?.get(1)?.trim()?.lowercase() ?: "neutral"
            
            // 응답 텍스트 추출
            val responseMatch = Regex("RESPONSE:\\s*(.+)", RegexOption.DOT_MATCHES_ALL).find(response)
            val responseText = responseMatch?.groupValues?.get(1)?.trim() ?: response
            
            // 감정 문자열을 Emotion 열거형으로 변환
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
            Log.e(TAG, "응답 파싱 오류: $response", e)
            return Pair(Emotion.NEUTRAL, response)
        }
    }
}