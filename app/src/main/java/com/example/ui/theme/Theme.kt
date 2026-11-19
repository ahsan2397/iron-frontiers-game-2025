package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val RivalDarkColorScheme = darkColorScheme(
    primary = PakistanEmerald,
    onPrimary = TopBarBg,
    primaryContainer = PakistanEmerald.copy(alpha = 0.2f),
    onPrimaryContainer = TextPrimary,
    secondary = HoverYellow,
    onSecondary = TopBarBg,
    secondaryContainer = KingGold.copy(alpha = 0.2f),
    onSecondaryContainer = TextPrimary,
    tertiary = SteelBlue,
    onTertiary = TextPrimary,
    error = WarCrimson,
    onError = TextPrimary,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SurfacePanel,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = HairlineBorder,
    outlineVariant = SurfaceHighlight
)

private val RivalShapes = Shapes(
    extraSmall = RoundedCornerShape(3.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(14.dp)
)

@Composable
fun RivalRegionsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RivalDarkColorScheme,
        typography = RivalTypography,
        shapes = RivalShapes,
        content = content
    )
}
