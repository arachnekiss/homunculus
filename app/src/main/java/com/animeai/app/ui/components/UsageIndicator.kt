package com.animeai.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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

/**
 * Displays the user's remaining credits
 */
@Composable
fun UsageIndicator(
    credits: Float,
    modifier: Modifier = Modifier
) {
    // Animate the credits value for smoother display
    val animatedCredits by animateFloatAsState(
        targetValue = credits,
        animationSpec = tween(500),
        label = "credits_animation"
    )
    
    // Progress is based on a relative scale (assuming 100 is full)
    val progress = (animatedCredits / 100f).coerceIn(0f, 1f)
    
    // Color changes based on remaining credits
    val creditColor = when {
        credits > 50 -> Color(0xFF4CAF50)  // Green
        credits > 20 -> Color(0xFFFFC107)  // Yellow/Amber
        else -> Color(0xFFF44336)          // Red
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x662D3047))
            .padding(12.dp)
    ) {
        Column {
            // Credits title and value
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "크레딧",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = String.format("%.1f", animatedCredits),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = creditColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Credits progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = creditColor,
                trackColor = Color(0xFF424242)
            )
            
            // Low credit warning
            if (credits < 20) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "크레딧이 부족합니다",
                    fontSize = 12.sp,
                    color = Color(0xFFF44336),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}