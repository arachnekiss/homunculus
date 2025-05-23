package com.animeai.app.model

import kotlinx.serialization.Serializable

/**
 * Tracks usage of AI features for billing purposes
 */
@Serializable
data class Usage(
    val userId: String,
    val textTokens: Int = 0,
    val imageGenerations: Int = 0,
    val audioMinutes: Float = 0f,
    val storageBytes: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Calculate the cost based on current usage
     */
    fun calculateCost(): Float {
        return (textTokens * Config.TOKEN_COST) +
               (imageGenerations * Config.IMAGE_COST) +
               (audioMinutes * Config.AUDIO_COST)
    }
}

/**
 * User credit information
 */
@Serializable
data class UserCredits(
    val userId: String,
    val credits: Float = Config.DEFAULT_CREDITS,
    val lastPurchase: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Credit package for in-app purchases
 */
@Serializable
data class CreditPackage(
    val id: String,
    val name: String,
    val credits: Int,
    val price: Float,
    val description: String? = null
)

/**
 * Usage action types for tracking
 */
sealed class UsageAction

/**
 * Text generation usage (tokens)
 */
data class TextGeneration(val tokens: Int) : UsageAction()

/**
 * Image generation usage (count)
 */
data class ImageGeneration(val count: Int = 1) : UsageAction()

/**
 * Audio processing usage (minutes)
 */
data class AudioProcessing(val duration: Float) : UsageAction()