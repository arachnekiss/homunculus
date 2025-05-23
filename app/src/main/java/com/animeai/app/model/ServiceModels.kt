package com.animeai.app.model

import kotlinx.serialization.Serializable

/**
 * Response model for API status checks
 */
@Serializable
data class ApiStatusResponse(
    val status: String,
    val message: String
)

/**
 * Request model for character generation
 */
@Serializable
data class CharacterGenerationRequest(
    val prompt: String,
    val style: String,
    val userId: String
)

/**
 * Response model for character generation
 */
@Serializable
data class CharacterGenerationResponse(
    val characterId: String,
    val baseImageUrl: String,
    val success: Boolean,
    val message: String? = null
)

/**
 * Request model for expression generation
 */
@Serializable
data class ExpressionGenerationRequest(
    val characterId: String,
    val emotion: String,
    val userId: String
)

/**
 * Response model for expression generation
 */
@Serializable
data class ExpressionGenerationResponse(
    val expressionUrl: String,
    val emotion: String,
    val success: Boolean,
    val message: String? = null
)

/**
 * Request model for persona generation
 */
@Serializable
data class PersonaGenerationRequest(
    val characterId: String,
    val userPreferences: UserPreferences
)

/**
 * Response model for credits update
 */
@Serializable
data class CreditsResponse(
    val userId: String,
    val credits: Float,
    val success: Boolean,
    val message: String? = null
)

/**
 * Request model for conversation
 */
@Serializable
data class ConversationRequest(
    val userId: String,
    val characterId: String,
    val message: String
)

/**
 * Response model for conversation
 */
@Serializable
data class ConversationResponse(
    val messageId: String,
    val response: String,
    val emotion: String,
    val audioUrl: String?,
    val success: Boolean,
    val message: String? = null
)