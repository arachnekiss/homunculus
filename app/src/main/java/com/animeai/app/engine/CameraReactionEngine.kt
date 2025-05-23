package com.animeai.app.engine

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.camera.core.ImageProxy
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.animeai.app.model.Emotion
import com.animeai.app.model.OpenAIModels
import com.animeai.app.util.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine for analyzing user facial expressions via camera
 * and generating appropriate character reactions
 */
@Singleton
class CameraReactionEngine @Inject constructor(
    private val openAI: OpenAI,
    private val imageUtil: ImageUtil
) {
    private val TAG = "CameraReactionEngine"
    
    /**
     * Analyze a camera image to detect user emotion
     */
    suspend fun analyzeUserExpression(imageProxy: ImageProxy): Emotion = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Analyzing user expression from camera")
            
            // Convert image to bitmap
            val bitmap = imageUtil.imageProxyToBitmap(imageProxy)
            
            // Resize for efficiency
            val resizedBitmap = imageUtil.resizeBitmap(bitmap, 512, 512)
            
            // Convert to Base64
            val base64Image = bitmapToBase64(resizedBitmap)
            
            // Create GPT-4 vision prompt
            val messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = listOf(
                        ChatMessage.Content.Text(
                            "Analyze this facial expression. Return only one word: happy, sad, surprised, neutral, angry, or embarrassed. Nothing else."
                        ),
                        ChatMessage.Content.Image(base64Image)
                    )
                )
            )
            
            // Create chat completion request
            val completionRequest = ChatCompletionRequest(
                model = ModelId(OpenAIModels.GPT_4_1_NANO),
                messages = messages,
                maxTokens = 10
            )
            
            // Get response from OpenAI
            val completion: ChatCompletion = openAI.chatCompletion(completionRequest)
            val response = completion.choices.first().message.content
                ?.trim()?.lowercase() ?: "neutral"
            
            Log.d(TAG, "Detected emotion: $response")
            
            // Parse the detected emotion
            return@withContext parseEmotion(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing user expression", e)
            return@withContext Emotion.NEUTRAL
        }
    }
    
    /**
     * Convert bitmap to Base64
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    /**
     * Parse emotion string to Emotion enum
     */
    private fun parseEmotion(emotionStr: String): Emotion {
        return when (emotionStr.trim().lowercase()) {
            "happy" -> Emotion.HAPPY
            "sad" -> Emotion.SAD
            "angry" -> Emotion.ANGRY
            "surprised" -> Emotion.SURPRISED
            "embarrassed" -> Emotion.EMBARRASSED
            else -> Emotion.NEUTRAL
        }
    }
}