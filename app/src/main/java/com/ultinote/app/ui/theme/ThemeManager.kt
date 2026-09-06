package com.ultinote.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ultinote.app.data.model.AppThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class KomorebiAestheticPalette(
    val themeOption: AppThemeOption,
    val colorScheme: ColorScheme,
    val canvasBackground: Color,
    val canvasDeskMat: Color,
    val paperGridColor: Color,
    val paperLineColor: Color,
    val toolbarBackground: Color,
    val accentPill: Color,
    // Liquid Glass Additions
    val glassBackground: Color,
    val glassBorder: Color,
    val glassSurface: Color,
    val glassHighlight: Color,
    val ambientGradient: List<Color>
)

object ThemeRepository {
    private val _currentTheme = MutableStateFlow(AppThemeOption.MATCHA_CREAM)
    val currentTheme: StateFlow<AppThemeOption> = _currentTheme

    fun setTheme(theme: AppThemeOption) {
        _currentTheme.value = theme
    }

    fun getPalette(theme: AppThemeOption): KomorebiAestheticPalette {
        return when (theme) {
            AppThemeOption.MATCHA_CREAM -> KomorebiAestheticPalette(
                themeOption = theme,
                colorScheme = lightColorScheme(
                    primary = Color(0xFF3F664F),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFCCE4D3),
                    onPrimaryContainer = Color(0xFF0C2417),
                    secondary = Color(0xFF6B8673),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFDDECE1),
                    onSecondaryContainer = Color(0xFF1E3224),
                    tertiary = Color(0xFFB07736),
                    onTertiary = Color.White,
                    background = Color(0xFFF7F5EE),
                    onBackground = Color(0xFF1B241F),
                    surface = Color(0xFFF3EFE6),
                    onSurface = Color(0xFF1B241F),
                    surfaceVariant = Color(0xFFE4DECFA),
                    onSurfaceVariant = Color(0xFF3F4640),
                    outline = Color(0xFF868F87),
                    outlineVariant = Color(0xFFC3CCC4)
                ),
                canvasBackground = Color(0xFFFAF8F2),
                canvasDeskMat = Color(0xFFEBE5D8),
                paperGridColor = Color(0x333F664F),
                paperLineColor = Color(0x403F664F),
                toolbarBackground = Color(0xCCFAF8F3),
                accentPill = Color(0xFFCCE4D3),
                glassBackground = Color(0xC8F8F6EF),
                glassBorder = Color(0x80FFFFFF),
                glassSurface = Color(0xDDFBF9F3),
                glassHighlight = Color(0x66FFFFFF),
                ambientGradient = listOf(Color(0xFFF5F3EC), Color(0xFFEAEFE7), Color(0xFFF5EFEB))
            )

