package com.animeai.app.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.camera.core.ImageProxy
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.Content
import com.aallam.openai.api.chat.ImageContent
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.animeai.app.model.Emotion
import com.animeai.app.util.ImageProcessing
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class CameraReactionEngine @Inject constructor(
    private val openAI: OpenAI,
    private val imageProcessing: ImageProcessing
) {
    
    // Analyze user's face to detect emotion
    suspend fun analyzeUserExpression(imageProxy: ImageProxy): Emotion {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Analyzing user expression")
                
                // Convert the camera image to a format we can send to the API
                val bitmap = imageProcessing.imageProxyToBitmap(imageProxy)
                val base64Image = bitmap.toBase64String()
                
                // Create the image content for the API
                val imageContent = ImageContent(
                    data = base64Image,
                    mimeType = "image/jpeg"
                )
                
                // Analyze the image using GPT-4 nano for speed
                val request = ChatCompletionRequest(
                    model = ModelId("gpt-4.1-nano"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = listOf(
                                TextContent(
                                    """
                                    Analyze the facial expression in the image and determine the user's emotion.
                                    Respond with exactly one of these emotions only:
                                    - happy
                                    - sad
                                    - angry
                                    - surprised
                                    - embarrassed
                                    - neutral
                                    No explanations, just the emotion name in lowercase.
                                    """
                                )
                            )
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = listOf(imageContent)
                        )
                    ),
                    maxTokens = 50
                )
                
                val response = openAI.chatCompletion(request)
                val emotionText = response.choices.first().message.content.toString().trim().lowercase()
                
                Log.d(TAG, "Detected emotion: $emotionText")
                
                // Map the emotion text to our Emotion enum
                when (emotionText) {
                    "happy" -> Emotion.HAPPY
                    "sad" -> Emotion.SAD
                    "angry" -> Emotion.ANGRY
                    "surprised" -> Emotion.SURPRISED
                    "embarrassed" -> Emotion.EMBARRASSED
                    else -> Emotion.NEUTRAL
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing user expression", e)
                Emotion.NEUTRAL // Default to neutral on error
            } finally {
                imageProxy.close()
            }
        }
    }
    
    // Helper extension to convert Bitmap to Base64 string
    private fun Bitmap.toBase64String(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    companion object {
        private const val TAG = "CameraReactionEngine"
    }
}
