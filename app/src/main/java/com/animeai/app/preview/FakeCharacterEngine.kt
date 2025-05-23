package com.animeai.app.preview

import android.graphics.Bitmap
import com.animeai.app.engine.AnimationType
import com.animeai.app.engine.Phoneme
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.Emotion
import com.animeai.app.util.ImageUtil
import javax.inject.Inject

/**
 * 미리보기용 가짜 캐릭터 애니메이션 엔진
 */
class FakeCharacterEngine @Inject constructor() {

    /**
     * 특정 감정에 맞는 캐릭터 표정 URL 생성
     */
    suspend fun generateExpressions(character: AnimeCharacter, emotion: Emotion): String {
        // 이미 있는 표정 반환하거나 기본 이미지 사용
        return character.expressions[emotion] ?: character.baseImageUrl
    }
    
    /**
     * 표정 간 전환 프레임 생성
     */
    suspend fun generateTransitionFrames(
        fromExpressionUrl: String,
        toExpressionUrl: String,
        frameCount: Int = 10
    ): List<Bitmap> {
        // 미리보기에서는 빈 리스트 반환
        return emptyList()
    }
    
    /**
     * 특정 애니메이션 생성 (눈 깜빡임, 말하기 등)
     */
    suspend fun animateExpression(
        expressionUrl: String,
        animationType: AnimationType
    ): List<Bitmap> {
        // 미리보기에서는 빈 리스트 반환
        return emptyList()
    }
    
    /**
     * 립싱크 애니메이션 생성
     */
    suspend fun generateLipSyncAnimation(
        expressionBitmap: Bitmap,
        phonemes: List<Phoneme>
    ): List<Bitmap> {
        // 미리보기에서는 빈 리스트 반환
        return emptyList()
    }
}