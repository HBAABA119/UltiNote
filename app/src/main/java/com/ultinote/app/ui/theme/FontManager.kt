package com.ultinote.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Aesthetic Typography Presets designed specifically for the Liquid Glass stationery aesthetic.
 */
enum class AppFontOption(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val sampleText: String,
    val fontFamily: FontFamily
) {
    MODERN_NEO_GROTESQUE(
        id = "modern_sans",
        displayName = "Glass Neo-Grotesque",
        subtitle = "Clean geometric sans with refined optical tracking",
        sampleText = "The quick brown fox jumps over the lazy dog",
        fontFamily = FontFamily.SansSerif
    ),
    EDITORIAL_SERIF(
        id = "editorial_serif",
        displayName = "Editorial Academic",
        subtitle = "Scholarly, literary serif for textbook & Cornell study",
        sampleText = "Knowledge emerges through deliberate contemplation",
        fontFamily = FontFamily.Serif
    ),
    STUDIO_MONOSPACE(
        id = "studio_mono",
        displayName = "Studio Monospace",
        subtitle = "Tabular precision for calculus equations & code",
        sampleText = "∫ f(x)dx = lim Σ f(c_i)Δx  [det(A - λI) = 0]",
        fontFamily = FontFamily.Monospace
    ),
    KYOTO_HANDWRITTEN(
        id = "kyoto_script",
        displayName = "Kyoto Journal Script",
        subtitle = "Organic cursive feel for creative digital planners",
        sampleText = "Notes, sketches, and mindful moments",
        fontFamily = FontFamily.Cursive
    )
}

object FontRepository {
    private val _currentFont = MutableStateFlow(AppFontOption.MODERN_NEO_GROTESQUE)
    val currentFont: StateFlow<AppFontOption> = _currentFont

    fun setFont(option: AppFontOption) {
        _currentFont.value = option
    }

    fun getTypography(option: AppFontOption): Typography {
        val family = option.fontFamily
        val isScript = option == AppFontOption.KYOTO_HANDWRITTEN
        val isMono = option == AppFontOption.STUDIO_MONOSPACE

        return Typography(
            displayLarge = TextStyle(
                fontFamily = family,
                fontWeight = if (isScript) FontWeight.Normal else FontWeight.Bold,
                fontSize = 54.sp,
                lineHeight = 62.sp,
                letterSpacing = if (isMono) 0.sp else (-0.5).sp
            ),
            displayMedium = TextStyle(
                fontFamily = family,
                fontWeight = if (isScript) FontWeight.Normal else FontWeight.Bold,
                fontSize = 42.sp,
                lineHeight = 50.sp,
                letterSpacing = if (isMono) 0.sp else (-0.25).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp,
                lineHeight = 38.sp,
                letterSpacing = if (isMono) 0.sp else 0.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 34.sp,
                letterSpacing = 0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                letterSpacing = if (isMono) 0.sp else 0.15.sp
            ),
            titleMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 23.sp,
                letterSpacing = if (isMono) 0.sp else 0.15.sp
            ),
            titleSmall = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = if (isMono) 0.sp else 0.25.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = if (isMono) 0.sp else 0.2.sp
            ),
            bodySmall = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.2.sp
            ),
            labelLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
            labelMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            ),
            labelSmall = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
}

val LocalKomorebiFont = compositionLocalOf { AppFontOption.MODERN_NEO_GROTESQUE }
