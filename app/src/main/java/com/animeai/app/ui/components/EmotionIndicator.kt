package com.animeai.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animeai.app.model.Emotion

/**
 * Displays the current emotion of the character
 */
@Composable
fun EmotionIndicator(
    emotion: Emotion,
    modifier: Modifier = Modifier
) {
    // Color animation based on emotion
    val backgroundColor by animateColorAsState(
        targetValue = getColorForEmotion(emotion),
        animationSpec = tween(300),
        label = "emotion_color"
    )
    
    Box(
        modifier = modifier
            .size(width = 100.dp, height = 36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emotion.displayName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
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