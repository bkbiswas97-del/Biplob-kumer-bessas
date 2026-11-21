package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = OnForestGreenContainer,
    secondary = HarvestGoldSecondary,
    onSecondary = Color.White,
    secondaryContainer = HarvestGoldContainer,
    onSecondaryContainer = OnHarvestGoldContainer,
    tertiary = TerracottaTertiary,
    onTertiary = Color.White,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = OnTerracottaContainer,
    background = WarmCreamBackground,
    onBackground = CharcoalInk,
    surface = WarmSurface,
    onSurface = CharcoalInk,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = MutedStone,
    outline = SubtleBorder,
    error = StatusDanger,
    onError = Color.White,
    errorContainer = StatusDangerBg,
    onErrorContainer = StatusDanger
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryGreen,
    onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0xFF14532D),
    onPrimaryContainer = ForestGreenContainer,
    secondary = DarkHarvestGold,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = HarvestGoldContainer,
    tertiary = Color(0xFFFB923C),
    onTertiary = Color(0xFF431407),
    background = DarkForestBackground,
    onBackground = Color(0xFFF5F5F4),
    surface = DarkForestSurface,
    onSurface = Color(0xFFF5F5F4),
    surfaceVariant = DarkForestSurfaceVariant,
    onSurfaceVariant = Color(0xFFD6D3D1),
    outline = Color(0xFF334139),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PuratonTractorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
