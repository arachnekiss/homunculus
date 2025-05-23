package com.animeai.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

/**
 * 애니메이션 배경 효과를 그리는 컴포넌트
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier
) {
    // 파티클 생성
    val particles = remember {
        List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 10f + 2f,
                color = particleColors[Random.nextInt(particleColors.size)],
                speed = Random.nextFloat() * 0.002f + 0.0005f
            )
        }
    }
    
    // 파티클 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_movement"
    )
    
    Box(modifier = modifier) {
        // 파티클 그리기
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 애니메이션 진행 상황에 따라 파티클 위치 업데이트
            particles.forEach { particle ->
                val x = (particle.x + (animationProgress * particle.speed * 20f)) % 1f
                val y = (particle.y + (animationProgress * particle.speed * 10f)) % 1f
                
                // 실제 캔버스 위치로 변환
                val xPos = x * size.width
                val yPos = y * size.height
                
                // 은은한 글로우 효과
                drawCircle(
                    color = particle.color.copy(alpha = 0.15f),
                    radius = particle.radius * 3,
                    center = Offset(xPos, yPos)
                )
                
                // 파티클 그리기
                drawCircle(
                    color = particle.color.copy(alpha = 0.5f),
                    radius = particle.radius,
                    center = Offset(xPos, yPos)
                )
            }
            
            // 배경 글로우 효과 추가
            drawLightBeams(animationProgress)
        }
    }
}

/**
 * 배경에 은은한 빛 효과 추가
 */
private fun DrawScope.drawLightBeams(progress: Float) {
    // 오른쪽 상단 빛 효과
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x105762D5), // 보라색 계열
                Color.Transparent
            ),
            center = Offset(size.width * 0.9f, size.height * 0.1f),
            radius = size.width * 0.8f
        ),
        size = size
    )
    
    // 왼쪽 하단 빛 효과
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x108E6FE4), // 파란색 계열
                Color.Transparent
            ),
            center = Offset(size.width * 0.1f, size.height * 0.9f),
            radius = size.width * 0.8f
        ),
        size = size
    )
    
    // 중앙 빛 효과 (시간에 따라 깜빡임)
    val pulseAlpha = (kotlin.math.sin(progress * 2 * kotlin.math.PI) * 0.03f + 0.05f).coerceIn(0.02f, 0.08f)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE4A5FF).copy(alpha = pulseAlpha), // 핑크색 계열
                Color.Transparent
            ),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.width * 0.7f
        ),
        size = size
    )
}

/**
 * 배경 파티클 데이터 클래스
 */
private data class Particle(
    val x: Float,          // 정규화된 x 위치 (0-1)
    val y: Float,          // 정규화된 y 위치 (0-1)
    val radius: Float,     // 파티클 반지름
    val color: Color,      // 파티클 색상
    val speed: Float       // 이동 속도
)

// 애니메이션에 어울리는 파스텔 톤 색상들
private val particleColors = listOf(
    Color(0x335762D5),  // 보라색
    Color(0x338E6FE4),  // 라벤더
    Color(0x33E4A5FF),  // 핑크
    Color(0x33FF98CA),  // 연한 핑크
    Color(0x33A5D8FF)   // 하늘색
)