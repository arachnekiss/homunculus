package com.animeai.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.Emotion
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Composable that displays an animated anime character with expressions
 */
@Composable
fun AnimatedCharacterView(
    character: AnimeCharacter,
    currentEmotion: Emotion,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Get the appropriate expression image based on emotion
    val imageUrl = character.getExpressionForEmotion(currentEmotion)
    
    // State for animation and blinking
    var shouldBlink by remember { mutableStateOf(false) }
    var isBlinking by remember { mutableStateOf(false) }
    
    // Random blinking effect
    LaunchedEffect(key1 = character.id) {
        while (true) {
            val waitTime = Random.nextInt(3000, 8000).toLong()
            delay(waitTime)
            isBlinking = true
            delay(200)  // Blink duration
            isBlinking = false
        }
    }
    
    // Subtle breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )
    
    // Talking animation
    val talkingOffset by animateFloatAsState(
        targetValue = if (isSpeaking) 1f else 0f,
        animationSpec = tween(300),
        label = "talking"
    )
    
    // Blinking scale animation
    val eyeScale by animateFloatAsState(
        targetValue = if (isBlinking) 0.2f else 1f,
        animationSpec = tween(100),
        label = "blinking"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Main character image with animations
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build()
            ),
            contentDescription = "Anime Character",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize(0.9f)
                .scale(breathingScale)
                .graphicsLayer {
                    // Apply slight movement for talking
                    if (isSpeaking) {
                        translationY = talkingOffset * 2f
                    }
                }
        )
        
        // Eye blinking overlay could be added here
        // Mouth animation overlay could be added here
    }
}