            AppThemeOption.SAKURA_PASTEL -> KomorebiAestheticPalette(
                themeOption = theme,
                colorScheme = lightColorScheme(
                    primary = Color(0xFFBA4F6E),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFFD4DF),
                    onPrimaryContainer = Color(0xFF3B071B),
                    secondary = Color(0xFF945667),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFFFD4DF),
                    onSecondaryContainer = Color(0xFF351220),
                    tertiary = Color(0xFF864862),
                    onTertiary = Color.White,
                    background = Color(0xFFFFF6F8),
                    onBackground = Color(0xFF26191C),
                    surface = Color(0xFFFDEEF2),
                    onSurface = Color(0xFF26191C),
                    surfaceVariant = Color(0xFFF5DBE1),
                    onSurfaceVariant = Color(0xFF534145),
                    outline = Color(0xFF967E84),
                    outlineVariant = Color(0xFFE8CBD2)
                ),
                canvasBackground = Color(0xFFFFF8FA),
                canvasDeskMat = Color(0xFFF4DDE4),
                paperGridColor = Color(0x33BA4F6E),
                paperLineColor = Color(0x40BA4F6E),
                toolbarBackground = Color(0xCCFFF4F7),
                accentPill = Color(0xFFFFD4DF),
                glassBackground = Color(0xC8FFF5F8),
                glassBorder = Color(0x99FFFFFF),
                glassSurface = Color(0xDDFEF9FA),
                glassHighlight = Color(0x80FFFFFF),
                ambientGradient = listOf(Color(0xFFFFF5F7), Color(0xFFFDEBF2), Color(0xFFFFF8FB))
            )

            AppThemeOption.LAVENDER_TWILIGHT -> KomorebiAestheticPalette(
                themeOption = theme,
                colorScheme = lightColorScheme(
                    primary = Color(0xFF65509D),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFE4D8FF),
                    onPrimaryContainer = Color(0xFF200C49),
                    secondary = Color(0xFF5D566E),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFE4DAF5),
                    onSecondaryContainer = Color(0xFF1B1527),
                    tertiary = Color(0xFF7A4D60),
                    onTertiary = Color.White,
                    background = Color(0xFFF9F6FF),
                    onBackground = Color(0xFF1C1920),
                    surface = Color(0xFFF2EDFB),
                    onSurface = Color(0xFF1C1920),
                    surfaceVariant = Color(0xFFE3DCED),
                    onSurfaceVariant = Color(0xFF45424D),
                    outline = Color(0xFF8B8596),
                    outlineVariant = Color(0xFFCCC4D7)
                ),
                canvasBackground = Color(0xFFF9F7FF),
                canvasDeskMat = Color(0xFFE2DCEB),
                paperGridColor = Color(0x3365509D),
                paperLineColor = Color(0x4065509D),
                toolbarBackground = Color(0xCCF4F0FC),
                accentPill = Color(0xFFE4D8FF),
                glassBackground = Color(0xC8F6F2FF),
                glassBorder = Color(0x99FFFFFF),
                glassSurface = Color(0xDDFBFAFF),
                glassHighlight = Color(0x80FFFFFF),
                ambientGradient = listOf(Color(0xFFF8F5FF), Color(0xFFECE5FC), Color(0xFFF4F6FF))
            )

            AppThemeOption.OBSIDIAN_DARK -> KomorebiAestheticPalette(
                themeOption = theme,
                colorScheme = darkColorScheme(
                    primary = Color(0xFF5EEAD4),
                    onPrimary = Color(0xFF003830),
                    primaryContainer = Color(0xFF005047),
                    onPrimaryContainer = Color(0xFF7CF7E1),
                    secondary = Color(0xFFB0CCC4),
                    onSecondary = Color(0xFF1B352F),
                    secondaryContainer = Color(0xFF324B45),
                    onSecondaryContainer = Color(0xFFCCE8DF),
                    tertiary = Color(0xFFFBBF24),
                    onTertiary = Color(0xFF422C00),
                    background = Color(0xFF121418),
                    onBackground = Color(0xFFE2E5EB),
                    surface = Color(0xFF181B21),
                    onSurface = Color(0xFFE2E5EB),
                    surfaceVariant = Color(0xFF262A33),
                    onSurfaceVariant = Color(0xFFBAC0CA),
                    outline = Color(0xFF858C97),
                    outlineVariant = Color(0xFF3A3E48)
                ),
                canvasBackground = Color(0xFF16191E),
                canvasDeskMat = Color(0xFF0D0F12),
                paperGridColor = Color(0x225EEAD4),
                paperLineColor = Color(0x285EEAD4),
                toolbarBackground = Color(0xCC1A1E26),
                accentPill = Color(0xFF005047),
                glassBackground = Color(0xB51E222B),
                glassBorder = Color(0x335EEAD4),
                glassSurface = Color(0xD0242934),
                glassHighlight = Color(0x22FFFFFF),
                ambientGradient = listOf(Color(0xFF121418), Color(0xFF181D26), Color(0xFF13151A))
            )

            AppThemeOption.VINTAGE_PAPER -> KomorebiAestheticPalette(
                themeOption = theme,
                colorScheme = lightColorScheme(
                    primary = Color(0xFF754928),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFFD8BF),
                    onPrimaryContainer = Color(0xFF2B1300),
                    secondary = Color(0xFF6E5643),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFF7D9C3),
                    onSecondaryContainer = Color(0xFF251506),
                    tertiary = Color(0xFF8B462E),
                    onTertiary = Color.White,
                    background = Color(0xFFF8F3E9),
                    onBackground = Color(0xFF211A14),
                    surface = Color(0xFFF2ECE0),
                    onSurface = Color(0xFF211A14),
                    surfaceVariant = Color(0xFFE3D9CA),
                    onSurfaceVariant = Color(0xFF494036),
                    outline = Color(0xFF8A7F73),
                    outlineVariant = Color(0xFFCAC0B1)
                ),
                canvasBackground = Color(0xFFF9F4EB),
                canvasDeskMat = Color(0xFFE6DCBC),
                paperGridColor = Color(0x33754928),
                paperLineColor = Color(0x3B754928),
                toolbarBackground = Color(0xCCF4ECE2),
                accentPill = Color(0xFFFFD8BF),
                glassBackground = Color(0xC8F6EFE5),
                glassBorder = Color(0x80FFFFFF),
                glassSurface = Color(0xDDFBF6EF),
                glassHighlight = Color(0x66FFFFFF),
                ambientGradient = listOf(Color(0xFFF7F2E8), Color(0xFFEFE6D8), Color(0xFFF7EFE4))
            )

            AppThemeOption.NORDIC_FROST -> KomorebiAestheticPalette(
                themeOption = theme,
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2B5F70),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFBCE5F6),
                    onPrimaryContainer = Color(0xFF001E27),
                    secondary = Color(0xFF485E67),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFCBE4EE),
                    onSecondaryContainer = Color(0xFF061C24),
                    tertiary = Color(0xFF245061),
                    onTertiary = Color.White,
                    background = Color(0xFFF5F9FB),
                    onBackground = Color(0xFF161E21),
                    surface = Color(0xFFECF3F6),
                    onSurface = Color(0xFF161E21),
                    surfaceVariant = Color(0xFFD8E3E9),
                    onSurfaceVariant = Color(0xFF3D464B),
                    outline = Color(0xFF7C898F),
                    outlineVariant = Color(0xFFB8C5CB)
                ),
                canvasBackground = Color(0xFFF6FAFC),
                canvasDeskMat = Color(0xFFDAE5EA),
                paperGridColor = Color(0x332B5F70),
                paperLineColor = Color(0x3B2B5F70),
                toolbarBackground = Color(0xCCECF4F7),
                accentPill = Color(0xFFBCE5F6),
                glassBackground = Color(0xC8EEF5F8),
                glassBorder = Color(0x99FFFFFF),
                glassSurface = Color(0xDDF8FBFC),
                glassHighlight = Color(0x80FFFFFF),
                ambientGradient = listOf(Color(0xFFF4F9FB), Color(0xFFE6F0F5), Color(0xFFF3FAFC))
            )
        }
    }
}

val LocalKomorebiPalette = compositionLocalOf {
    ThemeRepository.getPalette(AppThemeOption.MATCHA_CREAM)
}
