package com.ultinote.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unified Liquid Glass Theme Specification.
 * Enforces consistent backdrop translucency, glassmorphic gradients,
 * and subtle edge-glow effects across cards, sidebars, docks, and overlays.
 */
data class LiquidGlassStyle(
    val surfaceBrush: Brush,
    val borderBrush: Brush,
    val edgeGlowColor: Color,
    val cardRadius: Dp = 22.dp,
    val sidebarRadius: Dp = 28.dp,
    val pillRadius: Dp = 50.dp,
    val elevation: Dp = 6.dp,
    val highlightAlpha: Float = 0.22f,
    val ambientGlowRadius: Dp = 8.dp
)

val LocalLiquidGlassTheme = compositionLocalOf {
    LiquidGlassStyle(
        surfaceBrush = Brush.verticalGradient(
            colors = listOf(Color(0xDDFBF9F3), Color(0xCCF3EFE6))
        ),
        borderBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.70f),
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.45f)
            )
        ),
        edgeGlowColor = Color(0x403F664F),
        cardRadius = 22.dp,
        sidebarRadius = 28.dp,
        pillRadius = 50.dp,
        elevation = 6.dp
    )
}

object LiquidGlassTheme {
    val current: LiquidGlassStyle
        @Composable
        @ReadOnlyComposable
        get() = LocalLiquidGlassTheme.current
}

/**
 * Centralized theme provider for the Liquid Glass aesthetic.
 * Automatically derives harmonious glass brushes, specular highlight edges,
 * and subtle glow tints from the active Komorebi palette.
 */
@Composable
fun LiquidGlassThemeProvider(
    palette: KomorebiAestheticPalette = LocalKomorebiPalette.current,
    content: @Composable () -> Unit
) {
    val primary = palette.colorScheme.primary
    val isDark = palette.themeOption == com.ultinote.app.data.model.AppThemeOption.OBSIDIAN_DARK

    val surfaceGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xEE1E242B),
                Color(0xDD15181E),
                Color(0xEE12151A)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                palette.glassSurface.copy(alpha = 0.88f),
                palette.glassBackground.copy(alpha = 0.75f),
                palette.glassSurface.copy(alpha = 0.92f)
            )
        )
    }

    val specularBorder = Brush.linearGradient(
        colors = listOf(
            palette.glassBorder.copy(alpha = if (isDark) 0.35f else 0.80f),
            palette.glassBorder.copy(alpha = if (isDark) 0.08f else 0.18f),
            primary.copy(alpha = if (isDark) 0.28f else 0.30f),
            palette.glassBorder.copy(alpha = if (isDark) 0.25f else 0.60f)
        )
    )

    val edgeGlow = primary.copy(alpha = if (isDark) 0.35f else 0.18f)

    val style = LiquidGlassStyle(
        surfaceBrush = surfaceGradient,
        borderBrush = specularBorder,
        edgeGlowColor = edgeGlow,
        cardRadius = 22.dp,
        sidebarRadius = 28.dp,
        pillRadius = 50.dp,
        elevation = if (isDark) 8.dp else 6.dp,
        highlightAlpha = if (isDark) 0.12f else 0.28f,
        ambientGlowRadius = 10.dp
    )

    CompositionLocalProvider(
        LocalLiquidGlassTheme provides style
    ) {
        content()
    }
}

/**
 * Modifier that applies the unified Liquid Glass aesthetic (Apple WWDC25-inspired):
 * 1. Ambient edge-glow shadow (chromatic, soft)
 * 2. Shape clip (continuous rounded corners)
 * 3. Translucent frosted gradient (light refracts top-left, denser bottom)
 * 4. Specular highlight border (bright top, soft sides, tinted bottom)
 * 5. Inner top highlight line for that wet-glass look
 */
fun Modifier.liquidGlassEffect(
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 6.dp,
    customSurfaceBrush: Brush? = null,
    customBorderBrush: Brush? = null,
    glowColor: Color? = null
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        spotColor = glowColor ?: Color(0x33000000),
        ambientColor = glowColor ?: Color(0x22000000)
    )
    .clip(shape)
    .background(
        customSurfaceBrush ?: Brush.verticalGradient(
            listOf(
                Color(0xD6FFFFFF),
                Color(0xB8F4F4F4),
                Color(0xCCFFFFFF)
            )
        )
    )
    .border(
        width = 1.2.dp,
        brush = customBorderBrush ?: Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f), // top specular
                Color.White.copy(alpha = 0.25f), // mid fade
                Color.White.copy(alpha = 0.10f), // side
                Color.White.copy(alpha = 0.55f)  // bottom bounce
            )
        ),
        shape = shape
    )

/**
 * Apple-style pill toolbar glass: extra translucent, high specular, saturated tint.
 * Use for top bars, docks, side rails.
 */
fun Modifier.applePillGlass(
    shape: Shape = RoundedCornerShape(50.dp),
    tint: Color = Color.White,
    isDark: Boolean = false
): Modifier = this
    .shadow(10.dp, shape, spotColor = Color.Black.copy(alpha = 0.18f), ambientColor = Color.Black.copy(alpha = 0.10f))
    .clip(shape)
    .background(
        if (isDark) Brush.verticalGradient(
            listOf(
                Color(0xE62A2F38),
                Color(0xD61B1F26)
            )
        ) else Brush.verticalGradient(
            listOf(
                tint.copy(alpha = 0.72f),
                Color.White.copy(alpha = 0.55f),
                tint.copy(alpha = 0.68f)
            )
        )
    )
    .border(
        1.dp,
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.28f else 0.9f),
                Color.White.copy(alpha = 0.15f),
                tint.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.5f)
            )
        ),
        shape
    )
