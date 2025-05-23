package com.animeai.app.preview

import com.animeai.app.engine.AnimeCharacterEngine
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.CharacterResponse
import com.animeai.app.model.Emotion
import com.animeai.app.model.MockData
import com.animeai.app.model.VoiceSettings
import com.animeai.app.service.ConversationService
import com.animeai.app.service.CreditService
import com.animeai.app.service.VoiceService

/**
 * 미리보기용 가짜 대화 서비스
 */
class FakeConversationService : ConversationService {
    override suspend fun getCharacterGreeting(character: AnimeCharacter): CharacterResponse {
        return CharacterResponse(
            text = "안녕하세요! 반가워요. 제 이름은 ${character.persona?.name ?: "미유"}입니다. 어떻게 도와드릴까요?",
            emotion = Emotion.HAPPY
        )
    }

    override suspend fun generateResponse(
        userInput: String,
        character: AnimeCharacter,
        userId: String,
        conversationId: String
    ): CharacterResponse {
        return CharacterResponse(
            text = "네, 이해했어요. 더 이야기해 볼까요?",
            emotion = Emotion.NEUTRAL
        )
    }

    override suspend fun generateEmotionReaction(
        character: AnimeCharacter,
        userEmotion: Emotion
    ): CharacterResponse {
        return CharacterResponse(
            text = "당신의 감정이 느껴져요!",
            emotion = userEmotion
        )
    }
}

/**
 * 미리보기용 가짜 음성 서비스
 */
class FakeVoiceService : VoiceService {
    override suspend fun textToSpeech(text: String, voiceSettings: VoiceSettings): ByteArray {
        return ByteArray(0) // 빈 오디오 데이터
    }

    override suspend fun speechToText(audioData: ByteArray): String {
        return "안녕하세요, 반가워요!"
    }

    override suspend fun analyzeSpeechForLipSync(audioData: ByteArray): Map<Long, String> {
        return mapOf(
            0L to "A",
            500L to "O"
        )
    }
}

/**
 * 미리보기용 가짜 크레딧 서비스
 */
class FakeCreditService : CreditService {
    override suspend fun getUserCredits(userId: String): Float {
        return 50.0f
    }

    override suspend fun updateUserCredits(userId: String, newCredits: Float): Boolean {
        return true
    }

    override suspend fun addCredits(userId: String, credits: Float): Boolean {
        return true
    }

    override suspend fun deductCredits(userId: String, credits: Float): Boolean {
        return true
    }

    override suspend fun hasEnoughCredits(userId: String, requiredCredits: Float): Boolean {
        return true
    }
}