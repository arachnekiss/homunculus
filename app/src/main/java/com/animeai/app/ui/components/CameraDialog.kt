package com.animeai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoodBad
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.animeai.app.model.Emotion
import com.animeai.app.ui.theme.CameraButtonColor

/**
 * 카메라 및 표정 선택 대화상자
 */
@Composable
fun CameraDialog(
    onDismiss: () -> Unit,
    onCameraCapture: () -> Unit,
    onGallerySelect: () -> Unit,
    onEmotionSelected: (Emotion) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D3047)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목 및 닫기 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "표정 선택",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = Color(0x33FFFFFF),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "닫기",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 카메라/갤러리 옵션
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconOptionItem(
                        icon = Icons.Rounded.CameraAlt,
                        label = "카메라",
                        onClick = {
                            onCameraCapture()
                            onDismiss()
                        }
                    )
                    
                    IconOptionItem(
                        icon = Icons.Rounded.Image,
                        label = "갤러리",
                        onClick = {
                            onGallerySelect()
                            onDismiss()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "또는 표정 직접 선택",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 감정 선택 옵션들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EmotionOptionItem(
                        icon = Icons.Rounded.Mood,
                        label = "행복",
                        emotion = Emotion.HAPPY,
                        onEmotionSelected = {
                            onEmotionSelected(it)
                            onDismiss()
                        }
                    )
                    
                    EmotionOptionItem(
                        icon = Icons.Rounded.MoodBad,
                        label = "화남",
                        emotion = Emotion.ANGRY,
                        onEmotionSelected = {
                            onEmotionSelected(it)
                            onDismiss()
                        }
                    )
                    
                    EmotionOptionItem(
                        icon = Icons.Rounded.SentimentDissatisfied,
                        label = "슬픔",
                        emotion = Emotion.SAD,
                        onEmotionSelected = {
                            onEmotionSelected(it)
                            onDismiss()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EmotionOptionItem(
                        icon = Icons.Rounded.SentimentSatisfied,
                        label = "놀람",
                        emotion = Emotion.SURPRISED,
                        onEmotionSelected = {
                            onEmotionSelected(it)
                            onDismiss()
                        }
                    )
                    
                    EmotionOptionItem(
                        icon = Icons.Rounded.SentimentVeryDissatisfied,
                        label = "당황",
                        emotion = Emotion.EMBARRASSED,
                        onEmotionSelected = {
                            onEmotionSelected(it)
                            onDismiss()
                        }
                    )
                    
                    // 빈 공간 (두 개의 감정만 표시)
                    Box(modifier = Modifier.width(80.dp))
                }
            }
        }
    }
}

/**
 * 카메라/갤러리 옵션 버튼
 */
@Composable
fun IconOptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = CameraButtonColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

/**
 * 감정 선택 버튼
 */
@Composable
fun EmotionOptionItem(
    icon: ImageVector,
    label: String,
    emotion: Emotion,
    onEmotionSelected: (Emotion) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { 
                isPressed = true
                onEmotionSelected(emotion)
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isPressed) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}