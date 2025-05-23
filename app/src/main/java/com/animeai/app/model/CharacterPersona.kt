package com.animeai.app.model

import kotlinx.serialization.Serializable

/**
 * Represents the personality and characteristics of an anime character
 */
@Serializable
data class CharacterPersona(
    val name: String,
    val personality: Personality,
    val speechStyle: SpeechStyle,
    val backgroundStory: String,
    val interests: List<String>,
    val voiceSettings: VoiceSettings
)

/**
 * Personality types for characters
 */
enum class Personality {
    FRIENDLY,
    SHY,
    TSUNDERE,
    COOL,
    ENERGETIC,
    MYSTERIOUS,
    SERIOUS
}

/**
 * Speech styles for character dialogue
 */
enum class SpeechStyle {
    FORMAL,
    CASUAL,
    CUTE,
    POETIC,
    SCHOLARLY,
    PLAYFUL,
    RESERVED
}

/**
 * Voice settings for text-to-speech
 */
@Serializable
data class VoiceSettings(
    val pitch: Float = 1.0f,  // Range: 0.5f to 1.5f
    val speed: Float = 1.0f   // Range: 0.5f to 1.5f
)

/**
 * User preferences for character generation
 */
@Serializable
data class UserPreferences(
    val preferredStyle: AnimeStyle = AnimeStyle.MODERN,
    val preferredPersonality: Personality = Personality.FRIENDLY,
    val preferredSpeechStyle: SpeechStyle = SpeechStyle.CASUAL,
    val customPrompt: String = ""
) {
    fun toJson(): String {
        return """
            {
                "style": "${preferredStyle.value}",
                "personality": "${preferredPersonality.name}",
                "speechStyle": "${preferredSpeechStyle.name}",
                "customPrompt": "$customPrompt"
            }
        """.trimIndent()
    }
}
