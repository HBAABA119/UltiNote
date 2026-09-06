package com.ultinote.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS-inspired native design system: colors, typography, spacing, radii, elevation.
 */
object NativeDesignSystem {
    // Palette - light
    val AccentBlue = Color(0xFF007AFF)
    val AccentGreen = Color(0xFF34C759)
    val AccentIndigo = Color(0xFF5856D6)
    val AccentOrange = Color(0xFFFF9500)
    val AccentPink = Color(0xFFFF2D55)
    val AccentPurple = Color(0xFFAF52DE)
    val AccentRed = Color(0xFFFF3B30)
    val AccentTeal = Color(0xFF5AC8FA)
    val AccentYellow = Color(0xFFFFCC00)

    // Neutrals - light
    val SystemGray = Color(0xFF8E8E93)
    val SystemGray2 = Color(0xFFAEAEB2)
    val SystemGray3 = Color(0xFFC7C7CC)
    val SystemGray4 = Color(0xFFD1D1D6)
    val SystemGray5 = Color(0xFFE5E5EA)
    val SystemGray6 = Color(0xFFF2F2F7)
    val SystemBackground = Color(0xFFFFFFFF)
    val SystemGroupedBackground = Color(0xFFF2F2F7)

    // Neutrals - dark
    val DarkBackground = Color(0xFF000000)
    val DarkGroupedBackground = Color(0xFF1C1C1E)
    val DarkElevated = Color(0xFF2C2C2E)
    val DarkGray = Color(0xFF8E8E93)
    val DarkSeparator = Color(0xFF38383A)

    val LightColors = androidx.compose.material3.lightColorScheme(
        primary = AccentBlue,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD6E9FF),
        onPrimaryContainer = Color(0xFF001A33),
        secondary = AccentIndigo,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE3E1FF),
        onSecondaryContainer = Color(0xFF17005C),
        tertiary = AccentTeal,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFC8F4FF),
        onTertiaryContainer = Color(0xFF001F26),
        background = SystemBackground,
        onBackground = Color(0xFF1A1C1E),
        surface = SystemBackground,
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = SystemGray5,
        onSurfaceVariant = Color(0xFF43474E),
        outline = SystemGray3,
        outlineVariant = SystemGray4,
        error = AccentRed,
        onError = Color.White,
    )

    val DarkColors = androidx.compose.material3.darkColorScheme(
        primary = Color(0xFF9ECAFF),
        onPrimary = Color(0xFF003259),
        primaryContainer = Color(0xFF00497D),
        onPrimaryContainer = Color(0xFFD1E4FF),
        secondary = Color(0xFFC3C3FF),
        onSecondary = Color(0xFF25005A),
        secondaryContainer = Color(0xFF3B2C8F),
        onSecondaryContainer = Color(0xFFE3E1FF),
        tertiary = Color(0xFF86D2E5),
        onTertiary = Color(0xFF00363F),
        tertiaryContainer = Color(0xFF004E5B),
        onTertiaryContainer = Color(0xFFC8F4FF),
        background = DarkBackground,
        onBackground = Color(0xFFE3E2E6),
        surface = DarkBackground,
        onSurface = Color(0xFFE3E2E6),
        surfaceVariant = DarkElevated,
        onSurfaceVariant = Color(0xFFC4C6CD),
        outline = DarkSeparator,
        outlineVariant = Color(0xFF43474E),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
    )

    // Typography
    val Typography = Typography(
        displayLarge = TextStyle(fontWeight = FontWeight.Light, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Light, fontSize = 45.sp, lineHeight = 52.sp),
        displaySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    )

    // Spacing scale
    object Spacing {
        val xs = 4.dp
        val s = 8.dp
        val m = 12.dp
        val l = 16.dp
        val xl = 20.dp
        val xxl = 24.dp
        val xxxl = 32.dp
    }

    // Corner radius scale
    object Radius {
        val small = 8.dp
        val medium = 14.dp
        val large = 20.dp
        val pill = 999.dp
    }

    // Elevation scale
    object Elevation {
        val low = 2.dp
        val medium = 6.dp
        val high = 12.dp
    }
}

/**
 * Semantic "tier" for adaptive layout decisions.
 */
enum class NativeDeviceTier { Phone, Tablet, Desktop }

val LocalNativeDeviceTier = staticCompositionLocalOf { NativeDeviceTier.Phone }

/**
 * Color helpers shared by components.
 */
object NativeColorExtensions {
    fun Color.shine(alpha: Float = 0.35f): Color = Color(
        red = (red + 1f) / 2f,
        green = (green + 1f) / 2f,
        blue = (blue + 1f) / 2f,
        alpha = alpha,
    )
}

fun ColorScheme.isDark(): Boolean = background.luminance() < 0.5f

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
