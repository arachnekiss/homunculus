package com.animeai.app.service

import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.CharacterResponse
import com.animeai.app.model.Emotion

/**
 * 애니메이션 캐릭터와의 대화를 처리하는 서비스 인터페이스
 */
interface ConversationService {
    
    /**
     * 캐릭터의 인사말 생성
     */
    suspend fun getCharacterGreeting(character: AnimeCharacter): CharacterResponse
    
    /**
     * 사용자 입력에 대한 캐릭터 응답 생성
     */
    suspend fun generateResponse(
        userInput: String,
        character: AnimeCharacter,
        userId: String,
        conversationId: String
    ): CharacterResponse
    
    /**
     * 사용자 감정에 대한 캐릭터 반응 생성
     */
    suspend fun generateEmotionReaction(
        character: AnimeCharacter,
        userEmotion: Emotion
    ): CharacterResponse
}