package com.animeai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.Emotion
import com.animeai.app.ui.components.AnimatedBackground
import com.animeai.app.ui.components.AnimatedCharacterView
import com.animeai.app.ui.components.ChatInterface
import com.animeai.app.ui.components.EmotionIndicator
import com.animeai.app.ui.components.UsageIndicator
import com.animeai.app.viewmodel.CharacterViewModel

/**
 * Main screen for interacting with the anime character
 */
@Composable
fun CharacterScreen(
    viewModel: CharacterViewModel,
    modifier: Modifier = Modifier
) {
    // Collect state from the ViewModel
    val character by viewModel.currentCharacter.collectAsState()
    val emotion by viewModel.currentEmotion.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val remainingCredits by viewModel.remainingCredits.collectAsState()
    val messages by viewModel.messages.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2D3047),
                        Color(0xFF1B1B25)
                    )
                )
            )
    ) {
        // Animated background elements (particles, etc.)
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        
        // Character view (upper 70% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.TopCenter)
        ) {
            // Animated character
            character?.let { char ->
                AnimatedCharacterView(
                    character = char,
                    currentEmotion = emotion,
                    isSpeaking = isSpeaking,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Emotion indicator in top-right corner
            EmotionIndicator(
                emotion = emotion,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
        
        // Chat interface (lower 30% of screen)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x001B1B25),
                            Color(0xFF1B1B25)
                        )
                    )
                )
        ) {
            ChatInterface(
                messages = messages,
                onTextInput = viewModel::sendTextMessage,
                onVoiceInput = viewModel::startVoiceRecording,
                onCameraInput = viewModel::captureUserExpression,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Credits indicator in top-left
        UsageIndicator(
            credits = remainingCredits,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}