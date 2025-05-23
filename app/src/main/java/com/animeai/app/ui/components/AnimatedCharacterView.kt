package com.animeai.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.animeai.app.model.AnimeCharacter
import com.animeai.app.model.Emotion
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Composable that displays an animated anime character with expressions
 */
@Composable
fun AnimatedCharacterView(
    character: AnimeCharacter,
    currentEmotion: Emotion,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Get the appropriate expression image based on emotion
    val imageUrl = character.getExpressionForEmotion(currentEmotion)
    
    // 이미지 로딩 상태
    var isImageLoading by remember { mutableStateOf(true) }
    
    // 이전 감정과 현재 감정 상태 저장
    var prevEmotion by remember { mutableStateOf(currentEmotion) }
    var isEmotionChanging by remember { mutableStateOf(false) }
    
    // 감정 변화 감지
    LaunchedEffect(currentEmotion) {
        if (prevEmotion != currentEmotion) {
            isEmotionChanging = true
            delay(300)  // 감정 변화 애니메이션 시간
            prevEmotion = currentEmotion
            isEmotionChanging = false
        }
    }
    
    // 상태 변수들
    var isBlinking by remember { mutableStateOf(false) }
    var isBreathing by remember { mutableStateOf(true) }
    
    // Random blinking effect
    LaunchedEffect(key1 = character.id) {
        while (true) {
            // 캐릭터가 대화 중일 때는 덜 깜빡임
            val waitTime = if (isSpeaking) 
                Random.nextInt(4000, 10000).toLong()
            else 
                Random.nextInt(2000, 6000).toLong()
            
            delay(waitTime)
            isBlinking = true
            delay(150)  // 깜빡임 시간 (더 짧게)
            isBlinking = false
        }
    }
    
    // 호흡 효과 (더 자연스러운 애니메이션)
    val infiniteTransition = rememberInfiniteTransition(label = "animations")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )
    
    // 미세한 흔들림 효과 (더 생동감 있는 느낌)
    val swayOffset by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway_offset"
    )
    
    // 말하기 애니메이션 (입 움직임)
    val talkingValue by animateFloatAsState(
        targetValue = if (isSpeaking) 1f else 0f,
        animationSpec = tween(200),
        label = "talking"
    )
    
    // 감정 변화 애니메이션
    val emotionChangeScale by animateFloatAsState(
        targetValue = if (isEmotionChanging) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "emotion_change"
    )
    
    // 눈 깜빡임 애니메이션
    val eyeScale by animateFloatAsState(
        targetValue = if (isBlinking) 0.1f else 1f,
        animationSpec = tween(100),
        label = "blinking"
    )
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 캐릭터 배경 효과 (은은한 글로우)
        Box(
            modifier = Modifier
                .size(300.dp)
                .alpha(0.2f)
                .blur(20.dp)
                .background(
                    color = when (currentEmotion) {
                        Emotion.HAPPY -> Color(0xFF9DDAFF)
                        Emotion.ANGRY -> Color(0xFFFF9D9D)
                        Emotion.SAD -> Color(0xFF9DBBFF)
                        Emotion.SURPRISED -> Color(0xFFFFEE9D)
                        Emotion.EMBARRASSED -> Color(0xFFFFB9D1)
                        else -> Color(0xFFE0E0E0)
                    },
                    shape = CircleShape
                )
        )
        
        // 이미지 로딩 인디케이터
        if (isImageLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // 메인 캐릭터 이미지와 애니메이션
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build()
        )
        
        // 이미지 로딩 상태 체크
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                isImageLoading = true
            }
            is AsyncImagePainter.State.Success -> {
                isImageLoading = false
            }
            else -> {
                isImageLoading = false
            }
        }
        
        Image(
            painter = painter,
            contentDescription = "애니메이션 캐릭터",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize(0.9f)
                .scale(breathingScale * emotionChangeScale)
                .graphicsLayer {
                    // 말할 때 미세한 움직임
                    if (isSpeaking) {
                        translationY = (talkingValue * 1.5f) * Random.nextFloat()
                    }
                    
                    // 자연스러운 흔들림 (미세한 회전)
                    rotationZ = swayOffset * 0.3f
                    
                    // 감정에 따른 특별 효과
                    when (currentEmotion) {
                        Emotion.SURPRISED -> {
                            scaleX = 1.02f
                            scaleY = 1.02f
                        }
                        Emotion.ANGRY -> {
                            translationY = -2f
                        }
                        Emotion.SAD -> {
                            translationY = 2f
                        }
                        else -> { /* 기본 상태 유지 */ }
                    }
                }
                .alpha(if (isEmotionChanging) 0.9f else 1f)
        )
        
        // 감정 상태 표시 (디버깅용, 실제 앱에서는 주석 처리)
        /*
        Text(
            text = "감정: ${currentEmotion.displayName}, 말하는 중: $isSpeaking",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            color = Color.White
        )
        */
    }
}