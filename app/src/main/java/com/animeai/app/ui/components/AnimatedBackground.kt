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
 * Animated background with floating particles and gradients
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier
) {
    // Create particles
    val particles = remember {
        List(40) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 12f + 2f,
                color = particleColors[Random.nextInt(particleColors.size)],
                speed = Random.nextFloat() * 0.003f + 0.001f
            )
        }
    }
    
    // Animation for particles
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_movement"
    )
    
    Box(modifier = modifier) {
        // Draw the particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Update particle positions based on animation progress
            particles.forEach { particle ->
                // Calculate new position based on time
                val x = (particle.x + (animationProgress * particle.speed * 10f)) % 1f
                val y = (particle.y + (animationProgress * particle.speed * 5f)) % 1f
                
                // Convert normalized coordinates to actual canvas positions
                val xPos = x * size.width
                val yPos = y * size.height
                
                // Apply a glow effect
                drawCircle(
                    color = particle.color.copy(alpha = 0.2f),
                    radius = particle.radius * 2,
                    center = Offset(xPos, yPos)
                )
                
                // Draw the particle
                drawCircle(
                    color = particle.color,
                    radius = particle.radius,
                    center = Offset(xPos, yPos)
                )
            }
            
            // Draw some soft light beams
            drawLightBeams(animationProgress)
        }
    }
}

/**
 * Draw soft anime-style light beams in the background
 */
private fun DrawScope.drawLightBeams(progress: Float) {
    // Top-right light beam
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x155762D5),
                Color.Transparent
            ),
            center = Offset(size.width, 0f),
            radius = size.width * 0.7f
        ),
        size = size
    )
    
    // Bottom-left light beam
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x108E6FE4),
                Color.Transparent
            ),
            center = Offset(0f, size.height),
            radius = size.width * 0.7f
        ),
        size = size
    )
}

/**
 * Data class representing a background particle
 */
private data class Particle(
    val x: Float,          // Normalized x position (0-1)
    val y: Float,          // Normalized y position (0-1)
    val radius: Float,     // Radius of the particle
    val color: Color,      // Color of the particle
    val speed: Float       // Movement speed
)

// Particle colors in anime-inspired pastel tones
private val particleColors = listOf(
    Color(0x335762D5),  // Soft blue
    Color(0x338E6FE4),  // Soft purple
    Color(0x33E4A5FF),  // Soft pink
    Color(0x33FF98CA),  // Light pink
    Color(0x33A5D8FF)   // Light blue
)