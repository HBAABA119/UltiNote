package com.ultinote.app.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive sizing model shared by every screen. Computed from the real window
 * width in dp, so phone / tablet / foldable / landscape all get tuned metrics.
 */
data class AdaptiveWindowSize(
    /** Horizontal padding for screen content (24dp phones, 48dp tablets). */
    val horizontalPadding: Dp,
    /** Full safe-area padding values for scrollable content. */
    val contentPadding: PaddingValues,
    /** Vertical rhythm between sections. */
    val sectionSpacing: Dp,
    /** Minimum card width for FlowRow-based grids. */
    val cardMinWidth: Dp,
    /** Note card height for grid items. */
    val noteCardHeight: Dp,
    /** Top bar title style scale factor. */
    val titleScale: Float,
    /** Whether the device is wide enough for side-by-side layouts. */
    val isExpanded: Boolean,
    /** Whether the device is in landscape orientation. */
    val isLandscape: Boolean,
)

@Composable
fun rememberWindowSizeClass(maxWidth: Dp): AdaptiveWindowSize {
    val configuration = LocalConfiguration.current

    val isExpanded = maxWidth >= 600.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val horizontalPadding = when {
        maxWidth >= 840.dp -> 48.dp
        isExpanded -> 32.dp
        else -> 24.dp
    }

    val sectionSpacing = if (isExpanded) 18.dp else 14.dp

    val cardMinWidth = when {
        maxWidth >= 900.dp -> 220.dp
        isExpanded -> 200.dp
        else -> 160.dp
    }

    val noteCardHeight = when {
        maxWidth >= 900.dp -> 190.dp
        isExpanded -> 180.dp
        else -> 170.dp
    }

    // Full safe-drawing padding so scrolling content clears status/nav bars and cutouts.
    val contentPadding: PaddingValues = WindowInsets.safeDrawing.asPaddingValues()

    return AdaptiveWindowSize(
        horizontalPadding = horizontalPadding,
        contentPadding = contentPadding,
        sectionSpacing = sectionSpacing,
        cardMinWidth = cardMinWidth,
        noteCardHeight = noteCardHeight,
        titleScale = if (isExpanded) 1.1f else 1f,
        isExpanded = isExpanded,
        isLandscape = isLandscape,
    )
}
