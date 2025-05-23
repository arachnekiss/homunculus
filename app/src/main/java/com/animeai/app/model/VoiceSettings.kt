package com.animeai.app.model

import kotlinx.serialization.Serializable

/**
 * Settings for text-to-speech voice generation
 */
@Serializable
data class VoiceSettings(
    val pitch: Float = 1.0f,  // Range: 0.5f to 1.5f
    val speed: Float = 1.0f,  // Range: 0.5f to 1.5f
    val voiceType: VoiceType = VoiceType.ALLOY
)

/**
 * Available voice types for text-to-speech
 */
enum class VoiceType(val apiValue: String, val displayName: String) {
    ALLOY("alloy", "Alloy"),
    SHIMMER("shimmer", "Shimmer"),
    NOVA("nova", "Nova"),
    ECHO("echo", "Echo"),
    FABLE("fable", "Fable")
}