package com.animeai.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animeai.app.model.Message
import com.animeai.app.model.Role
import kotlinx.coroutines.delay

/**
 * Chat interface for communication with the anime character
 */
@Composable
fun ChatInterface(
    messages: List<Message>,
    onTextInput: (String) -> Unit,
    onVoiceInput: () -> Unit,
    onCameraInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(modifier = modifier) {
        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                MessageItem(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text input field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("메시지를 입력하세요...") },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF2D3047),
                    focusedContainerColor = Color(0xFF2D3047),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onTextInput(inputText)
                            inputText = ""
                        }
                    }
                ),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onTextInput(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Voice input button
            IconButton(
                onClick = { onVoiceInput() },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF5762D5),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Voice Input",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Camera button for emotion detection
            IconButton(
                onClick = { onCameraInput() },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF8E6FE4),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Camera,
                    contentDescription = "Camera",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Individual message item in the chat
 */
@Composable
fun MessageItem(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUserMessage = message.role == Role.User
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        if (isUserMessage) {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Animated message appearance
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUserMessage) 16.dp else 4.dp,
                    bottomEnd = if (isUserMessage) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUserMessage) Color(0xFF5762D5) else Color(0xFF2D3047)
                ),
                modifier = Modifier.fillMaxWidth(if (isUserMessage) 0.8f else 0.8f)
            ) {
                Box(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
        
        if (!isUserMessage) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}