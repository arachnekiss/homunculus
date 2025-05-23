package com.animeai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.animeai.app.model.CharacterPersona
import com.animeai.app.model.Personality
import com.animeai.app.model.SpeechStyle
import com.animeai.app.model.VoiceSettings
import com.animeai.app.viewmodel.CharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    characterViewModel: CharacterViewModel,
    onNavigateBack: () -> Unit
) {
    val character = characterViewModel.currentCharacter.collectAsState().value
    val persona = character?.persona ?: CharacterPersona(
        name = "",
        personality = Personality.FRIENDLY,
        speechStyle = SpeechStyle.CASUAL,
        backgroundStory = "",
        interests = emptyList(),
        voiceSettings = VoiceSettings(pitch = 1.0f, speed = 1.0f)
    )
    
    var name by remember { mutableStateOf(persona.name) }
    var selectedPersonality by remember { mutableStateOf(persona.personality) }
    var selectedSpeechStyle by remember { mutableStateOf(persona.speechStyle) }
    var backgroundStory by remember { mutableStateOf(persona.backgroundStory) }
    var voicePitch by remember { mutableStateOf(persona.voiceSettings.pitch) }
    var voiceSpeed by remember { mutableStateOf(persona.voiceSettings.speed) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Character Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Character Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Text("Personality", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Personality.values().forEach { personality ->
                        FilterChip(
                            selected = selectedPersonality == personality,
                            onClick = { selectedPersonality = personality },
                            label = { Text(personality.name) }
                        )
                    }
                }
            }
            
            item {
                Text("Speech Style", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeechStyle.values().forEach { style ->
                        FilterChip(
                            selected = selectedSpeechStyle == style,
                            onClick = { selectedSpeechStyle = style },
                            label = { Text(style.name) }
                        )
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = backgroundStory,
                    onValueChange = { backgroundStory = it },
                    label = { Text("Background Story") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            
            item {
                Text("Voice Settings", style = MaterialTheme.typography.titleMedium)
                
                Text("Pitch: ${voicePitch}")
                Slider(
                    value = voicePitch,
                    onValueChange = { voicePitch = it },
                    valueRange = 0.5f..1.5f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Speed: ${voiceSpeed}")
                Slider(
                    value = voiceSpeed,
                    onValueChange = { voiceSpeed = it },
                    valueRange = 0.5f..1.5f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Button(
                    onClick = {
                        val updatedPersona = CharacterPersona(
                            name = name,
                            personality = selectedPersonality,
                            speechStyle = selectedSpeechStyle,
                            backgroundStory = backgroundStory,
                            interests = persona.interests,
                            voiceSettings = VoiceSettings(
                                pitch = voicePitch,
                                speed = voiceSpeed
                            )
                        )
                        characterViewModel.updateCharacterPersona(updatedPersona)
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Save Changes")
                }
            }
            
            item {
                Button(
                    onClick = { characterViewModel.generateNewCharacter() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Generate New Character")
                }
            }
        }
    }
}
