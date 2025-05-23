package com.animeai.app.model

import kotlinx.serialization.Serializable

/**
 * Represents an anime character with expressions and personality
 */
@Serializable
data class AnimeCharacter(
    val id: String,
    val baseImageUrl: String,
    val expressions: Map<Emotion, String> = mapOf(),
    val persona: CharacterPersona? = null,
    val creator: String = "AI",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Gets the appropriate expression image for the given emotion
     * Falls back to neutral or base image if not available
     */
    fun getExpressionForEmotion(emotion: Emotion): String {
        return expressions[emotion] 
            ?: expressions[Emotion.NEUTRAL] 
            ?: baseImageUrl
    }
    
    /**
     * Checks if this character has the specified expression
     */
    fun hasExpression(emotion: Emotion): Boolean {
        return expressions.containsKey(emotion)
    }
}

/**
 * Anime character style options
 */
enum class AnimeStyle(val apiValue: String, val displayName: String) {
    MODERN("modern_anime", "Modern Anime"),
    CLASSIC("classic_anime", "90s Anime"),
    CHIBI("chibi", "Chibi"),
    SHOUJO("shoujo", "Shoujo"),
    SHOUNEN("shounen", "Shounen")
}

/**
 * Character emotions for expressions
 */
enum class Emotion(val apiValue: String, val displayName: String) {
    NEUTRAL("neutral", "Neutral"),
    HAPPY("happy", "Happy"),
    SAD("sad", "Sad"),
    ANGRY("angry", "Angry"),
    SURPRISED("surprised", "Surprised"),
    EMBARRASSED("embarrassed", "Embarrassed")
}