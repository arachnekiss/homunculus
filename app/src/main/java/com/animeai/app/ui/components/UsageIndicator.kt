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
    // 부드러운 크레딧 값 변화를 위한 애니메이션
    val animatedCredits by animateFloatAsState(
        targetValue = credits,
        animationSpec = tween(700),
        label = "credits_animation"
    )
    
    // 진행률은 상대적인 스케일 기준 (100이 최대)
    val progress = (animatedCredits / 100f).coerceIn(0f, 1f)
    
    // 남은 크레딧에 따른 색상 변화
    val creditColor = when {
        credits > 50 -> Color(0xFF4CAF50)  // 많음 - 녹색
        credits > 20 -> Color(0xFFFFC107)  // 중간 - 노란색/황색
        else -> Color(0xFFF44336)          // 적음 - 빨간색
    }
    
    // 저 크레딧 경고 애니메이션
    var pulseOpacity by remember { mutableStateOf(1f) }
    
    // 크레딧이 적을 때 깜빡이는 경고 효과
    LaunchedEffect(key1 = credits < 20) {
        if (credits < 20) {
            while (true) {
                // 깜빡임 효과 (1.0f -> 0.6f -> 1.0f)
                for (i in 0..5) {
                    pulseOpacity = 1f - (i / 5f * 0.4f)
                    delay(100)
                }
                for (i in 5 downTo 0) {
                    pulseOpacity = 1f - (i / 5f * 0.4f)
                    delay(100)
                }
                delay(1000) // 깜빡임 사이 간격
            }
        } else {
            pulseOpacity = 1f
        }
    }
    
    // 디자인 개선된 카드 형태
    Card(
        modifier = modifier
            .graphicsLayer {
                // 크레딧이 적을 때 깜빡이는 효과
                alpha = if (credits < 20) pulseOpacity else 1f
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3047).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 제목과 크레딧 표시 행
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 좌측 제목 부분
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 아이콘 애니메이션 (로테이션)
                    val iconRotation by animateFloatAsState(
                        targetValue = if (credits < 20) 10f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "icon_rotation"
                    )
                    
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                rotationZ = iconRotation
                            }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "크레딧",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                
                // 우측 크레딧 값
                Text(
                    text = String.format("%.1f", animatedCredits),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = creditColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 크레딧 진행 표시줄
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = creditColor,
                trackColor = Color(0xFF424242).copy(alpha = 0.5f)
            )
            
            // 적은 크레딧 경고
            AnimatedVisibility(
                visible = credits < 20,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "크레딧이 부족합니다",
                        fontSize = 12.sp,
                        color = Color(0xFFF44336)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // 충전 버튼
                    Text(
                        text = "충전",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5762D5),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF5762D5).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}