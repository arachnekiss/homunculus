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
enum class Personality(val apiValue: String, val displayName: String) {
    FRIENDLY("friendly", "Friendly"),
    SHY("shy", "Shy"),
    TSUNDERE("tsundere", "Tsundere"),
    COOL("cool", "Cool"),
    ENERGETIC("energetic", "Energetic"),
    MYSTERIOUS("mysterious", "Mysterious"),
    SERIOUS("serious", "Serious")
}

/**
 * Speech styles for character dialogue
 */
enum class SpeechStyle(val apiValue: String, val displayName: String) {
    FORMAL("formal", "Formal"),
    CASUAL("casual", "Casual"),
    CUTE("cute", "Cute"),
    POETIC("poetic", "Poetic"),
    SCHOLARLY("scholarly", "Scholarly"),
    PLAYFUL("playful", "Playful"),
    RESERVED("reserved", "Reserved")
}

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
                "style": "${preferredStyle.apiValue}",
                "personality": "${preferredPersonality.apiValue}",
                "speechStyle": "${preferredSpeechStyle.apiValue}",
                "customPrompt": "$customPrompt"
            }
        """.trimIndent()
    }
}