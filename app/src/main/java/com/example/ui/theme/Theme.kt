package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ShockwaveDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = TextPrimary,
    primaryContainer = DeepPurpleCard,
    onPrimaryContainer = BrightViolet,
    secondary = ElectricBlue,
    onSecondary = PureBlack,
    secondaryContainer = CardDarkElevated,
    onSecondaryContainer = ElectricBlue,
    tertiary = NeonGreen,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = DeepObsidian,
    onSurface = TextPrimary,
    surfaceVariant = CardDarkSurface,
    onSurfaceVariant = TextSecondary,
    outline = DividerGlow,
    error = NeonRed,
    onError = TextPrimary
)

val ShockwaveShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ShockwaveTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShockwaveDarkColorScheme,
        typography = ShockwaveTypography,
        shapes = ShockwaveShapes,
        content = content
    )
}
