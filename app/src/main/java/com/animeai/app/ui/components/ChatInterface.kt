package com.animeai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.animeai.app.model.Message
import com.animeai.app.model.Role
import com.animeai.app.ui.theme.MessageBubbleAI
import com.animeai.app.ui.theme.MessageBubbleUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInterface(
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onMicToggle: (Boolean) -> Unit,
    onCameraToggle: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Message List (chat history)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
        }
        
        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera button
            IconButton(onClick = onCameraToggle) {
                Icon(Icons.Default.Camera, contentDescription = "Camera")
            }
            
            // Text input field
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button
            if (messageText.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            } else {
                // Microphone button
                IconButton(
                    onClick = {
                        isRecording = !isRecording
                        onMicToggle(isRecording)
                    }
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (isRecording) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.role == Role.User
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isUser) MessageBubbleUser else MessageBubbleAI
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
    )
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
