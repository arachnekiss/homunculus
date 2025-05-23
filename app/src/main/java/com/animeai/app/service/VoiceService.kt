package com.animeai.app.service

import com.animeai.app.model.VoiceSettings

/**
 * 음성 관련 기능을 위한 서비스 인터페이스
 */
interface VoiceService {
    
    /**
     * 텍스트를 음성으로 변환
     */
    suspend fun textToSpeech(text: String, voiceSettings: VoiceSettings): ByteArray
    
    /**
     * 음성을 텍스트로 변환
     */
    suspend fun speechToText(audioData: ByteArray): String
    
    /**
     * 음성을 립싱크용으로 분석
     */
    suspend fun analyzeSpeechForLipSync(audioData: ByteArray): Map<Long, String>
}