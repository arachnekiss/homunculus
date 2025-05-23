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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.animeai.app.ui.theme.MicButtonColor
import com.animeai.app.ui.theme.CameraButtonColor
import com.animeai.app.ui.theme.UserBubbleColor
import com.animeai.app.ui.theme.CharacterBubbleColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animeai.app.model.Message
import com.animeai.app.model.Role
import kotlinx.coroutines.delay

/**
 * Chat interface for communication with the anime character
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Input 배경
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D3047)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 텍스트 입력 필드
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { 
                            Text(
                                "무엇이든 부탁하세요",
                                color = Color.White.copy(alpha = 0.6f)
                            ) 
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    onTextInput(inputText)
                                    inputText = ""
                                }
                            }
                        ),
                        maxLines = 1,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White
                        )
                    )
                    
                    // 전송 버튼 (텍스트가 있을 때만 표시)
                    AnimatedVisibility(
                        visible = inputText.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    onTextInput(inputText)
                                    inputText = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Send,
                                contentDescription = "전송",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // 음성 입력 버튼 (텍스트가 없을 때만 표시)
                    AnimatedVisibility(
                        visible = inputText.isBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = { onVoiceInput() }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = "음성 입력",
                                tint = MicButtonColor
                            )
                        }
                    }
                    
                    // 추가 기능 버튼 (+ 버튼)
                    IconButton(
                        onClick = { onCameraInput() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "더보기",
                            tint = CameraButtonColor
                        )
                    }
                }
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        // 캐릭터 메시지 프로필 사진 (캐릭터 메시지인 경우만)
        if (!isUserMessage) {
            // 캐릭터의 프로필 이미지 (원형)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.3f))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // 실제 구현에서는 캐릭터 이미지를 표시
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // 메시지 버블
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(300)) + 
                    slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(300)),
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
                    containerColor = if (isUserMessage) UserBubbleColor else CharacterBubbleColor
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Box(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column {
                        // 감정 표시 (캐릭터 메시지일 때만, 시스템 메시지 제외)
                        if (!isUserMessage && message.id?.startsWith("system_") != true) {
                            message.emotion?.let { emotion ->
                                Text(
                                    text = emotion.displayName,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                        
                        // 메시지 텍스트
                        Text(
                            text = message.content,
                            color = Color.White,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        // 미디어 첨부 여부에 따른 추가 UI (음성 메시지 등)
                        message.audioUrl?.let {
                            // 오디오 URL이 있으면 재생 버튼 표시
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "음성 메시지",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "음성 메시지",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}