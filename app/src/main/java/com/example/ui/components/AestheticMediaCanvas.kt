package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

@Composable
fun AestheticMediaCanvas(presetName: String, modifier: Modifier = Modifier) {
    // Animation tick for movement
    val infiniteTransition = rememberInfiniteTransition(label = "canvas_anim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        when (presetName) {
            "Mountain Sunrise" -> {
                drawMountainSunrise(waveOffset)
            }
            "Makkah Mosque" -> {
                drawMakkahMosque(waveOffset)
            }
            "Peaceful Cosmos" -> {
                drawPeacefulCosmos(waveOffset)
            }
            "Ocean Ripples" -> {
                drawOceanRipples(waveOffset)
            }
            else -> {
                // Classic Warm Gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBC4DF), Color(0xFFAEC6CF))
                    ),
                    size = size
                )
            }
        }
    }
}

private fun DrawScope.drawMountainSunrise(animVal: Float) {
    // 1. Sky Gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF8E53), // Sunrise orange
                Color(0xFFFE6B8B), // Rose pink
                Color(0xFFFBC4DF)  // Pastel pink bottom
            )
        ),
        size = size
    )

    // 2. Glowing Sun
    drawCircle(
        color = Color(0xFFFFF9C4),
        radius = 110f,
        center = Offset(size.width / 2, size.height * 0.45f)
    )

    // 3. Mountains Path Back
    val pathBack = Path().apply {
        moveTo(0f, size.height * 0.7f)
        cubicTo(
            size.width * 0.3f, size.height * 0.55f,
            size.width * 0.7f, size.height * 0.75f,
            size.width, size.height * 0.65f
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(pathBack, color = Color(0xFF81C784).copy(alpha = 0.6f))

    // 4. Mountains Path Front
    val pathFront = Path().apply {
        moveTo(0f, size.height * 0.82f)
        cubicTo(
            size.width * 0.25f, size.height * 0.72f,
            size.width * 0.6f, size.height * 0.88f,
            size.width, size.height * 0.75f
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(pathFront, color = Color(0xFF388E3C).copy(alpha = 0.85f))
}

private fun DrawScope.drawMakkahMosque(animVal: Float) {
    // 1. Midnight Starry Indigo sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B),
                Color(0xFF3A4B59)
            )
        ),
        size = size
    )

    // 2. Stars
    val starCoordinates = listOf(
        Offset(200f, 150f), Offset(150f, 400f), Offset(400f, 250f), Offset(600f, 100f),
        Offset(800f, 300f), Offset(900f, 500f), Offset(50f, 600f), Offset(650f, 700f)
    )
    starCoordinates.forEach { star ->
        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 5f, center = star)
    }

    // 3. Glowing Crescent Moon
    drawCircle(
        color = Color(0xFFFFF59D),
        radius = 80f,
        center = Offset(size.width * 0.8f, size.height * 0.2f)
    )
    drawCircle( // Eclipse out to make crescent
        color = Color(0xFF0F172A),
        radius = 80f,
        center = Offset(size.width * 0.8f - 25f, size.height * 0.2f)
    )

    // 4. Central Minimalist Golden Kaaba outline representing peaceful spiritual mood
    val kaabaWidth = size.width * 0.45f
    val kaabaHeight = kaabaWidth * 0.9f
    val startX = (size.width - kaabaWidth) / 2
    val startY = size.height * 0.55f

    // Kaaba body
    drawRect(
        color = Color(0xFF090F1B),
        topLeft = Offset(startX, startY),
        size = Size(kaabaWidth, kaabaHeight)
    )

    // Golden Kiswah belt
    drawRect(
        color = Color(0xFFFFD54F),
        topLeft = Offset(startX, startY + kaabaHeight * 0.2f),
        size = Size(kaabaWidth, kaabaHeight * 0.08f)
    )
}

private fun DrawScope.drawPeacefulCosmos(animVal: Float) {
    val safeRadius = if (size.width > 0f) size.width else 100f
    // 1. Deep Space Plum theme
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF4A1525), // core violet
                Color(0xFF1E293B), // intermediate
                Color(0xFF0A0F14)  // outer dark
            ),
            center = Offset(size.width / 2, size.height / 2),
            radius = safeRadius
        ),
        size = size
    )

    // 2. Cosmic Nebulas
    val sinVal = sin(Math.toRadians(animVal.toDouble())).toFloat()
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE89BB6).copy(alpha = 0.2f), Color.Transparent),
        ),
        radius = 400f + sinVal * 40f,
        center = Offset(size.width * 0.4f, size.height * 0.4f)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF90CAF9).copy(alpha = 0.15f), Color.Transparent),
        ),
        radius = 500f - sinVal * 30f,
        center = Offset(size.width * 0.7f, size.height * 0.6f)
    )

    // 3. Shimmering cosmic particles
    val particles = listOf(
        Offset(300f, 500f), Offset(700f, 200f), Offset(550f, 800f),
        Offset(250f, 1000f), Offset(600f, 400f)
    )
    particles.forEachIndexed { i, offset ->
        val osc = sin(Math.toRadians((animVal + i * 50).toDouble())).toFloat()
        drawCircle(
            color = Color.White.copy(alpha = 0.4f + osc * 0.3f),
            radius = 8f + osc * 3f,
            center = offset
        )
    }
}

private fun DrawScope.drawOceanRipples(animVal: Float) {
    // 1. Calming blue-green ocean
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE6EFF5),
                Color(0xFFAEC6CF),
                Color(0xFF7D9BB0)
            )
        ),
        size = size
    )

    // 2. Continuous ocean wave strokes using Math offset
    val steps = 5
    for (i in 0 until steps) {
        val yOffset = size.height * 0.4f + (i * 120f)
        val path = Path().apply {
            moveTo(0f, yOffset)
            for (x in 0..size.width.toInt() step 20) {
                val cycle = (x / size.width) * 2 * Math.PI
                val y = sin(cycle + Math.toRadians(animVal.toDouble() + i * 60)).toFloat() * 32f
                lineTo(x.toFloat(), yOffset + y)
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.12f * (i + 1))
        )
    }

    // Centered Concentric Ripple Circles representing calm breathing wave
    val animRad = (animVal % 120f) / 120f
    drawCircle(
        color = Color.White.copy(alpha = 1f - animRad),
        radius = animRad * 250f,
        center = Offset(size.width / 2, size.height * 0.4f),
        style = Stroke(width = 6f)
    )
    drawCircle(
        color = Color.White.copy(alpha = (1f - animRad) * 0.5f),
        radius = ((animRad + 0.5f) % 1f) * 250f,
        center = Offset(size.width / 2, size.height * 0.4f),
        style = Stroke(width = 4f)
    )
}
