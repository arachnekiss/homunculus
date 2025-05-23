package com.animeai.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.animeai.app.ui.components.CharacterView
import com.animeai.app.ui.components.ChatInterface
import com.animeai.app.ui.components.CameraView
import com.animeai.app.viewmodel.CharacterViewModel
import com.animeai.app.viewmodel.ConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterScreen(
    characterViewModel: CharacterViewModel,
    conversationViewModel: ConversationViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToStore: () -> Unit
) {
    var showCamera by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "AI Anime Character") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToStore) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Store")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background - can be customized
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Character Display Area (70% of screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f)
                ) {
                    if (showCamera) {
                        CameraView(
                            onEmotionDetected = { emotion ->
                                characterViewModel.updateCharacterEmotion(emotion)
                                showCamera = false
                            },
                            onClose = { showCamera = false }
                        )
                    } else {
                        CharacterView(
                            character = characterViewModel.currentCharacter.collectAsState().value,
                            emotion = characterViewModel.currentEmotion.collectAsState().value,
                            isAnimating = characterViewModel.isAnimating.collectAsState().value,
                            onTap = { characterViewModel.onCharacterTapped() }
                        )
                    }
                }
                
                // Chat Interface (30% of screen)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                ) {
                    ChatInterface(
                        messages = conversationViewModel.messages.collectAsState().value,
                        onSendMessage = { message ->
                            conversationViewModel.sendMessage(message)
                        },
                        onMicToggle = { isRecording ->
                            if (isRecording) {
                                conversationViewModel.startVoiceRecording()
                            } else {
                                conversationViewModel.stopVoiceRecordingAndProcess()
                            }
                        },
                        onCameraToggle = { showCamera = !showCamera }
                    )
                }
            }
            
            // Credits Display
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Surface(
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Credits: ${characterViewModel.userCredits.collectAsState().value}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
