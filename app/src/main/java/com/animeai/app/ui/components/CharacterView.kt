package com.animeai.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.animeai.app.model.Character
import com.animeai.app.model.Emotion

@Composable
fun CharacterView(
    character: Character?,
    emotion: Emotion,
    isAnimating: Boolean,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        if (character != null) {
            // Get the appropriate image based on emotion
            val imageUrl = remember(character, emotion) {
                character.getExpressionForEmotion(emotion)
            }
            
            // Use Coil to load and display the image
            val painter = rememberAsyncImagePainter(imageUrl)
            
            Image(
                painter = painter,
                contentDescription = "Anime Character",
                modifier = Modifier.fillMaxSize(0.9f),
                contentScale = ContentScale.Fit
            )
            
            // Handle animation effects when character is speaking
            if (isAnimating) {
                LaunchedEffect(key1 = isAnimating) {
                    // Animation code here if needed
                }
                
                DisposableEffect(key1 = Unit) {
                    onDispose {
                        // Clean up animation resources if needed
                    }
                }
            }
        }
    }
}
