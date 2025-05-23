package com.animeai.app.model

import kotlinx.serialization.Serializable

/**
 * Represents an anime character with associated images, expressions, and personality
 */
@Serializable
data class Character(
    val id: String,
    val baseImageUrl: String,
    val expressions: MutableMap<Emotion, String> = mutableMapOf(),
    var persona: CharacterPersona? = null
) {
    /**
     * Gets the image URL for a specific emotion
     * Falls back to neutral or base image if the specific emotion is not available
     */
    fun getExpressionForEmotion(emotion: Emotion): String {
        return expressions[emotion] 
            ?: expressions[Emotion.NEUTRAL] 
            ?: baseImageUrl
    }
    
    /**
     * Checks if the character has a specific expression
     */
    fun hasExpression(emotion: Emotion): Boolean {
        return expressions.containsKey(emotion)
    }
    
    /**
     * Updates the character's persona
     */
    fun updatePersona(newPersona: CharacterPersona) {
        persona = newPersona
    }
}

/**
 * Anime art styles
 */
enum class AnimeStyle(val value: String) {
    MODERN("modern anime"),
    RETRO("90s anime"),
    CHIBI("chibi"),
    SHOUJO("shoujo manga"),
    SHOUNEN("shounen manga")
}

/**
 * Emotions that the character can express
 */
enum class Emotion(val description: String) {
    NEUTRAL("a calm, neutral expression"),
    HAPPY("a joyful, smiling expression"),
    SAD("a sad, downcast expression"),
    ANGRY("an angry, frustrated expression"),
    SURPRISED("a shocked, surprised expression"),
    EMBARRASSED("a flustered, blushing expression")
}

/**
 * Represents a single frame in a lip-syncing animation
 */
@Serializable
data class LipSyncFrame(
    val timestamp: Float,
    val mouthShape: MouthShape,
    val imageUrl: String? = null
)

/**
 * Different mouth shapes for lip syncing
 */
enum class MouthShape {
    CLOSED,
    SLIGHTLY_OPEN,
    OPEN,
    WIDE_OPEN,
    SMILING,
    O_SHAPE
}
