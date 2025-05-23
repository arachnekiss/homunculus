package com.animeai.app.model

import kotlinx.serialization.Serializable

/**
 * Represents a message in the conversation between user and AI character
 */
@Serializable
data class Message(
    val id: String,
    val content: String,
    val role: Role,
    val timestamp: Long = System.currentTimeMillis(),
    val audioUrl: String? = null,
    val emotion: Emotion? = null
)

/**
 * Role in the conversation
 */
enum class Role {
    User,
    Assistant
}

/**
 * Response from the character including text, audio and animation data
 */
@Serializable
data class CharacterResponse(
    val text: String,
    val audio: ByteArray? = null,
    val emotion: Emotion = Emotion.NEUTRAL,
    val animation: List<String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CharacterResponse

        if (text != other.text) return false
        if (audio != null) {
            if (other.audio == null) return false
            if (!audio.contentEquals(other.audio)) return false
        } else if (other.audio != null) return false
        if (emotion != other.emotion) return false
        if (animation != other.animation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + (audio?.contentHashCode() ?: 0)
        result = 31 * result + emotion.hashCode()
        result = 31 * result + (animation?.hashCode() ?: 0)
        return result
    }
}

/**
 * Usage tracking action types
 */
sealed class UsageAction

data class TextGeneration(val tokens: Int) : UsageAction()
data class ImageGeneration(val count: Int = 1) : UsageAction()
data class AudioProcessing(val duration: Float) : UsageAction()
