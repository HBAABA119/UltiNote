package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.model.AppThemeOption

@Composable
fun MyApplicationTheme(
    selectedTheme: AppThemeOption? = null,
    selectedFont: AppFontOption? = null,
    content: @Composable () -> Unit
) {
    val currentThemeState by ThemeRepository.currentTheme.collectAsState()
    val currentFontState by FontRepository.currentFont.collectAsState()

    val activeTheme = selectedTheme ?: currentThemeState
    val activeFont = selectedFont ?: currentFontState

    val palette = ThemeRepository.getPalette(activeTheme)
    val typography = FontRepository.getTypography(activeFont)

    CompositionLocalProvider(
        LocalKomorebiPalette provides palette,
        LocalKomorebiFont provides activeFont
    ) {
        LiquidGlassThemeProvider(palette = palette) {
            MaterialTheme(
                colorScheme = palette.colorScheme,
                typography = typography,
                content = content
            )
        }
    }
}

