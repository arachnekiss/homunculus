package com.animeai.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animeai.app.model.Emotion
import kotlinx.coroutines.delay

/**
 * Displays the current emotion of the character
 */
@Composable
fun EmotionIndicator(
    emotion: Emotion,
    modifier: Modifier = Modifier
) {
    // 감정에 따라 색상 애니메이션
    val backgroundColor by animateColorAsState(
        targetValue = getColorForEmotion(emotion),
        animationSpec = tween(300),
        label = "emotion_color"
    )
    
    // 아이콘 회전 애니메이션
    val rotation by animateFloatAsState(
        targetValue = when (emotion) {
            Emotion.SURPRISED -> 10f
            Emotion.ANGRY -> -5f
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "emotion_rotation"
    )
    
    // 아이콘 크기 펄스 애니메이션 (HAPPY일 때만)
    var pulseDirection by remember { mutableStateOf(1f) }
    var pulseScale by remember { mutableStateOf(1f) }
    
    // HAPPY일 때만 펄스 애니메이션 적용
    LaunchedEffect(emotion) {
        if (emotion == Emotion.HAPPY) {
            while (true) {
                // 크기 증가/감소를 반복
                val targetScale = if (pulseDirection > 0) 1.1f else 1.0f
                
                // 작은 크기 변화를 부드럽게 애니메이션
                val steps = 5
                for (i in 1..steps) {
                    val progress = i.toFloat() / steps
                    pulseScale = if (pulseDirection > 0) {
                        1f + (targetScale - 1f) * progress
                    } else {
                        targetScale + (1f - targetScale) * progress
                    }
                    delay(25)
                }
                
                pulseDirection *= -1f
                delay(300)
            }
        } else {
            pulseScale = 1f
        }
    }
    
    // 감정 표시 컨테이너
    Card(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
                scaleX = if (emotion == Emotion.HAPPY) pulseScale else 1f
                scaleY = if (emotion == Emotion.HAPPY) pulseScale else 1f
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 감정에 따른 아이콘
            Icon(
                imageVector = when (emotion) {
                    Emotion.HAPPY -> Icons.Rounded.ThumbUp
                    Emotion.SAD -> Icons.Rounded.SentimentDissatisfied
                    Emotion.ANGRY -> Icons.Rounded.Bolt
                    Emotion.SURPRISED -> Icons.Rounded.Star
                    Emotion.EMBARRASSED -> Icons.Rounded.Favorite
                    else -> Icons.Rounded.Face
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 감정 텍스트
            Text(
                text = emotion.displayName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Returns a color associated with the given emotion
 */
private fun getColorForEmotion(emotion: Emotion): Color {
    return when (emotion) {
        Emotion.HAPPY -> Color(0xFF4CAF50)        // Green
        Emotion.SAD -> Color(0xFF2196F3)          // Blue
        Emotion.ANGRY -> Color(0xFFF44336)        // Red
        Emotion.SURPRISED -> Color(0xFFFF9800)    // Orange
        Emotion.EMBARRASSED -> Color(0xFFE91E63)  // Pink
        Emotion.NEUTRAL -> Color(0xFF9E9E9E)      // Gray
    }
}