package com.animeai.app.service

import android.util.Log
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.AnimeStyle
import com.animeai.app.model.CharacterPersona
import com.animeai.app.model.Emotion
import com.animeai.app.model.OpenAIModels
import com.animeai.app.model.Personality
import com.animeai.app.model.SpeechStyle
import com.animeai.app.model.VoiceSettings
import com.animeai.app.util.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for generating anime characters using OpenAI
 */
@Singleton
class CharacterGenerationService @Inject constructor(
    private val openAI: OpenAI,
    private val imageUtil: ImageUtil
) {
    private val TAG = "CharacterGenService"
    
    /**
     * Generate a new anime character from a prompt
     */
    suspend fun generateCharacter(
        prompt: String,
        style: AnimeStyle = AnimeStyle.MODERN
    ): AnimeCharacter = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating character with prompt: $prompt, style: ${style.apiValue}")
            
            // Create the full prompt for the image generation
            val fullPrompt = """
                Generate a high-quality anime character portrait in ${style.apiValue} style.
                $prompt
                Clean background, front-facing, neutral expression, high detail on face and eyes.
                Professional anime art style, suitable for a visual novel or game.
            """.trimIndent()
            
            // Request image generation from OpenAI
            val imageResponse = openAI.imageCreation(
                creation = ImageCreation(
                    prompt = fullPrompt,
                    model = ModelId(OpenAIModels.GPT_IMAGE_1),
                    n = 1,
                    size = ImageSize.is1024x1024
                )
            )
            
            // Get the generated image URL
            val imageUrl = imageResponse.created.firstOrNull()?.url
                ?: throw IllegalStateException("Failed to generate character image")
            
            Log.d(TAG, "Character image generated: $imageUrl")
            
            // Create a character with default persona and the neutral expression
            val characterId = "char_${UUID.randomUUID()}"
            
            // Create a default persona
            val defaultPersona = CharacterPersona(
                name = "Anime Character",
                personality = Personality.FRIENDLY,
                speechStyle = SpeechStyle.CASUAL,
                backgroundStory = "A friendly anime character who enjoys chatting.",
                interests = listOf("Anime", "Games", "Art"),
                voiceSettings = VoiceSettings(pitch = 1.0f, speed = 1.0f)
            )
            
            // Return the new character with just the neutral expression for now
            AnimeCharacter(
                id = characterId,
                baseImageUrl = imageUrl,
                expressions = mapOf(Emotion.NEUTRAL to imageUrl),
                persona = defaultPersona
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating character", e)
            throw e
        }
    }
    
    /**
     * Generate an expression for an existing character
     */
    suspend fun generateExpression(
        character: AnimeCharacter,
        emotion: Emotion
    ): String = withContext(Dispatchers.IO) {
        try {
            // Skip if we already have this expression
            if (character.hasExpression(emotion)) {
                return@withContext character.getExpressionForEmotion(emotion)
            }
            
            Log.d(TAG, "Generating ${emotion.apiValue} expression for character ${character.id}")
            
            // Download the base image bytes
            val baseImageBytes = imageUtil.downloadImageAsBytes(character.baseImageUrl)
            if (baseImageBytes.isEmpty()) {
                throw IllegalStateException("Failed to download base image")
            }
            
            // Generate the expression prompt
            val prompt = """
                Modify this anime character to show ${emotion.apiValue} expression.
                Keep the same character, only change facial expression.
                Maintain anime art style and character consistency.
                Focus on: eyes, mouth, eyebrows.
                The expression should clearly show ${emotion.apiValue}.
            """.trimIndent()
            
            // Generate the new expression using image variation
            val imageVariation = openAI.imageCreation(
                creation = ImageCreation(
                    prompt = prompt,
                    model = ModelId(OpenAIModels.GPT_IMAGE_1),
                    n = 1,
                    size = ImageSize.is1024x1024
                )
            )
            
            // Get the generated expression URL
            val expressionUrl = imageVariation.created.firstOrNull()?.url
                ?: throw IllegalStateException("Failed to generate expression image")
            
            Log.d(TAG, "Expression generated: $expressionUrl")
            
            expressionUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error generating expression", e)
            throw e
        }
    }
}