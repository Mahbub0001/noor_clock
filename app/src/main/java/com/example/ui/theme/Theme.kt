package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AppThemeName(val displayName: String) {
    FROSTED_GLASS("Frosted Glass"),
    PINK_PASTEL("Pink Pastel"),
    GREEN_NATURAL("Green Natural"),
    BLUE_SKY("Blue Sky"),
    DARK_MINIMAL("Dark Minimal")
}

val PinkColorScheme = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = PinkTertiary,
    background = PinkBackground,
    surface = PinkSurface,
    onPrimary = PinkOnPrimary,
    onSecondary = PinkOnSecondary,
    onBackground = PinkOnPrimary,
    onSurface = PinkOnPrimary
)

val GreenColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = GreenTertiary,
    background = GreenBackground,
    surface = GreenSurface,
    onPrimary = GreenOnPrimary,
    onSecondary = GreenOnSecondary,
    onBackground = GreenOnPrimary,
    onSurface = GreenOnPrimary
)

val BlueColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = BlueBackground,
    surface = BlueSurface,
    onPrimary = BlueOnPrimary,
    onSecondary = BlueOnSecondary,
    onBackground = BlueOnPrimary,
    onSurface = BlueOnPrimary
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = Color.White,
    onSurface = Color.White
)

val FrostedColorScheme = lightColorScheme(
    primary = FrostedPrimary,
    secondary = FrostedSecondary,
    tertiary = FrostedTertiary,
    background = FrostedBackground,
    surface = FrostedSurface,
    onPrimary = FrostedOnPrimary,
    onSecondary = FrostedOnSecondary,
    onBackground = FrostedOnSecondary,
    onSurface = FrostedOnSecondary
)

@Composable
fun MeshBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDF2F8)) // Base pink canvas background
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w > 0f && h > 0f) {
                val r1 = w * 0.8f
                // Circle 1: top-left (pink-200 / 0xFFFBC4DF)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFBC4DF).copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(w * -0.1f, h * -0.1f),
                        radius = r1
                    ),
                    radius = r1,
                    center = Offset(w * -0.1f, h * -0.1f)
                )

                val r2 = w * 0.7f
                // Circle 2: bottom-right (blue-100 / 0xFFD0E1FD)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD0E1FD).copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(w * 1.1f, h * 0.9f),
                        radius = r2
                    ),
                    radius = r2,
                    center = Offset(w * 1.1f, h * 0.9f)
                )

                val r3 = w * 0.6f
                // Circle 3: mid-right/center (purple-200 / 0xFFE8D0FD)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE8D0FD).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(w * 0.9f, h * 0.4f),
                        radius = r3
                    ),
                    radius = r3,
                    center = Offset(w * 0.9f, h * 0.4f)
                )
            }
        }
    }
}

@Composable
fun NoorClockTheme(
    themeName: AppThemeName = AppThemeName.FROSTED_GLASS,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        AppThemeName.PINK_PASTEL -> PinkColorScheme
        AppThemeName.GREEN_NATURAL -> GreenColorScheme
        AppThemeName.BLUE_SKY -> BlueColorScheme
        AppThemeName.DARK_MINIMAL -> DarkColorScheme
        AppThemeName.FROSTED_GLASS -> FrostedColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            if (themeName == AppThemeName.FROSTED_GLASS) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MeshBackground()
                    content()
                }
            } else {
                content()
            }
        }
    )
}

